package fi.nls.hakunapi.tiles.zoom;

import fi.nls.hakunapi.tiles.VectorTileLayer;

/** layerName is the Layer.name to extract from the backing tiles, or null to relay them whole. */
public record ZoomBacking(
        String id,
        int minZoom,
        int maxZoom,
        String layerName,
        VectorTileLayer layer) {

    /** A tile matrix id that is not an integer matches nothing. */
    public boolean covers(String tileMatrix) {
        try {
            int zoom = Integer.parseInt(tileMatrix);
            return zoom >= minZoom && zoom <= maxZoom;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
