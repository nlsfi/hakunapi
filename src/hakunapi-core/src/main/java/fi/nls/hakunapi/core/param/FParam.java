package fi.nls.hakunapi.core.param;

import java.util.List;
import java.util.stream.Collectors;

import fi.nls.hakunapi.core.OutputFormat;
import fi.nls.hakunapi.core.WFS3Service;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.Parameter.StyleEnum;
import io.swagger.v3.oas.models.parameters.QueryParameter;

public class FParam implements GetFeatureParam {

    public static final String QUERY_PARAM_NAME = "f"; 
    
    private static final String DESCRIPTION = "The format of the response. If no value is provided, the standard http rules apply, i.e., the accept header shall be used to determine the format.";

    @Override
    public String getParamName() {
        return QUERY_PARAM_NAME;
    }

    @Override
    public Parameter toParameter(WFS3Service service) {
        List<String> values = service.getOutputFormats().stream()
                .map(it -> it.getId())
                .collect(Collectors.toList());
        return new QueryParameter()
                .name(getParamName())
                .style(StyleEnum.FORM)
                .explode(false)
                .required(false)
                .description(DESCRIPTION)
                .schema(new StringSchema()._enum(values));
    }

    @Override
    public void modify(WFS3Service service, GetFeatureRequest request, String value) throws IllegalArgumentException {
        if (value == null || value.isEmpty()) {
            return;
        }
        OutputFormat o = service.getOutputFormat(value);
        if (o == null) {
            throw new IllegalArgumentException("Unknown value");
        }
        request.setFormat(o);
        request.addQueryParam(getParamName(), value);
    }

}
