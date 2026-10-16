package fi.nls.hakunapi.tiles.config;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.nls.hakunapi.core.config.HakunaConfigParser;
import fi.nls.hakunapi.core.util.CrsUtil;
import fi.nls.hakunapi.tiles.VectorTileSource;
import fi.nls.hakunapi.tiles.DefaultTileMatrixSetRegistry;
import fi.nls.hakunapi.tiles.MapTileLayer;
import fi.nls.hakunapi.tiles.TileMatrix;
import fi.nls.hakunapi.tiles.TileMatrixSet;
import fi.nls.hakunapi.tiles.TileMatrixSetRegistry;
import fi.nls.hakunapi.tiles.MapTileSource;
import fi.nls.hakunapi.tiles.VectorTileLayer;

/** Wraps HakunaConfigParser so hakunapi-core stays free of the tiles model. */
public class TilesConfigParser {

    private static final Logger LOG = LoggerFactory.getLogger(TilesConfigParser.class);

    public static final String PROP_LAYERS = "tiles.layers";
    public static final String PROP_TILE_MATRIX_SETS = "tileMatrixSets";

    /** Explicit tiles.layers.<id> definitions take precedence over this toggle. */
    public static final String PROP_VECTOR = "tiles.vector";

    /** OGC standardized rendering pixel size in metres (0.28 mm), OGC 17-083r4 section 6.2. */
    public static final double STANDARDIZED_PIXEL_SIZE = 0.00028;

    /** Relative tolerance for the whole-number-of-tiles check, absorbing double rounding in extent/resolution. */
    public static final double WHOLE_TILE_EPSILON = 1e-6;

    private final HakunaConfigParser cfg;

    public TilesConfigParser(HakunaConfigParser cfg) {
        this.cfg = cfg;
    }

    public String[] readTileLayerIds() {
        return cfg.getMultiple(PROP_LAYERS);
    }

    public boolean hasTileLayers() {
        return readTileLayerIds().length > 0;
    }

    public boolean isVectorAutoPublishEnabled() {
        return Boolean.parseBoolean(cfg.get(PROP_VECTOR, "false"));
    }


    public TileMatrixSetRegistry readTileMatrixSets() {
        Collection<TileMatrixSet> additional = new ArrayList<>();
        for (String id : cfg.getMultiple(PROP_TILE_MATRIX_SETS)) {
            additional.add(readTileMatrixSet(id));
        }
        return new DefaultTileMatrixSetRegistry(additional);
    }

    /** Matrix width/height derive from extent / (resolution * tileWidth), which must be a whole number of tiles at every level. */
    public TileMatrixSet readTileMatrixSet(String id) {
        String p = PROP_TILE_MATRIX_SETS + "." + id + ".";
        String title = cfg.get(p + "title", id);
        int srid = Integer.parseInt(cfg.getRequired(p + "srid"));
        String crs = CrsUtil.toUri(srid);
        int tileWidth = Integer.parseInt(cfg.get(p + "tileWidth", "256"));
        int tileHeight = Integer.parseInt(cfg.get(p + "tileHeight", "256"));

        double[] bbox = parseDoubles(cfg.getRequired(p + "bbox"));
        if (bbox.length != 4) {
            throw new IllegalArgumentException("tileMatrixSet " + id + ": bbox must be minX,minY,maxX,maxY");
        }
        double minX = bbox[0], minY = bbox[1], maxX = bbox[2], maxY = bbox[3];

        double originX = minX;
        double originY = maxY;

        double[] resolutions = parseDoubles(cfg.getRequired(p + "resolutions"));
        if (resolutions.length == 0) {
            throw new IllegalArgumentException("tileMatrixSet " + id + ": resolutions must not be empty");
        }
        String[] matrixIds = cfg.getMultiple(p + "matrixIds");
        if (matrixIds.length != 0 && matrixIds.length != resolutions.length) {
            throw new IllegalArgumentException("tileMatrixSet " + id
                    + ": matrixIds length must match resolutions length");
        }

        double extentWidth = maxX - minX;
        double extentHeight = maxY - minY;
        List<TileMatrix> matrices = new ArrayList<>(resolutions.length);
        for (int z = 0; z < resolutions.length; z++) {
            double res = resolutions[z];
            String matrixId = matrixIds.length != 0 ? matrixIds[z] : Integer.toString(z);
            double scaleDenominator = res / STANDARDIZED_PIXEL_SIZE;
            int matrixWidth = wholeTiles(id, matrixId, "width", extentWidth, res, tileWidth);
            int matrixHeight = wholeTiles(id, matrixId, "height", extentHeight, res, tileHeight);
            matrices.add(new TileMatrix(matrixId, scaleDenominator, res, originX, originY,
                    tileWidth, tileHeight, matrixWidth, matrixHeight));
        }
        return new TileMatrixSet(id, title, crs, srid, matrices);
    }

