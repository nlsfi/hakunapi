package fi.nls.hakunapi.tiles.generator;

import java.io.ByteArrayOutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.SimpleFeatureType;
import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.tiles.TileBody;
import fi.nls.hakunapi.tiles.TileMatrix;
import fi.nls.hakunapi.tiles.TileMatrixSet;
import fi.nls.hakunapi.tiles.source.vectortile.JavaVectorTileGenerator;
import fi.nls.hakunapi.tiles.source.vectortile.TileContext;
import fi.nls.hakunapi.tiles.source.vectortile.VectorTileGenerator;
import fi.nls.hakunapi.tiles.source.vectortile.VectorTileGeneratorSource;

public class PyramidGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(PyramidGenerator.class);

    private static final int QUEUE_DEPTH_PER_WORKER = 4;

    private static final int BLOCK = Integer.getInteger("hakunapi.tiles.block", 4);

    private boolean useBlocks() {
        return BLOCK > 1 && generator.supportsBlocks();
    }

    private final GeneratorControl control;
    private final TileSources sources;
    private final TileMatrixSet tms;
    private final VectorTileGenerator generator;
    private final String mediaType;

    private final Map<String, List<FeatureType>> collectionsByRange;

    public PyramidGenerator(GeneratorControl control, TileSources sources, TileMatrixSet tms,
            VectorTileGenerator generator, String mediaType,
            Map<String, List<FeatureType>> collectionsByRange) {
        this.control = control;
        this.sources = sources;
        this.tms = tms;
        this.generator = generator;
        this.mediaType = mediaType;
        this.collectionsByRange = collectionsByRange;
    }

    public static Map<String, List<FeatureType>> resolveCollections(GeneratorBootstrap bootstrap,
            GeneratorControl control) throws Exception {
        Map<String, List<FeatureType>> byRange = new LinkedHashMap<>();
        for (ZoomRange range : control.getRanges()) {
            List<String> ids = range.collectionIds();
            List<FeatureType> collections = new ArrayList<>(ids.size());
            for (int i = 0; i < ids.size(); i++) {
                String id = ids.get(i);
                Filter rangeFilter = range.filters().get(id);
                if (rangeFilter == null) {
                    collections.add(bootstrap.getService().getCollection(id));
                } else {
                    SimpleFeatureType ft = bootstrap.readCollection(id);
                    List<Filter> filters = new ArrayList<>(ft.getStaticFilters());
                    filters.add(rangeFilter);
                    ft.setStaticFilters(filters);
                    collections.add(ft);
                }
            }
            byRange.put(range.name(), collections);
        }
        return byRange;
    }

    public long generate(PyramidTables tables, int minZoom, int maxZoom,
            double minX, double minY, double maxX, double maxY,
            int threads) throws Exception {
        List<TileRange> ranges = new ArrayList<>(maxZoom - minZoom + 1);
        long total = 0;
        for (int zoom = minZoom; zoom <= maxZoom; zoom++) {
            TileRange range = TileRange.of(tms.getTileMatrices().get(zoom), zoom, minX, minY, maxX, maxY);
            ranges.add(range);
            total += range.size();
        }
        LOG.info("Generating {} tiles across zoom levels {}-{} with {} threads", total, minZoom, maxZoom, threads);

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            return run(tables, ranges, pool, threads);
        } finally {
            pool.shutdownNow();
            pool.awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /** Writes in completion order: a GeoPackage tile table is addressed by key, not insertion order. */
    private long run(PyramidTables tables, List<TileRange> ranges,
            ExecutorService pool, int threads) throws Exception {
        int depth = Math.max(1, threads * QUEUE_DEPTH_PER_WORKER);
        CompletionService<Rendered> rendered = new ExecutorCompletionService<>(pool);
        int inFlight = 0;
        long empty = 0;

        try {
            for (int r = 0; r < ranges.size(); r++) {
                TileRange range = ranges.get(r);
                if (range.isEmpty()) {
                    continue;
                }
                int side = useBlocks() ? BLOCK : 1;
                for (long row = range.minRow(); row <= range.maxRow(); row += side) {
                    long rowEnd = Math.min(row + side - 1, range.maxRow());
                    for (long col = range.minCol(); col <= range.maxCol(); col += side) {
                        long colEnd = Math.min(col + side - 1, range.maxCol());
                        rendered.submit(renderBlockTask(range.zoom(), row, rowEnd, col, colEnd));
                        inFlight++;
                        if (inFlight >= depth) {
                            empty += write(tables, rendered.take().get());
                            inFlight--;
                        }
                    }
                }
            }
            while (inFlight > 0) {
                empty += write(tables, rendered.take().get());
                inFlight--;
            }
        } catch (Exception e) {
            LOG.warn("Generation failed: the tile tables {} are incomplete and were kept; they must be removed before a rerun",
                    tables.getWrittenBySource().keySet());
            salvage(tables, pool, e);
            throw e;
        }
        tables.finish();

        LOG.info("Wrote {} tiles across {} table(s), {} addresses were empty everywhere",
                tables.getWritten(), tables.getWrittenBySource().size(), empty);
        return tables.getWritten();
    }

    /** A flush failure must not mask the render failure. */
    private static void salvage(PyramidTables tables, ExecutorService pool, Exception cause) {
        try {
            tables.flush();
        } catch (Exception e) {
            cause.addSuppressed(e);
        }
        pool.shutdownNow();
    }

    private record Rendered(List<TileAddress> addresses, List<Map<String, byte[]>> tiles) {
    }

    /** Returns how many of the block's addresses wrote no row. */
    private long write(PyramidTables tables, Rendered r) throws SQLException {
        long empty = 0;
        for (int i = 0; i < r.addresses().size(); i++) {
            Map<String, byte[]> tiles = r.tiles().get(i);
            if (tiles.isEmpty()) {
                empty++;
                continue;
            }
            for (Map.Entry<String, byte[]> e : tiles.entrySet()) {
                tables.write(e.getKey(), r.addresses().get(i), e.getValue());
            }
        }
        return empty;
    }

    private List<byte[]> renderPerTile(List<TileContext> ctxs, FeatureType ft) throws Exception {
        List<byte[]> layers = new ArrayList<>(ctxs.size());
        for (int t = 0; t < ctxs.size(); t++) {
            TileBody body = generator.generate(ctxs.get(t), List.of(ft),
                    mediaType, Map.of()).orElse(null);
            if (body == null) {
                layers.add(new byte[0]);
                continue;
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            body.writeTo(baos);
            layers.add(baos.toByteArray());
        }
        return layers;
    }

    /** Empty tiles are absent from the result: GeoPackage has no empty-tile marker. */
    private Callable<Rendered> renderBlockTask(int zoom, long minRow, long maxRow,
            long minCol, long maxCol) {
        return () -> {
            TileMatrix tm = tms.getTileMatrices().get(zoom);
            ZoomRange range = control.getRange(zoom);
            List<FeatureType> collections = collectionsByRange.get(range.name());
            List<String> ids = range.collectionIds();

            List<TileAddress> addresses = new ArrayList<>();
            List<TileContext> ctxs = new ArrayList<>();
            for (long row = minRow; row <= maxRow; row++) {
                for (long col = minCol; col <= maxCol; col++) {
                    addresses.add(new TileAddress(zoom, row, col));
                    ctxs.add(VectorTileGeneratorSource.toTileContext(tms, tm, row, col));
                }
            }

            // Union of the tiles' clip rects, buffer included; partial edge blocks have no corner order.
            double[] queryBox = { Double.MAX_VALUE, Double.MAX_VALUE,
                    -Double.MAX_VALUE, -Double.MAX_VALUE };
            for (int t = 0; t < ctxs.size(); t++) {
                TileContext ctx = ctxs.get(t);
                double margin = JavaVectorTileGenerator.clipMargin(ctx);
                queryBox[0] = Math.min(queryBox[0], ctx.minX() - margin);
                queryBox[1] = Math.min(queryBox[1], ctx.minY() - margin);
                queryBox[2] = Math.max(queryBox[2], ctx.maxX() + margin);
                queryBox[3] = Math.max(queryBox[3], ctx.maxY() + margin);
            }

            List<Map<String, ByteArrayOutputStream>> bySource = new ArrayList<>(ctxs.size());
            for (int t = 0; t < ctxs.size(); t++) {
                bySource.add(new LinkedHashMap<>());
            }
            for (int i = 0; i < collections.size(); i++) {
                List<byte[]> layers = useBlocks()
                        ? generator.generateBlock(ctxs, List.of(collections.get(i)), queryBox)
                        : renderPerTile(ctxs, collections.get(i));
                String source = sources.getSource(ids.get(i));
                for (int t = 0; t < layers.size(); t++) {
                    byte[] layer = layers.get(t);
                    if (layer.length == 0 || !MvtTile.hasFeatures(layer)) {
                        continue;
                    }
                    bySource.get(t).computeIfAbsent(source, __ -> new ByteArrayOutputStream())
                            .write(layer);
                }
            }

            List<Map<String, byte[]>> tiles = new ArrayList<>(ctxs.size());
            for (int t = 0; t < ctxs.size(); t++) {
                Map<String, ByteArrayOutputStream> src = bySource.get(t);
                Map<String, byte[]> out = new LinkedHashMap<>(src.size());
                for (Map.Entry<String, ByteArrayOutputStream> e : src.entrySet()) {
                    out.put(e.getKey(), TileGzip.compress(e.getValue().toByteArray()));
                }
                tiles.add(out);
            }
            return new Rendered(addresses, tiles);
        };
    }

}
