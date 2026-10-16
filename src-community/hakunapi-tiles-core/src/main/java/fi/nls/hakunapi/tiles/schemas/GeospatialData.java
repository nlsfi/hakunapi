package fi.nls.hakunapi.tiles.schemas;

/** The id is the collection id and the layer name inside the encoded tile (a style source-layer). */
public class GeospatialData {

    private final String id;
    private final String title;
    private final String dataType;

    public GeospatialData(String id, String title, String dataType) {
        this.id = id;
        this.title = title;
        this.dataType = dataType;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDataType() {
        return dataType;
    }

}
