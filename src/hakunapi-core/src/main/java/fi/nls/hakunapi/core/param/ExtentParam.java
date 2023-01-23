package fi.nls.hakunapi.core.param;

import java.math.BigDecimal;

import fi.nls.hakunapi.core.WFS3Service;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import fi.nls.hakunapi.core.request.GetTileFeaturesRequest;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.parameters.Parameter.StyleEnum;

public class ExtentParam implements GetFeatureParam {
    
    private static final int MIN = 256;
    private static final int MAX = 4096;
    private static final int DEFAULT = 4096;

    @Override
    public String getParamName() {
        return "extent";
    }

    @Override
    public Parameter toParameter(WFS3Service service) {
        return new QueryParameter()
                .name(getParamName())
                .style(StyleEnum.FORM)
                .explode(false)
                .required(false)
                .description("Extent of vector tile")
                .schema(new IntegerSchema()
                        ._default(DEFAULT)
                        .minimum(BigDecimal.valueOf(MIN))
                        .maximum(BigDecimal.valueOf(MAX)));
    }
    
    @Override
    public void modify(WFS3Service service, GetFeatureRequest request, String value) throws IllegalArgumentException {
        if (!(request instanceof GetTileFeaturesRequest)) {
            return;
        }
        GetTileFeaturesRequest tileReq = (GetTileFeaturesRequest) request;

        if (value == null || value.isEmpty()) {
            tileReq.setExtent(DEFAULT);
            return;
        }

        int v = Integer.parseInt(value);
        if (v < MIN || v > MAX) {
            throw new IllegalArgumentException("extent must be within " + MIN + " - " + MAX);
        }
        request.addQueryParam(getParamName(), value);
        tileReq.setExtent(v);
    }

}