    private static int wholeTiles(String tmsId, String matrixId, String axis,
            double extent, double res, int tilePixels) {
        double tiles = extent / (res * tilePixels);
        long rounded = Math.round(tiles);
        if (rounded <= 0 || Math.abs(tiles - rounded) > WHOLE_TILE_EPSILON * rounded) {
            throw new IllegalArgumentException("tileMatrixSet " + tmsId + " matrix " + matrixId
                    + ": extent " + axis + " is not a whole number of tiles at resolution " + res
                    + " (got " + tiles + " tiles)");
        }
        return (int) rounded;
    }

    private static double[] parseDoubles(String csv) {
        String[] parts = csv.split(",");
        double[] out = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            out[i] = Double.parseDouble(parts[i].trim());
        }
        return out;
    }

    public String readTileLayerType(String layerId) {
        String p = PROP_LAYERS + "." + layerId + ".";
        return cfg.get(p + "type", readDefaultTileLayerType());
    }

    public String readDefaultTileLayerType() {
        return cfg.get("default.tiles.layers.type");
    }

    public MapTileLayer readMapTileLayer(Path path, Map<String, MapTileSource> sourcesByType,
            TileMatrixSetRegistry tms, String layerId) throws Exception {
        LOG.info("map tile layer {}", layerId);
        MapTileSource source = resolveSource(sourcesByType, layerId);
        return source.parse(cfg, path, tms, layerId);
    }

    public VectorTileLayer readVectorTileLayer(Path path, Map<String, VectorTileSource> sourcesByType,
            TileMatrixSetRegistry tms, String layerId) throws Exception {
        LOG.info("vector tile layer {}", layerId);
        VectorTileSource source = resolveSource(sourcesByType, layerId);
        return source.parse(cfg, path, tms, layerId);
    }

    private <S> S resolveSource(Map<String, S> sourcesByType, String layerId) {
        if (sourcesByType.size() == 1) {
            return sourcesByType.values().iterator().next();
        }
        String type = readTileLayerType(layerId);
        if (type == null) {
            throw new IllegalArgumentException("Missing tile source type for layer: " + layerId);
        }
        S source = sourcesByType.get(type);
        if (source == null) {
            throw new IllegalArgumentException("Unknown tile source type: " + type + ", layer: " + layerId);
        }
        return source;
    }

    /** Resolution order: per-layer tileMatrixSets, then default.tiles.layers.tileMatrixSets, then WebMercatorQuad. */
    public List<String> readTileMatrixSetIds(String layerId) {
        String p = PROP_LAYERS + "." + layerId + ".";
        String[] ids = cfg.getMultiple(p + "tileMatrixSets", null);
        if (ids == null) {
            ids = cfg.getMultiple("default." + PROP_LAYERS + ".tileMatrixSets",
                    new String[] { DefaultTileMatrixSetRegistry.WEB_MERCATOR_QUAD });
        }
        return Arrays.asList(ids);
    }

}
