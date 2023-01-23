package fi.nls.hakunapi.core.filter;

import java.util.ArrayList;
import java.util.List;

import fi.nls.hakunapi.core.property.HakunaProperty;

public class FilterHelper {

    public static List<HakunaProperty> getAllProperties(List<Filter> filters) {
        List<HakunaProperty> properties = new ArrayList<>();
        for (Filter filter : filters) {
            addAllProperties(filter, properties);
        }
        return properties;
    }

    @SuppressWarnings("unchecked")
    private static void addAllProperties(Filter filter, List<HakunaProperty> properties) {
        FilterOp op = filter.getOp();
        if (op == FilterOp.AND || op == FilterOp.OR) {
            for (Filter subfilter : ((List<Filter>) filter.getValue())) {
                addAllProperties(subfilter, properties);
            }
        } else if (op == FilterOp.NOT) {
            addAllProperties((Filter) filter.getValue(), properties);
        } else {
            properties.add(filter.getProp());
        }
    }


}
