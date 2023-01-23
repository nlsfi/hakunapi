package fi.nls.hakunapi.core.transformer;

import java.time.Instant;

import fi.nls.hakunapi.core.property.HakunaPropertyTransformed;
import fi.nls.hakunapi.core.property.HakunaPropertyType;

public class TimestamptzToDateTransformer extends TimestampToDateTransformer {
    
    @Override
    protected void assertType(HakunaPropertyTransformed t) throws IllegalArgumentException {
        if (t.getInnerType() != HakunaPropertyType.TIMESTAMPTZ) {
            throw new IllegalArgumentException("Expected Timestamp property");
        }
    }
    
    @Override
    public Object toPublic(Object value) {
        if (value == null) {
            return null;
        }
        return ((Instant) value).atZone(zone).toLocalDate();
    }

}
