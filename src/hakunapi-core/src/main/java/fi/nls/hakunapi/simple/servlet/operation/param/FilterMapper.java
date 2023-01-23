package fi.nls.hakunapi.simple.servlet.operation.param;

import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.property.HakunaProperty;

public interface FilterMapper {
    
    public void init(String arg);
    public Filter toSingleFilter(HakunaProperty property, String value) throws IllegalArgumentException;

}
