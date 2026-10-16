package fi.nls.hakunapi.tiles;

import java.util.List;

public class MapTileLayer {

    private final String id;
    private final String title;
    private final String description;
    private final MapTileSource source;
    private final List<String> tileMatrixSetIds;
    private final List<String> mediaTypes;
    /** Optional WGS84 bounding box [minLon, minLat, maxLon, maxLat]; may be null. */
    private final double[] bbox;

    public MapTileLayer(String id, String title, String description, MapTileSource source,
            List<String> tileMatrixSetIds, List<String> mediaTypes, double[] bbox) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.source = source;
        this.tileMatrixSetIds = List.copyOf(tileMatrixSetIds);
        this.mediaTypes = List.copyOf(mediaTypes);
        this.bbox = bbox;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public MapTileSource getSource() {
        return source;
    }

    public List<String> getTileMatrixSetIds() {
        return tileMatrixSetIds;
    }

    public boolean supportsTileMatrixSet(String tileMatrixSetId) {
        return tileMatrixSetIds.contains(tileMatrixSetId);
    }

    public List<String> getMediaTypes() {
        return mediaTypes;
    }

    public double[] getBbox() {
        return bbox;
    }

}
