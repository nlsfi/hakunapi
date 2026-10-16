package fi.nls.hakunapi.tiles.zoom;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
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
import fi.nls.hakunapi.tiles.config.TilesConfigParser;

public class StubVectorTileSource implements VectorTileSource {

    public static final String TYPE = "stub";

    private final Map<String, String> bodyByLayer = new HashMap<>();

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public VectorTileLayer parse(HakunaConfigParser cfg, Path path, TileMatrixSetRegistry tms, String layerId) {
        String p = "tiles.layers." + layerId + ".";
        boolean absent = Boolean.parseBoolean(cfg.get(p + "stub.absent", "false"));
        if (!absent) {
            bodyByLayer.put(layerId, cfg.get(p + "stub.body", layerId));
        }
        List<String> tmsIds = new TilesConfigParser(cfg).readTileMatrixSetIds(layerId);
        String mediaType = cfg.get(p + "format", "application/vnd.mapbox-vector-tile");
        return new VectorTileLayer(layerId, layerId, layerId, this, List.of(layerId), tmsIds,
                List.of(mediaType), null);
    }

    @Override
    public Optional<Tile> getTile(VectorTileLayer layer, String tileMatrixSetId, String tileMatrix,
            long tileRow, long tileCol, List<FeatureType> collections,
            String mediaType, Map<String, String> requestParams) {
        String body = bodyByLayer.get(layer.getId());
        if (body == null) {
            return Optional.empty();
        }
        return Optional.of(new BytesTile(tileMatrixSetId, tileMatrix, tileRow, tileCol,
                layer.getMediaTypes().get(0), body.getBytes(StandardCharsets.UTF_8)));
    }

}
