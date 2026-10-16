package fi.nls.hakunapi.tiles.source.pmtiles;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.config.HakunaConfigParser;
import fi.nls.hakunapi.tiles.BytesTile;
import fi.nls.hakunapi.tiles.Tile;
import fi.nls.hakunapi.tiles.TileMatrixSetRegistry;
import fi.nls.hakunapi.tiles.VectorTileLayer;
import fi.nls.hakunapi.tiles.VectorTileSource;

public class PMTilesVectorTileSource implements VectorTileSource {
    public static final String TYPE = "pmtiles-vector";

    private final List<PMTilesArchive> archives = new ArrayList<>();

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public VectorTileLayer parse(HakunaConfigParser cfg, Path path, TileMatrixSetRegistry tms, String layerId)
            throws Exception {
        PMTilesTileSource.Parsed parsed = PMTilesTileSource.parse(cfg, path, layerId, archives, true);
        List<String> collectionIds = List.of(
                cfg.getMultiple("tiles.layers." + layerId + ".collections", new String[] { layerId }));
        return new PMTilesVectorTileLayer(layerId, parsed.title(), parsed.description(), this,
                collectionIds, parsed.tmsIds(), List.of(parsed.config().mediaType()), null,
                parsed.config());
    }

    @Override
    public boolean needsCollections(VectorTileLayer layer) {
        return false;
    }

    @Override
    public Optional<Tile> getTile(VectorTileLayer layer, String tileMatrixSetId, String tileMatrix,
            long tileRow, long tileCol, List<FeatureType> collections, String mediaType,
            Map<String, String> requestParams) throws Exception {
        PMTilesConfig cfg = ((PMTilesVectorTileLayer) layer).getPMTilesConfig();
        return PMTilesTileSource.readTile(cfg, tileMatrixSetId, tileMatrix, tileRow, tileCol);
    }

    @Override
    public void close() throws Exception {
        PMTilesTileSource.closeAll(archives);
    }
}
