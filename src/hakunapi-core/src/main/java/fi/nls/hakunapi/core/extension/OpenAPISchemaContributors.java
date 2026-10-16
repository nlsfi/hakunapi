package fi.nls.hakunapi.core.extension;

import java.util.ServiceLoader;

import io.swagger.v3.oas.models.media.Schema;

public class OpenAPISchemaContributors {

    private static final ServiceLoader<OpenAPISchemaContributor> LOADER =
            ServiceLoader.load(OpenAPISchemaContributor.class);

    private OpenAPISchemaContributors() {}

    public static Schema<?> getSchema(Class<?> clazz) {
        synchronized (LOADER) {
            for (OpenAPISchemaContributor contributor : LOADER) {
                Schema<?> schema = contributor.getSchema(clazz);
                if (schema != null) {
                    return schema;
                }
            }
        }
        return null;
    }

}
