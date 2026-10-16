package fi.nls.hakunapi.tiles.source.gpkg;

import java.util.List;

import fi.nls.hakunapi.tiles.VectorTileSource;
import fi.nls.hakunapi.tiles.VectorTileLayer;

public class GpkgVectorTileLayer extends VectorTileLayer {

    private final GpkgTileConfig gpkgConfig;

    public GpkgVectorTileLayer(String id, String title, String description, VectorTileSource source,
            List<String> collectionIds, List<String> tileMatrixSetIds, List<String> mediaTypes,
            double[] bbox, GpkgTileConfig gpkgConfig) {
        super(id, title, description, source, collectionIds, tileMatrixSetIds, mediaTypes, bbox);
        this.gpkgConfig = gpkgConfig;
    }

    public GpkgTileConfig getGpkgConfig() {
        return gpkgConfig;
    }

    @Override
    public boolean isStoredTileset() {
        return true;
    }

}
