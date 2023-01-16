package fi.nls.hakunapi.core.schemas;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({ "$schema", "$id", "title", "description", "type", "properties" })
public class Queryables implements Component {

    private final String schema = "https://json-schema.org/draft/2019-09/schema";
    private final String id;
    private final String title;
    private final String description;
    private final String type = "object";
    private final Map<String, Schema<?>> properties;

    // For HTML
    private final String collectionId;
    private final Map<String, String> props;

    @JsonProperty("$schema")
    public String getSchema() {
        return schema;
    }

    @JsonProperty("$id")
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public Map<String, Schema<?>> getProperties() {
        return properties;
    }

    @JsonIgnore
    public String getCollectionId() {
        return collectionId;
    }

    @JsonIgnore
    public Map<String, String> getProps() {
        return props;
    }

    public Queryables() {
        this(null, null, null, null);
    }

    public Queryables(String collectionId, String id, String title, String description) {
        this.collectionId = collectionId;
        this.id = id;
        this.title = title;
        this.description = description;
        this.properties = new LinkedHashMap<>();
        this.props = new LinkedHashMap<>();
    }

    @JsonIgnore
    public void addProperty(String name, Schema<?> schema) {
        Schema s = OAS30toJsonSchema.toJsonSchema(schema);
        properties.put(name, s);
        props.put(name, s.get$ref() != null ? s.get$ref() : s.getType());
    }

    @Override
    @JsonIgnore
    public String getComponentName() {
        return "queryables";
    }

    @Override
    @JsonIgnore
    public Schema toSchema(Map<String, Schema> components) {
        return new ObjectSchema();
    }

}
