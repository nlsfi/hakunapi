package fi.nls.hakunapi.simple.servlet.operation.param;

import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.property.HakunaProperty;

public class NotEqualToFilterMapper implements FilterMapper {

    public Filter toSingleFilter(HakunaProperty property, String value) throws IllegalArgumentException {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return Filter.notEqualTo(property, value);
    }

    @Override
    public void init(String arg) {
        // NOP
    }

}
