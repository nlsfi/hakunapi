package fi.nls.hakunapi.core.param;

import java.math.BigDecimal;

import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import fi.nls.hakunapi.core.request.GetTileFeaturesRequest;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.parameters.Parameter.StyleEnum;

public class BufferParam implements GetFeatureParam {
    
    private static final int MIN = 0;
    private static final int MAX = 4096;
    private static final int DEFAULT = 0;
    
    @Override
    public String getParamName() {
        return "buffer";
    }

    @Override
    public Parameter toParameter(FeatureServiceConfig service) {
        return new QueryParameter()
                .name(getParamName())
                .style(StyleEnum.FORM)
                .explode(false)
                .required(false)
                .description("Buffer of vector tile")
                .schema(new IntegerSchema()
                        ._default(DEFAULT)
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
            tileReq.setBuffer(DEFAULT);
            return;
        }

        int v = Integer.parseInt(value);
        if (v < MIN || v > MAX) {
            throw new IllegalArgumentException("buffer must be within " + MIN + " - " + MAX);
        }
        request.addQueryParam(getParamName(), value);
        tileReq.setBuffer(v);
    }

}
