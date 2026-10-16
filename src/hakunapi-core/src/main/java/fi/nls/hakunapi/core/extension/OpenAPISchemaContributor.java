package fi.nls.hakunapi.core.extension;

import io.swagger.v3.oas.models.media.Schema;

public interface OpenAPISchemaContributor {

    public Schema<?> getSchema(Class<?> clazz);

}
