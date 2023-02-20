package fi.nls.hakunapi.core.tiles;

import java.util.List;
import java.util.stream.Collectors;

import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.param.APIParam;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.PathParameter;

public class TilingSchemeIdParam implements APIParam {
    
    public static String PARAM_NAME = "tilingSchemeId";

    @Override
    public String getParamName() {
        return PARAM_NAME;
    }

    @Override
    public Parameter toParameter(FeatureServiceConfig service) {
        List<String> tilingSchemeIdentifiers = service.getTilingSchemes()
                .stream()
                .map(scheme -> scheme.getIdentifier())
                .collect(Collectors.toList());
        return new PathParameter()
                .name(getParamName())
                .description("Local identifier of a specific tiling scheme. A list of all available tilingSchemeIds can be found under the /tiles path or below.")
                .schema(new StringSchema()._enum(tilingSchemeIdentifiers));
    }

}
