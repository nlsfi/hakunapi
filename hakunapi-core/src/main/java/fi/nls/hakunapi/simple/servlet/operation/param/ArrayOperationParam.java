package fi.nls.hakunapi.simple.servlet.operation.param;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import fi.nls.hakunapi.core.WFS3Service;
import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.filter.FilterOp;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyArray;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.Parameter.StyleEnum;
import io.swagger.v3.oas.models.parameters.QueryParameter;

public class ArrayOperationParam implements CustomizableGetFeatureParam {
    
    private static final EnumSet<FilterOp> ALLOWED_OPERATIONS = EnumSet.of(FilterOp.ARRAY_OVERLAPS);
    private static final String DEFAULT_FILTER_OP = FilterOp.ARRAY_OVERLAPS.name(); 
    
    private HakunaPropertyArray prop;
    private String name;
    private String desc;
    private FilterOp operation;
    
    @Override
    public void init(HakunaProperty prop, String name, String desc, String arg) {
        if (!(prop instanceof HakunaPropertyArray)) {
            throw new IllegalArgumentException("ArrayOperationParam only allowed for array properties!");
        }
        this.prop = (HakunaPropertyArray) prop;
        this.name = name;
        this.desc = desc;
        if (arg == null || arg.isEmpty()) {
            arg = DEFAULT_FILTER_OP;
        }
        this.operation = FilterOp.valueOf(arg);
        if (!ALLOWED_OPERATIONS.contains(this.operation)) {
            throw new IllegalArgumentException("Invalid arg for ArrayOperationParam! Allowed args are: "
                    + Arrays.toString(ALLOWED_OPERATIONS.toArray()));
        }
    }
    
    @Override
    public String getParamName() {
        return name;
    }

    @Override
    public void modify(WFS3Service service, GetFeatureRequest request, String value) throws IllegalArgumentException {
        if (value == null || value.isEmpty()) {
            return;
        }

        request.getCollections().stream()
        .filter(c -> c.getFt().getName().equals(prop.getFeatureType().getName()))
        .findAny()
        .ifPresent(c -> {
            c.addFilter(toFilter(value));
            request.addQueryParam(name, value);
        });
    }
    
    private Filter toFilter(String value) {
        List<Object> arr = Arrays.stream(value.split(",")).map(prop::toInner).collect(Collectors.toList());
        switch (operation) {
        case ARRAY_OVERLAPS:
            return Filter.arrayOverlaps(prop, arr);
        default:
            // Should never happen
            throw new RuntimeException("Configuration error");
        }
        
    }

    @Override
    public Parameter toParameter(WFS3Service service) {
        return new QueryParameter()
                .name(getParamName())
                .style(StyleEnum.FORM)
                .explode(false)
                .required(false)
                .description(desc)
                .schema(prop.getSchema());
    }

}
