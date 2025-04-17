package fi.nls.hakunapi.core.param;

import java.math.BigDecimal;

import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.Parameter.StyleEnum;
import io.swagger.v3.oas.models.parameters.QueryParameter;

public class OffsetParam implements GetFeatureParam {

    public static final String PARAM_NAME = "offset";

    private static final String DESCRIPTION = "The 'offset' parameter is used for offset based pagination.";

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
                .schema(new IntegerSchema()
                        .minimum(BigDecimal.valueOf(0))
                        .maximum(BigDecimal.valueOf(Integer.MAX_VALUE)));
    }

    @Override
    public void modify(FeatureServiceConfig service, GetFeatureRequest request, String value)
            throws IllegalArgumentException {
        if (request.getQueryParam(NextParam.PARAM_NAME) != null) {
            // Ignore offset parameter if next was already used
            return;
        }

        if (value == null || value.isEmpty()) {
            return;
        }

        int offset;
        try {
            offset = Integer.parseUnsignedInt(value);
        } catch (Exception ignore) {
            throw new IllegalArgumentException("Invalid value for 'offset' - expected non-negative integer value");
        }

        request.getCollections().get(0).getFt().getPaginationStrategy().apply(request, offset);
        request.addQueryParam(getParamName(), value);
    }

    @Override
    public int priority() {
        return 1; // Run after next param
    }

}
