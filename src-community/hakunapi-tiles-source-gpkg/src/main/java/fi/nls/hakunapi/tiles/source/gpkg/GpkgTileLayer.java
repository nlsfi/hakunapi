package fi.nls.hakunapi.tiles.source.gpkg;

import java.util.List;

import fi.nls.hakunapi.tiles.MapTileLayer;
import fi.nls.hakunapi.tiles.MapTileSource;

public class GpkgTileLayer extends MapTileLayer {

    private final GpkgTileConfig gpkgConfig;

    public GpkgTileLayer(String id, String title, String description, MapTileSource source,
            List<String> tileMatrixSetIds, List<String> mediaTypes, double[] bbox, GpkgTileConfig gpkgConfig) {
        super(id, title, description, source, tileMatrixSetIds, mediaTypes, bbox);
        this.gpkgConfig = gpkgConfig;
    }

    public GpkgTileConfig getGpkgConfig() {
        return gpkgConfig;
    }

}
