package fi.nls.hakunapi.core.param;

import fi.nls.hakunapi.core.NextCursor;
import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.Parameter.StyleEnum;
import io.swagger.v3.oas.models.parameters.QueryParameter;

public class NextParam implements GetFeatureParam {

    public static final String PARAM_NAME = "next";

    private static final String DESCRIPTION = "The 'next' parameter is used for pagination.";

    @Override
    public String getParamName() {
        return PARAM_NAME;
    }

    @Override
    public Parameter toParameter(FeatureServiceConfig service) {
        return new QueryParameter()
                .name(getParamName())
                .style(StyleEnum.FORM)
                .explode(false)
                .required(false)
                .description(DESCRIPTION)
                .schema(new StringSchema());
    }

    @Override
    public void modify(FeatureServiceConfig service, GetFeatureRequest request, String value)
            throws IllegalArgumentException {
        if (value == null || value.isEmpty()) {
            return;
        }
        request.addQueryParam(getParamName(), value);

        NextCursor cursor = new NextCursor(value);
        request.getCollections().get(0).getFt().getPaginationStrategy().apply(request, cursor);
    }

}
