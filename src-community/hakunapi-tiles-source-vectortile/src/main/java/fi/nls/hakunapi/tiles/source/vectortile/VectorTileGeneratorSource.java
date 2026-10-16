package fi.nls.hakunapi.tiles.source.vectortile;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.config.HakunaConfigParser;
import fi.nls.hakunapi.tiles.BodyTile;
import fi.nls.hakunapi.tiles.VectorTileSource;
import fi.nls.hakunapi.tiles.Tile;
import fi.nls.hakunapi.tiles.TileBody;
import fi.nls.hakunapi.tiles.TileMatrix;
import fi.nls.hakunapi.tiles.TileMatrixSet;
import fi.nls.hakunapi.tiles.TileMatrixSetRegistry;
import fi.nls.hakunapi.tiles.VectorTileLayer;
import fi.nls.hakunapi.tiles.config.TilesConfigParser;

/** VectorTileSource that delegates to a configured VectorTileGenerator. */
public class VectorTileGeneratorSource implements VectorTileSource {
    private static final Logger LOG = LoggerFactory.getLogger(VectorTileGeneratorSource.class);

    public static final String TYPE = "vectortile-generator";

    public static final String DEFAULT_MEDIA_TYPE = "application/vnd.mapbox-vector-tile";

    private TileMatrixSetRegistry tms;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public VectorTileLayer parse(HakunaConfigParser cfg, Path path, TileMatrixSetRegistry tms, String layerId)
            throws Exception {
        this.tms = tms;

        TilesConfigParser tiles = new TilesConfigParser(cfg);
        String p = "tiles.layers." + layerId + ".";
        String gp = p + "vectortile.";

        String title = cfg.get(p + "title", layerId);
        String description = cfg.get(p + "description", title);
        List<String> tmsIds = tiles.readTileMatrixSetIds(layerId);
        String mediaType = cfg.get(gp + "format", DEFAULT_MEDIA_TYPE);
        Set<String> passthrough = Set.of(cfg.getMultiple(gp + "passthroughParams"));
        List<String> collectionIds = List.of(cfg.getMultiple(gp + "collections", new String[] { layerId }));

        VectorTileGenerator generator = bindGenerator(cfg, gp, layerId);

        LOG.info("generator tile layer {} -> generator={} type={} collections={}",
                layerId, generator.getType(), mediaType, collectionIds);
        return new VectorTileGeneratorLayer(layerId, title, description, this, tmsIds,
                List.of(mediaType), null, generator, collectionIds, passthrough);
    }

    private VectorTileGenerator bindGenerator(HakunaConfigParser cfg, String gp, String layerId) {
        String type = cfg.get(gp + "generator", cfg.get("default.tiles.layers.vectortile.generator"));
        if (type == null) {
            throw new IllegalArgumentException("tile layer " + layerId + ": " + gp
                    + "generator is required (or set default.tiles.layers.vectortile.generator)");
        }
        VectorTileGenerator generator = VectorTileGeneratorProvider.get(type);
        if (generator == null) {
            throw new IllegalArgumentException("tile layer " + layerId
                    + ": unknown vector tile generator type: " + type);
        }
        return generator;
    }

    @Override
    public Set<String> getPassthroughParams(VectorTileLayer layer) {
        return ((VectorTileGeneratorLayer) layer).getPassthroughParams();
    }

    @Override
    public Optional<Tile> getTile(VectorTileLayer layer, String tileMatrixSetId, String tileMatrix,
            long tileRow, long tileCol, List<FeatureType> collections,
            String mediaType, Map<String, String> requestParams) throws Exception {
        VectorTileGeneratorLayer l = (VectorTileGeneratorLayer) layer;
        if (!l.supportsTileMatrixSet(tileMatrixSetId)) {
            return Optional.empty();
        }

        Optional<TileMatrixSet> tmsOpt = tms.get(tileMatrixSetId);
        if (tmsOpt.isEmpty()) {
            return Optional.empty();
        }
        TileMatrixSet tileMatrixSet = tmsOpt.get();
        Optional<TileMatrix> tmOpt = tileMatrixSet.getTileMatrix(tileMatrix);
        if (tmOpt.isEmpty()) {
            return Optional.empty();
        }
        TileMatrix tm = tmOpt.get();

        if (!tm.contains(tileRow, tileCol)) {
            return Optional.empty();
        }

        TileContext ctx = toTileContext(tileMatrixSet, tm, tileRow, tileCol);
        Optional<TileBody> body = l.getGenerator().generate(ctx, collections, mediaType, requestParams);
        return body.map(b -> new BodyTile(tileMatrixSetId, tileMatrix, tileRow, tileCol,
                l.getMediaTypes().get(0), b));
    }

    public static TileContext toTileContext(TileMatrixSet tms, TileMatrix tm, long tileRow, long tileCol) {
        double minX = tm.getTileMinX(tileCol);
        double maxY = tm.getTileMaxY(tileRow);
        return new TileContext(tms.getSrid(), minX, maxY - tm.getTileSpanY(),
                minX + tm.getTileSpanX(), maxY,
                tm.getTileWidth(), tm.getTileHeight(), tm.getCellSize());
    }
}
