package fi.nls.hakunapi.tiles.generator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.FilterParser;
import fi.nls.hakunapi.core.config.HakunaConfigParser;
import fi.nls.hakunapi.cql2.text.CQL2Text;
import fi.nls.hakunapi.gpkg.GPKG;
import fi.nls.hakunapi.gpkg.GPKGSpatialRefSystems;
import fi.nls.hakunapi.tiles.TileMatrix;
import fi.nls.hakunapi.tiles.TileMatrixSet;
import fi.nls.hakunapi.tiles.source.vectortile.JavaVectorTileGenerator;
import fi.nls.hakunapi.tiles.source.vectortile.VectorTileGenerator;
import fi.nls.hakunapi.tiles.source.vectortile.VectorTileGeneratorProvider;
import fi.nls.hakunapi.tiles.source.vectortile.VectorTileGeneratorSource;

public class TilesGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(TilesGenerator.class);

    public static void main(String[] args) {
        try {
            int status = run(args);
            System.exit(status);
        } catch (Exception e) {
            Throwable root = e;
            while (root instanceof ExecutionException && root.getCause() != null) {
                root = root.getCause();
            }
            System.err.println("ERROR: " + root.getMessage());
            LOG.debug("Generation failed", e);
            System.exit(1);
        }
    }

    private static int run(String[] args) throws Exception {
        Args parsed = Args.parse(args);
        if (parsed == null) {
            usage();
            return 2;
        }

        try (GeneratorBootstrap bootstrap = GeneratorBootstrap.read(parsed.configPath)) {
            List<FilterParser> filters = List.of(CQL2Text.INSTANCE);

            HakunaConfigParser controlCfg = GeneratorControl.load(parsed.controlPath);
            String tmsId = GeneratorControl.readTileMatrixSetId(controlCfg);
            TileMatrixSet tms = bootstrap.getTileMatrixSets().get(tmsId)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown tileMatrixSet '" + tmsId
                            + "'; define it as tileMatrixSets." + tmsId + ".* in the service configuration"));

            GeneratorControl control = GeneratorControl.readRanges(controlCfg, bootstrap.getService(),
                    filters, bootstrap.getSridCode(tms.getSrid()));
            TileSources sources = TileSources.read(controlCfg, control);

            int minZoom = parsed.minZoom != null ? parsed.minZoom : control.getMinZoom();
            int maxZoom = parsed.maxZoom != null ? parsed.maxZoom : control.getMaxZoom();
            validateZooms(control, tms, minZoom, maxZoom);

            double[] bbox = parsed.bbox != null ? parsed.bbox : fullExtent(tms);

            VectorTileGenerator generator = resolveGenerator(parsed.generatorType);
            LOG.info("Generating into {} using tileMatrixSet {} and generator {}",
                    parsed.outputPath, tms.getId(), generator.getType());

            Map<String, List<FeatureType>> collections =
                    PyramidGenerator.resolveCollections(bootstrap, control);

            return generate(parsed, control, sources, tms, generator, collections, minZoom, maxZoom, bbox);
        }
    }

    private static int generate(Args parsed, GeneratorControl control, TileSources sources,
            TileMatrixSet tms, VectorTileGenerator generator,
            Map<String, List<FeatureType>> collections,
            int minZoom, int maxZoom, double[] bbox) throws Exception {
        boolean fileExists = Files.exists(parsed.outputPath);
        List<String> tables = sources.getSourceIds();

        try (Connection c = open(parsed.outputPath)) {
            if (!fileExists) {
                GPKG.init(c);
            }
            GPKG.initTiles(c);
            GPKG.insertSpatialRefSys(c, GPKGSpatialRefSystems.get(tms.getSrid()));

            List<String> existing = PyramidTables.existingTables(c, tables);
            if (!existing.isEmpty()) {
                throw new IllegalArgumentException("Refusing to overwrite tiles table(s) " + existing
                        + " in " + parsed.outputPath);
            }
            PyramidTables.createTables(c, tables, tms, minZoom, maxZoom);

            PyramidGenerator pyramid = new PyramidGenerator(control, sources, tms, generator,
                    VectorTileGeneratorSource.DEFAULT_MEDIA_TYPE, collections);
            try (PyramidTables pyramidTables = new PyramidTables(c, tables)) {
                long written = pyramid.generate(pyramidTables, minZoom, maxZoom,
                        bbox[0], bbox[1], bbox[2], bbox[3], parsed.threads);
                System.out.println("Wrote " + written + " tiles into " + parsed.outputPath);
                for (Map.Entry<String, Long> e : pyramidTables.getWrittenBySource().entrySet()) {
                    if (e.getValue() > 0) {
                        System.out.printf("  %-28s %,8d%n", e.getKey(), e.getValue());
                    }
                }
            }
        }
        return 0;
    }

    /** Autocommit: GPKG.init's PRAGMA synchronous fails inside a transaction. */
    private static Connection open(Path path) throws Exception {
        return DriverManager.getConnection("jdbc:sqlite:" + path.toAbsolutePath());
    }

    private static VectorTileGenerator resolveGenerator(String type) {
        String wanted = type != null ? type : JavaVectorTileGenerator.TYPE;
        VectorTileGenerator generator = VectorTileGeneratorProvider.get(wanted);
        if (generator == null) {
            throw new IllegalArgumentException("Unknown vector tile generator type '" + wanted + "'");
        }
        return generator;
    }

    private static void validateZooms(GeneratorControl control, TileMatrixSet tms, int minZoom, int maxZoom) {
        if (minZoom > maxZoom) {
            throw new IllegalArgumentException("--min-zoom " + minZoom + " is above --max-zoom " + maxZoom);
        }
        int levels = tms.getTileMatrices().size();
        if (minZoom < 0 || maxZoom >= levels) {
            throw new IllegalArgumentException("Zoom levels " + minZoom + "-" + maxZoom
                    + " are outside tileMatrixSet " + tms.getId() + ", which has levels 0-" + (levels - 1));
        }
        for (int zoom = minZoom; zoom <= maxZoom; zoom++) {
            control.getRange(zoom);
        }
    }

    private static double[] fullExtent(TileMatrixSet tms) {
        TileMatrix tm = tms.getTileMatrices().get(0);
        double minX = tm.getPointOfOriginX();
        double maxY = tm.getPointOfOriginY();
        return new double[] {
                minX, maxY - tm.getMatrixHeight() * tm.getTileSpanY(),
                minX + tm.getMatrixWidth() * tm.getTileSpanX(), maxY };
    }

    private static void usage() {
        System.err.println("Usage: hakunapi-tiles-generator [options] <config.properties> <control.properties> <output.gpkg>");
        System.err.println();
        System.err.println("  --min-zoom <n>    coarsest zoom to write (default: the control file's minimum)");
        System.err.println("  --max-zoom <n>    finest zoom to write (default: the control file's maximum)");
        System.err.println("  --bbox <minx,miny,maxx,maxy>");
        System.err.println("                    limit to this extent, in the tiling scheme's CRS");
        System.err.println("  --threads <n>     parallel render workers (default: available processors)");
        System.err.println("  --generator <id>  vector tile generator type (default: hakunapi)");
    }

    private static class Args {

        Path configPath;
        Path controlPath;
        Path outputPath;
        Integer minZoom;
        Integer maxZoom;
        double[] bbox;
        int threads = Runtime.getRuntime().availableProcessors();
        String generatorType;

        static Args parse(String[] args) {
            Args a = new Args();
            List<String> positional = new ArrayList<>(3);
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (arg.startsWith("--") && i + 1 >= args.length) {
                    System.err.println("Option " + arg + " needs a value");
                    return null;
                }
                switch (arg) {
                case "--min-zoom":
                    a.minZoom = Integer.parseInt(args[++i]);
                    break;
                case "--max-zoom":
                    a.maxZoom = Integer.parseInt(args[++i]);
                    break;
                case "--bbox":
                    a.bbox = parseBbox(args[++i]);
                    break;
                case "--threads":
                    a.threads = Integer.parseInt(args[++i]);
                    break;
                case "--generator":
                    a.generatorType = args[++i];
                    break;
                default:
                    if (arg.startsWith("--")) {
                        System.err.println("Unknown option: " + arg);
                        return null;
                    }
                    positional.add(arg);
                }
            }
            if (positional.size() != 3) {
                return null;
            }
            if (a.threads < 1) {
                System.err.println("--threads must be at least 1");
                return null;
            }
            a.configPath = Paths.get(positional.get(0)).toAbsolutePath();
            a.controlPath = Paths.get(positional.get(1)).toAbsolutePath();
            a.outputPath = Paths.get(positional.get(2)).toAbsolutePath();
            return a;
        }

        private static double[] parseBbox(String csv) {
            String[] parts = csv.split(",");
            if (parts.length != 4) {
                throw new IllegalArgumentException("--bbox must be minX,minY,maxX,maxY");
            }
            double[] bbox = new double[4];
            for (int i = 0; i < 4; i++) {
                bbox[i] = Double.parseDouble(parts[i].trim());
            }
            if (bbox[0] >= bbox[2] || bbox[1] >= bbox[3]) {
                throw new IllegalArgumentException("--bbox is empty or inverted");
            }
            return bbox;
        }

    }

}
