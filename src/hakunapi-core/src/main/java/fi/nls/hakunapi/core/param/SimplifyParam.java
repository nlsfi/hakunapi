package fi.nls.hakunapi.core.param;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import fi.nls.hakunapi.core.WFS3Service;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import fi.nls.hakunapi.core.request.GetTileFeaturesRequest;
import fi.nls.hakunapi.core.util.SimplifyAlgorithm;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.Parameter.StyleEnum;
import io.swagger.v3.oas.models.parameters.QueryParameter;

public class SimplifyParam implements GetFeatureParam {
    
    private static final SimplifyAlgorithm DEFAULT = SimplifyAlgorithm.VW; 

    @Override
    public String getParamName() {
        return "simplify";
    }

    @Override
    public Parameter toParameter(WFS3Service service) {
        List<String> e = Arrays.stream(SimplifyAlgorithm.values())
                .map(SimplifyAlgorithm::name)
                .collect(Collectors.toList());
        return new QueryParameter()
                .name(getParamName())
                .style(StyleEnum.FORM)
                .explode(false)
                .required(false)
                .description("Algorithm for Simplify")
                .schema(new StringSchema()
                        ._enum(e)
                        ._default(DEFAULT.name()));
    }

    @Override
    public void modify(WFS3Service service, GetFeatureRequest request, String value) throws IllegalArgumentException {
        if (!(request instanceof GetTileFeaturesRequest)) {
            return;
        }
        GetTileFeaturesRequest tileReq = (GetTileFeaturesRequest) request;
        if (value == null || value.isEmpty()) {
            tileReq.setAlgorithm(DEFAULT);
            return;
        }
        
        SimplifyAlgorithm a;
        try {
            a = SimplifyAlgorithm.valueOf(value);
        } catch (Exception e) {
            String options = Arrays.stream(SimplifyAlgorithm.values())
                    .map(SimplifyAlgorithm::name)
                    .collect(Collectors.joining(", ", "[", "]"));
            throw new IllegalArgumentException("'simplify' must be one of " + options);
        }
        request.addQueryParam(getParamName(), value);
        tileReq.setAlgorithm(a);
    }

}
