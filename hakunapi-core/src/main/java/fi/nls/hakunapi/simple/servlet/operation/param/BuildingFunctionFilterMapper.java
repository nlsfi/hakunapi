package fi.nls.hakunapi.simple.servlet.operation.param;

import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyType;

public class BuildingFunctionFilterMapper implements FilterMapper {
    
    @Override
    public void init(String arg) {
        
    }

    @Override
    public Filter toSingleFilter(HakunaProperty property, String value) throws IllegalArgumentException {
        if (value == null || value.isEmpty()) {
            return null;
        }
        if (property.getType() != HakunaPropertyType.STRING) {
            throw new IllegalArgumentException("Invalid type");
        }
        if (value.indexOf('%') >= 0 || value.indexOf('*') >= 0 || value.indexOf('\\') >= 0) {
            return Filter.DENY;
        }
        return Filter.like(property, value, '%', '_', '\\', false);
    }

}
