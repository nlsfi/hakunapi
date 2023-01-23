package fi.nls.hakunapi.core.param;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.FilterParser;
import fi.nls.hakunapi.core.WFS3Service;
import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.request.GetFeatureCollection;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.Parameter.StyleEnum;
import io.swagger.v3.oas.models.parameters.QueryParameter;

public class FilterParam implements GetFeatureParam {

    public static final String TAG = FilterParam.class.getName();

    private static final String DESCRIPTION = "To Filter the response";

    @Override
    public String getParamName() {
        return "filter";
    }

    @Override
    public Parameter toParameter(WFS3Service service) {
        return new QueryParameter()
                .name(getParamName())
                .style(StyleEnum.FORM)
                .explode(false)
                .required(false)
                .description(DESCRIPTION)
                .schema(new StringSchema());
    }

    @Override
    public void modify(WFS3Service service, GetFeatureRequest request, String value) throws IllegalArgumentException {
        if (value == null || value.isEmpty()) {
            return;
        }

        String filterLang = request.getQueryParam(FilterLangParam.PARAM_NAME);
        FilterParser parser;
        if (filterLang != null) {
            parser = service.getFilterParser(filterLang);
        } else {
            parser = service.getFilterParsers().iterator().next();
        }
        
        if (parser == null) {
            String err = String.format("Missing valid value for %s",
                    FilterLangParam.PARAM_NAME);
            throw new IllegalArgumentException(err);
        }

        for (GetFeatureCollection c : request.getCollections()) {
            FeatureType ft = c.getFt();
            Filter filter = parser.parse(ft, value, request.getFilterSrid());
            if (filter != null) {
                filter.setTag(TAG);
                c.addFilter(filter);
            }
        }
        request.addQueryParam(getParamName(), value);
    }

    @Override
    public int priority() {
        return 10; // Don't run before FilterLangParam & FilterCrsParam!
    }

}
