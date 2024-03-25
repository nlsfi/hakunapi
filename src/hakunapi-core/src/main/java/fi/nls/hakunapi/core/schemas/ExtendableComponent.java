package fi.nls.hakunapi.core.schemas;

import java.util.Map;

import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

public abstract class ExtendableComponent implements Component {

    private Map<String, Object> extensions;

    public Map<String, Object> getExtensions() {
        return extensions;
    }
    
    public void setExtensions(Map<String, Object> extensions) {
        this.extensions = extensions;
    }
    
    protected void addExtensionsToSchema(Schema schema) {
        Schema extensionsSchema = new ObjectSchema();
        extensionsSchema.description("Free form object containing additional metadata");
        schema.addProperty("extensions", extensionsSchema);
    }
    
}
