package fi.nls.hakunapi.core.param;

import java.math.BigDecimal;

import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import fi.nls.hakunapi.core.request.GetTileFeaturesRequest;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.parameters.Parameter.StyleEnum;

public class ToleranceParam implements GetFeatureParam {

    private static final double MIN = 0;
    private static final double MAX = 4096;
    private static final double DEFAULT = 2;

    @Override
    public String getParamName() {
        return "tolerance";
    }

    @Override
    public Parameter toParameter(FeatureServiceConfig service) {
        return new QueryParameter()
                .name(getParamName())
                .style(StyleEnum.FORM)
                .explode(false)
                .required(false)
                .description("Tolerance for Simplify. In MVT virtual coordinates")
                .schema(new NumberSchema()
                        ._default(BigDecimal.valueOf(DEFAULT))
                        .minimum(BigDecimal.valueOf(MIN))
                        .maximum(BigDecimal.valueOf(MAX)));
    }

    @Override
    public void modify(FeatureServiceConfig service, GetFeatureRequest request, String value) throws IllegalArgumentException {
        if (!(request instanceof GetTileFeaturesRequest)) {
            return;
        }
        GetTileFeaturesRequest tileReq = (GetTileFeaturesRequest) request;

        if (value == null || value.isEmpty()) {
            tileReq.setTolerance(DEFAULT);
            return;
        }

        double v = Double.parseDouble(value);
        if (v < MIN || v > MAX) {
            throw new IllegalArgumentException("tolerance must be within " + MIN + " - " + MAX);
        }
        request.addQueryParam(getParamName(), value);
        tileReq.setTolerance(v);
    }

}
