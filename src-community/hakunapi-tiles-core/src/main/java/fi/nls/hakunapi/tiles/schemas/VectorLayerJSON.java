package fi.nls.hakunapi.tiles.schemas;

import java.util.Map;

/** An entry of TileJSON vector_layers; id is the layer name inside the encoded tile. */
public class VectorLayerJSON {

    private final String id;
    private final String description;
    private final Map<String, String> fields;

    public VectorLayerJSON(String id, String description, Map<String, String> fields) {
        this.id = id;
        this.description = description;
        this.fields = fields;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, String> getFields() {
        return fields;
    }

}
