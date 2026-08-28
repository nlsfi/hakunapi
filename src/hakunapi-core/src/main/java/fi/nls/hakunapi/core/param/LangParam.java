package fi.nls.hakunapi.core.param;

import java.util.List;

import fi.nls.hakunapi.core.FeatureServiceConfig;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;

/**
 * Documents the lang query parameter for the OpenAPI document.
 *
 * Without this the generator still picks up @QueryParam("lang") reflectively,
 * but produces a bare string parameter with no description and no enumeration
 * of the languages the service actually serves.
 *
 * The parameter is common: it appears on the landing page, /collections,
 * /collections/{collectionId} and /collections/{collectionId}/schema.
 */
public class LangParam implements APIParam {

    public static final String PARAM_NAME = "lang";

    @Override
    public String getParamName() {
        return PARAM_NAME;
    }

    @Override
    public Parameter toParameter(FeatureServiceConfig service) {
        StringSchema schema = new StringSchema();

        List<String> languages = service.getLanguages();
        if (!languages.isEmpty()) {
            languages.forEach(schema::addEnumItemObject);
            schema.setDefault(languages.get(0));
        }

        return new QueryParameter()
                .name(PARAM_NAME)
                .description("Language to use for the descriptive elements of this resource. "
                        + "Overrides the Accept-Language header. An unsupported value falls back "
                        + "to the default language.")
                .required(false)
                .schema(schema);
    }

}
