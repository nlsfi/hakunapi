package fi.nls.hakunapi.core.schema;

import java.util.Map;

import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyWrapper;
import io.swagger.v3.oas.models.media.Schema;

/**
 * Wrapper that enhances a HakunaProperty with metadata from JSON Schema.
 * Applies title, description, enum values, and custom extensions from the schema.
 */
public class HakunaPropertyWithMetadata extends HakunaPropertyWrapper {

    @SuppressWarnings("rawtypes")
    private final Schema propertySchema;

    @SuppressWarnings("rawtypes")
    public HakunaPropertyWithMetadata(HakunaProperty wrapped, Schema propertySchema) {
        super(wrapped);
        this.propertySchema = propertySchema;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Schema<?> getSchema() {
        Schema<?> schema = wrapped.getSchema();

        if (propertySchema.getTitle() != null) {
            schema.setTitle(propertySchema.getTitle());
        }

        if (propertySchema.getDescription() != null) {
            schema.setDescription(propertySchema.getDescription());
        }

        if (propertySchema.getEnum() != null) {
            schema.setEnum(propertySchema.getEnum());
        }

        if (propertySchema.getExtensions() != null) {
            Map<String, Object> extensions = propertySchema.getExtensions();
            for (String key : extensions.keySet()) {
                schema.addExtension(key, extensions.get(key));
            }
        }

        return schema;
    }

    @Override
    public Object toInner(String value) {
        return wrapped.toInner(value);
    }

}
