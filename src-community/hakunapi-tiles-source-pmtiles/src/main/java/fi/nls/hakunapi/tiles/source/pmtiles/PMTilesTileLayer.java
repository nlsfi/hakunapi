package fi.nls.hakunapi.tiles.source.pmtiles;

import java.nio.file.Path;
import java.util.List;

import fi.nls.hakunapi.tiles.AcceleratedTileLayer;
import fi.nls.hakunapi.tiles.MapTileLayer;
import fi.nls.hakunapi.tiles.MapTileSource;
import fi.nls.hakunapi.tiles.TileLocator;

public class PMTilesTileLayer extends MapTileLayer implements AcceleratedTileLayer {
    private final PMTilesConfig pmtilesConfig;

    public PMTilesTileLayer(String id, String title, String description, MapTileSource source,
            List<String> tileMatrixSetIds, List<String> mediaTypes, double[] bbox, PMTilesConfig pmtilesConfig) {
        super(id, title, description, source, tileMatrixSetIds, mediaTypes, bbox);
        this.pmtilesConfig = pmtilesConfig;
    }

    public PMTilesConfig getPMTilesConfig() {
        return pmtilesConfig;
    }

    @Override
    public TileLocator getTileLocator() {
        return pmtilesConfig.index();
    }

    @Override
    public String getInternalLocation() {
        return null;
    }

    @Override
    public Path getTileFile() {
        return pmtilesConfig.sendfileArchive();
    }
}
