package fi.nls.hakunapi.simple.servlet.operation.param;

import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyType;

public class LikeFilterMapper implements FilterMapper {
    
    private boolean caseInsensitive = false;
    private char wild = '*';
    private char single = '?';
    private char escape = '\\';
    
    @Override
    public void init(String arg) {
        if (arg == null || arg.isEmpty()) {
            return;
        }
        String[] arguments = arg.split(",");
        this.caseInsensitive = Boolean.parseBoolean(arguments[0]);
        if (arguments.length > 1) {
            this.wild = arguments[1].charAt(0); 
        }
        if (arguments.length > 2) {
            this.single = arguments[2].charAt(0);
        }
        if (arguments.length > 3) {
            this.escape = arguments[3].charAt(0);
        }
    }

    @Override
    public Filter toSingleFilter(HakunaProperty property, String value) throws IllegalArgumentException {
        if (value == null || value.isEmpty()) {
            return null;
        }
        if (property.getType() != HakunaPropertyType.STRING) {
            throw new IllegalArgumentException("Invalid type");
        }
        return Filter.like(property, value, wild, single, escape, caseInsensitive);
    }

}
