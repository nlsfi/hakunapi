package fi.nls.hakunapi.tiles;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Kept apart from FeatureServiceConfig so hakunapi-core has no dependency on the tiles module. */
public class TilesServiceConfig {

    private final Map<String, VectorTileLayer> vectorLayers;
    private final Map<String, MapTileLayer> mapLayers;
    private final TileMatrixSetRegistry tileMatrixSets;

    public TilesServiceConfig(List<VectorTileLayer> vectorLayers, List<MapTileLayer> mapLayers,
            TileMatrixSetRegistry tileMatrixSets) {
        Map<String, VectorTileLayer> vmap = new LinkedHashMap<>();
        for (VectorTileLayer layer : vectorLayers) {
            vmap.put(layer.getId(), layer);
        }
        Map<String, MapTileLayer> mmap = new LinkedHashMap<>();
        for (MapTileLayer layer : mapLayers) {
            mmap.put(layer.getId(), layer);
        }
        this.vectorLayers = vmap;
        this.mapLayers = mmap;
        this.tileMatrixSets = tileMatrixSets;
    }

    public Collection<VectorTileLayer> getVectorLayers() {
        return vectorLayers.values();
    }

    public Optional<VectorTileLayer> getVectorLayer(String id) {
        return Optional.ofNullable(vectorLayers.get(id));
    }

    public boolean hasVectorLayers() {
        return !vectorLayers.isEmpty();
    }

    public Collection<MapTileLayer> getMapLayers() {
        return mapLayers.values();
    }

    public Optional<MapTileLayer> getMapLayer(String id) {
        return Optional.ofNullable(mapLayers.get(id));
    }

    public boolean hasMapLayers() {
        return !mapLayers.isEmpty();
    }

    public boolean hasTileLayers() {
        return !vectorLayers.isEmpty() || !mapLayers.isEmpty();
    }

    public TileMatrixSetRegistry getTileMatrixSets() {
        return tileMatrixSets;
    }

}
