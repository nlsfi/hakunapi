package fi.nls.hakunapi.tiles.source.pmtiles;

import java.nio.file.Path;
import java.util.List;

import fi.nls.hakunapi.tiles.AcceleratedTileLayer;
import fi.nls.hakunapi.tiles.TileLocator;
import fi.nls.hakunapi.tiles.VectorTileLayer;
import fi.nls.hakunapi.tiles.VectorTileSource;

public class PMTilesVectorTileLayer extends VectorTileLayer implements AcceleratedTileLayer {
    private final PMTilesConfig pmtilesConfig;

    public PMTilesVectorTileLayer(String id, String title, String description, VectorTileSource source,
            List<String> collectionIds, List<String> tileMatrixSetIds, List<String> mediaTypes,
            double[] bbox, PMTilesConfig pmtilesConfig) {
        super(id, title, description, source, collectionIds, tileMatrixSetIds, mediaTypes, bbox);
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

    @Override
    public boolean isStoredTileset() {
        return true;
    }
}
