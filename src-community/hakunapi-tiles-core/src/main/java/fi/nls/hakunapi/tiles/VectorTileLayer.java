package fi.nls.hakunapi.tiles;

import java.util.List;

public class VectorTileLayer {

    private final String id;
    private final String title;
    private final String description;
    private final VectorTileSource source;
    private final List<String> collectionIds;
    private final List<String> tileMatrixSetIds;
    private final List<String> mediaTypes;
    /** Optional WGS84 bounding box [minLon, minLat, maxLon, maxLat]; may be null. */
    private final double[] bbox;

    public VectorTileLayer(String id, String title, String description, VectorTileSource source,
            List<String> collectionIds, List<String> tileMatrixSetIds, List<String> mediaTypes, double[] bbox) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.source = source;
        this.collectionIds = List.copyOf(collectionIds);
        this.tileMatrixSetIds = List.copyOf(tileMatrixSetIds);
        this.mediaTypes = List.copyOf(mediaTypes);
        this.bbox = bbox;
    }

    public String getId() {
        return id;
    }

    public VectorTileSource getSource() {
        return source;
    }

    public List<String> getCollectionIds() {
        return collectionIds;
    }

    /** Whether this layer renders exactly the collection it is named after, so it belongs under /collections/{collectionId}/tiles. */
    public boolean isCollectionTileset() {
        return collectionIds.size() == 1 && id.equals(collectionIds.get(0));
    }

    /** Stored tiles cannot be narrowed to some of their layers without decoding and re-encoding. */
    public boolean isStoredTileset() {
        return false;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
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
