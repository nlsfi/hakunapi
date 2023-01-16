package fi.nls.hakunapi.simple.servlet.operation.param;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.WFS3Service;
import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.param.GetFeatureParam;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.request.GetFeatureCollection;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.Parameter.StyleEnum;
import io.swagger.v3.oas.models.parameters.QueryParameter;

public class SimpleGetFeatureParam implements GetFeatureParam {

    protected final FeatureType ft;
    protected final HakunaProperty property;
    protected final FilterMapper filterMapper;

    public SimpleGetFeatureParam(FeatureType ft, HakunaProperty property, FilterMapper filterMapper) {
        this.ft = ft;
        this.property = property;
        this.filterMapper = filterMapper;
    }

    @Override
    public boolean isCommon() {
        return false;
    }

    @Override
    public String getParamName() {
        return property.getName();
    }
    
    public String getDescription() {
        return "Filter the collection by " + getParamName();
    }

    @Override
    public Parameter toParameter(WFS3Service service) {
        return new QueryParameter()
                .name(getParamName())
                .style(StyleEnum.FORM)
                .explode(false)
                .required(false)
                .description(getDescription())
                .schema(property.getSchema());
    }

    @Override
    public void modify(WFS3Service service, GetFeatureRequest request, String value)
            throws IllegalArgumentException {
        if (value == null || value.isEmpty()) {
            return;
        }

        Optional<GetFeatureCollection> optFc = request.getCollections().stream()
                .filter(c -> c.getFt().getName().equals(this.ft.getName()))
                .findAny();
        if (!optFc.isPresent()) {
            return;
        }
        GetFeatureCollection fc = optFc.get();

        Set<Filter> or = Arrays.stream(value.split(","))
                .map(v -> filterMapper.toSingleFilter(property, v))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (or.isEmpty()) {
            return;
        }

        if (property.getSchema().getEnum() != null) {
            int n = property.getSchema().getEnum().size();
            if (or.size() == n) {
                // Filter (a OR b) when every row is either a OR b is pointless
                request.addQueryParam(getParamName(), value);
                return;
            }
        }
        
        Filter f = Filter.or(new ArrayList<>(or));
        fc.addFilter(f);
        request.addQueryParam(getParamName(), value);
    }

}
