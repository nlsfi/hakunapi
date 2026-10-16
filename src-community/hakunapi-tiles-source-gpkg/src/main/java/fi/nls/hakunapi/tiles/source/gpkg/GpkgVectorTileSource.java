package fi.nls.hakunapi.tiles.source.gpkg;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.config.HakunaConfigParser;
import fi.nls.hakunapi.tiles.BytesTile;
import fi.nls.hakunapi.tiles.VectorTileSource;
import fi.nls.hakunapi.tiles.Tile;
import fi.nls.hakunapi.tiles.TileMatrixSetRegistry;
import fi.nls.hakunapi.tiles.VectorTileLayer;
import fi.nls.hakunapi.tiles.config.TilesConfigParser;

public class GpkgVectorTileSource implements VectorTileSource {

    private static final Logger LOG = LoggerFactory.getLogger(GpkgVectorTileSource.class);

    public static final String TYPE = "gpkg-vector";

    // MVT has no magic bytes to sniff, so the type is declared
    public static final String DEFAULT_MEDIA_TYPE = "application/vnd.mapbox-vector-tile";

    private final Map<Path, Connection> connections = new LinkedHashMap<>();

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public VectorTileLayer parse(HakunaConfigParser cfg, Path path, TileMatrixSetRegistry tms, String layerId)
            throws Exception {
        TilesConfigParser tiles = new TilesConfigParser(cfg);
        String p = "tiles.layers." + layerId + ".";

        String title = cfg.get(p + "title", layerId);
        String description = cfg.get(p + "description", title);
        List<String> tmsIds = tiles.readTileMatrixSetIds(layerId);
        List<String> collectionIds = List.of(cfg.getMultiple(p + "collections", new String[] { layerId }));

        GpkgTileConfig config = GpkgTiles.parse(cfg, path, tms, layerId, tmsIds, DEFAULT_MEDIA_TYPE,
                connections);

        LOG.info("gpkg vector tile layer {} -> table={} type={} collections={}",
                layerId, config.tableName(), config.mediaType(), collectionIds);
        return new GpkgVectorTileLayer(layerId, title, description, this, collectionIds, tmsIds,
                List.of(config.mediaType()), null, config);
    }

    @Override
    public boolean needsCollections(VectorTileLayer layer) {
        return false;
    }

    @Override
    public Optional<Tile> getTile(VectorTileLayer layer, String tileMatrixSetId, String tileMatrix,
            long tileRow, long tileCol, List<FeatureType> collections,
            String mediaType, Map<String, String> requestParams) throws Exception {
        GpkgTileConfig cfg = ((GpkgVectorTileLayer) layer).getGpkgConfig();
        byte[] bytes = GpkgTiles.readTile(cfg, tileMatrixSetId, tileMatrix, tileRow, tileCol);
        if (bytes == null) {
            return Optional.empty();
        }
        String contentEncoding = cfg.contentEncoding();
        if (contentEncoding == null && GpkgFormat.isGzip(bytes)) {
            contentEncoding = "gzip";
        }
        return Optional.of(new BytesTile(tileMatrixSetId, tileMatrix, tileRow, tileCol,
                cfg.mediaType(), contentEncoding, bytes));
    }

    @Override
    public void close() throws Exception {
        GpkgTiles.closeAll(connections);
    }

}
