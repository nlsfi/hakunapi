package fi.nls.hakunapi.tiles.servlet.jakarta;

import fi.nls.hakunapi.core.extension.OpenAPISchemaContributor;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

public class TilesOpenAPISchemaContributor implements OpenAPISchemaContributor {
    private static final String SCHEMAS_PACKAGE = "fi.nls.hakunapi.tiles.schemas.";

    @Override
    public Schema<?> getSchema(Class<?> clazz) {
        if (clazz.getName().startsWith(SCHEMAS_PACKAGE)) {
            return new ObjectSchema();
        }
        return null;
    }
}
