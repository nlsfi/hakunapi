package fi.nls.hakunapi.core.param;

import java.math.BigDecimal;

import fi.nls.hakunapi.core.WFS3Service;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.Parameter.StyleEnum;
import io.swagger.v3.oas.models.parameters.QueryParameter;

public class LimitParam implements GetFeatureParam {

    public static final int UNLIMITED = -1;
    public static final int MINIMUM = 1;

    private static final String DESCRIPTION = "The optional limit parameter limits the number of items that are\n" +
            "presented in the response document.\n" +
            "Only items are counted that are on the first level of the collection in\n" +
            "the response document. Nested objects contained within the explicitly\n" +
            "requested items shall not be counted.\n\n" +
            "Valid values are positive integers. Invalid values are replaced with the default value.\n" +
            "As a special case, -1 is allowed as 'unlimited'.";

    @Override
    public String getParamName() {
        return "limit";
    }

    @Override
    public Parameter toParameter(WFS3Service service) {
        return new QueryParameter()
                .name(getParamName())
                .style(StyleEnum.FORM)
                .explode(false)
                .required(false)
                .description(DESCRIPTION)
                .schema(new IntegerSchema()
                        ._default(service.getLimitDefault())
                        .minimum(BigDecimal.valueOf(MINIMUM))
                        .maximum(BigDecimal.valueOf(service.getLimitMaximum())));
    }

    @Override
    public void modify(WFS3Service service, GetFeatureRequest request, String value) throws IllegalArgumentException {
        if (value == null || value.isEmpty()) {
            request.setLimit(service.getLimitDefault());
            return;
        }

        int limit;
        try {
            limit = Integer.parseInt(value);
        } catch (Exception ignore) {
            throw new IllegalArgumentException("Invalid value for 'limit' - expected integer value between 1 and " + service.getLimitMaximum());
        }
        if (limit != UNLIMITED) {
            limit = Math.max(MINIMUM, Math.min(limit, service.getLimitMaximum()));
        }
        request.setLimit(limit);
        request.addQueryParam(getParamName(), "" + limit);
    }

}
