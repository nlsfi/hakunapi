package fi.nls.hakunapi.core.param;

import java.util.List;
import java.util.stream.Collectors;

import fi.nls.hakunapi.core.FilterParser;
import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.Parameter.StyleEnum;
import io.swagger.v3.oas.models.parameters.QueryParameter;

public class FilterLangParam implements GetFeatureParam {

    static final String PARAM_NAME = "filter-lang";

    private static final String DESCRIPTION = "Specify the filter language";

    @Override
    public String getParamName() {
        return PARAM_NAME;
    }

    @Override
    public Parameter toParameter(FeatureServiceConfig service) {
        List<String> valid = service.getFilterParsers().stream()
                .map(FilterParser::getCode)
                .collect(Collectors.toList());
        String _default = service.getFilterParsers().iterator().next().getCode();
        return new QueryParameter()
                .name(getParamName())
                .style(StyleEnum.FORM)
                .explode(false)
                .required(false)
                .description(DESCRIPTION)
                .schema(new StringSchema()._enum(valid)._default(_default));
    }

    @Override
    public void modify(FeatureServiceConfig service, GetFeatureRequest request, String value) throws IllegalArgumentException {
        if (value == null || value.isEmpty()) {
            return;
        }

        FilterParser parser = service.getFilterParser(value);
        if (parser == null) {
            String err = String.format("Invalid value for %s, allowed values are %s",
                    getParamName(),
                    service.getFilterParsers().stream()
                    .map(FilterParser::getCode)
                    .collect(Collectors.joining(",", "[", "]")));
            throw new IllegalArgumentException(err);
        }

        request.addQueryParam(getParamName(), value);
    }

    @Override
    public int priority() {
        return -1; // Run before FilterParam!
    }

}
