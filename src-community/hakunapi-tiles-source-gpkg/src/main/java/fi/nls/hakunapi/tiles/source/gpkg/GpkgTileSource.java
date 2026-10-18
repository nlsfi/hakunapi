package fi.nls.hakunapi.tiles.source.gpkg;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.nls.hakunapi.core.config.HakunaConfigParser;
import fi.nls.hakunapi.tiles.BytesTile;
import fi.nls.hakunapi.tiles.Tile;
import fi.nls.hakunapi.tiles.MapTileLayer;
import fi.nls.hakunapi.tiles.TileMatrixSetRegistry;
import fi.nls.hakunapi.tiles.MapTileSource;
import fi.nls.hakunapi.tiles.config.TilesConfigParser;

public class GpkgTileSource implements MapTileSource {

    private static final Logger LOG = LoggerFactory.getLogger(GpkgTileSource.class);

    public static final String TYPE = "gpkg";

    private final Map<Path, Connection> connections = new LinkedHashMap<>();

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public MapTileLayer parse(HakunaConfigParser cfg, Path path, TileMatrixSetRegistry tms, String layerId)
            throws Exception {
        TilesConfigParser tiles = new TilesConfigParser(cfg);
        String p = "tiles.layers." + layerId + ".";

        String title = cfg.get(p + "title", layerId);
        String description = cfg.get(p + "description", title);
        List<String> tmsIds = tiles.readTileMatrixSetIds(layerId);

        GpkgTileConfig config = GpkgTiles.parse(cfg, path, tms, layerId, tmsIds, null, connections);

        LOG.info("gpkg tile layer {} -> table={} type={}", layerId, config.tableName(), config.mediaType());
        return new GpkgTileLayer(layerId, title, description, this, tmsIds,
                List.of(config.mediaType()), null, config);
    }

    @Override
    public Optional<Tile> getTile(MapTileLayer layer, String tileMatrixSetId, String tileMatrix,
            long tileRow, long tileCol, String mediaType, Map<String, String> requestParams) throws Exception {
        GpkgTileConfig cfg = ((GpkgTileLayer) layer).getGpkgConfig();
        byte[] bytes = GpkgTiles.readTile(cfg, tileMatrixSetId, tileMatrix, tileRow, tileCol);
        if (bytes == null) {
            return Optional.empty();
        }

        String tileMediaType = cfg.mediaType();
        if (!cfg.formatConfigured()) {
            String sniffed = GpkgFormat.sniff(bytes);
            if (sniffed != null) {
                tileMediaType = sniffed;
            }
        }
        String contentEncoding = cfg.contentEncoding();
        if (contentEncoding == null && GpkgFormat.isGzip(bytes)) {
            contentEncoding = "gzip";
        }
        return Optional.of(new BytesTile(tileMatrixSetId, tileMatrix, tileRow, tileCol, tileMediaType,
                contentEncoding, bytes));
    }

    @Override
    public void close() throws Exception {
        GpkgTiles.closeAll(connections);
    }

}
