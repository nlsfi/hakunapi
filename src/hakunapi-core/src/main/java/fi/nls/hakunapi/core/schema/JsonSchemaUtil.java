package fi.nls.hakunapi.core.schema;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;

/**
 * Utilities for loading and working with JSON Schema documents
 * used to enrich feature type and property metadata.
 *
 * Expected schema structure:
 * <pre>
 * {
 *   "properties": {
 *     "collection_name": {
 *       "title": "...",
 *       "description": "...",
 *       "properties": {
 *         "property_name": {
 *           "title": "...",
 *           "description": "...",
 *           "enum": [...],
 *           "x-codelist-uri": "...",
 *           "x-geometry-type": "MULTIPOLYGON"
 *         }
 *       }
 *     }
 *   }
 * }
 * </pre>
 */
public class JsonSchemaUtil {

    private static final Logger LOG = LoggerFactory.getLogger(JsonSchemaUtil.class);
    private static final ObjectMapper MAPPER = new JsonMapper();

    private JsonSchemaUtil() {
    }

    @SuppressWarnings("rawtypes")
    public static Optional<Schema> loadFromFile(Path schemaPath) {
        if (schemaPath == null) {
            return Optional.empty();
        }
        try {
            if (!Files.exists(schemaPath)) {
                LOG.debug("No schema file found at {}", schemaPath);
                return Optional.empty();
            }
            String json = Files.readString(schemaPath);
            JsonNode root = MAPPER.readTree(json);
            Schema schema = nodeToSchema(root);
            LOG.info("Loaded JSON Schema from {}", schemaPath);
            return Optional.of(schema);
        } catch (IOException e) {
            LOG.warn("Failed to load schema from {}: {}", schemaPath, e.getMessage());
            return Optional.empty();
        }
    }

    @SuppressWarnings("rawtypes")
    public static Schema parse(String json) throws IOException {
        JsonNode root = MAPPER.readTree(json);
        return nodeToSchema(root);
    }

    public static Schema<?> getCollectionSchema(Schema<?> rootSchema, String collectionId) {
        if (rootSchema == null || rootSchema.getProperties() == null) {
            return null;
        }
        Map<String, Schema> properties = rootSchema.getProperties();
        return properties.get(collectionId);
    }

    public static Schema<?> getPropertySchema(Schema<?> collectionSchema, String propertyName) {
        if (collectionSchema == null || collectionSchema.getProperties() == null) {
            return null;
        }
        Map<String, Schema> properties = collectionSchema.getProperties();
        return properties.get(propertyName);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    static Schema nodeToSchema(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }

        String type = node.has("type") ? node.get("type").asString() : "object";
        Schema schema;
        switch (type) {
            case "string":
                schema = new StringSchema();
                break;
            case "integer":
                schema = new IntegerSchema();
                break;
            case "number":
                schema = new NumberSchema();
                break;
            case "boolean":
                schema = new BooleanSchema();
                break;
            default:
                schema = new ObjectSchema();
                break;
        }

        if (node.has("title")) {
            schema.setTitle(node.get("title").asString());
        }
        if (node.has("description")) {
            schema.setDescription(node.get("description").asString());
        }
        if (node.has("format")) {
            schema.setFormat(node.get("format").asString());
        }
        if (node.has("enum")) {
            List<Object> enumValues = new ArrayList<>();
            for (JsonNode val : node.get("enum")) {
                if (val.isInt()) {
                    enumValues.add(val.intValue());
                } else if (val.isLong()) {
                    enumValues.add(val.longValue());
                } else if (val.isDouble()) {
                    enumValues.add(val.doubleValue());
                } else {
                    enumValues.add(val.asString());
                }
            }
            schema.setEnum(enumValues);
        }

        if (node.has("properties")) {
            Map<String, Schema> props = new LinkedHashMap<>();
            for (Map.Entry<String, JsonNode> field : node.get("properties").properties()) {
                Schema propSchema = nodeToSchema(field.getValue());
                if (propSchema != null) {
                    props.put(field.getKey(), propSchema);
                }
            }
            schema.setProperties(props);
        }

        // Collect x- extension fields
        for (Map.Entry<String, JsonNode> field : node.properties()) {
            if (field.getKey().startsWith("x-")) {
                schema.addExtension(field.getKey(), field.getValue().isString()
                        ? field.getValue().asString()
                        : field.getValue());
            }
        }

        return schema;
    }

}