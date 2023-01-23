package fi.nls.hakunapi.core.transformer;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyTransformed;
import fi.nls.hakunapi.core.property.HakunaPropertyType;
import io.swagger.v3.oas.models.media.DateSchema;
import io.swagger.v3.oas.models.media.Schema;

public class TimestampToDateTransformer implements ValueTransformer {
    
    protected ZoneId zone;

    @Override
    public void init(HakunaProperty property, String arg) {
        if (!(property instanceof HakunaPropertyTransformed)) {
            throw new IllegalArgumentException("Expected HakunaPropertyTransformed");
        }
        HakunaPropertyTransformed t = (HakunaPropertyTransformed) property;
        assertType(t);
        if (arg == null || arg.isEmpty()) {
            zone = ZoneId.systemDefault();
        } else {
            zone = ZoneId.of(arg);
        }
    }
    
    protected void assertType(HakunaPropertyTransformed t) throws IllegalArgumentException {
        if (t.getInnerType() != HakunaPropertyType.TIMESTAMP) {
            throw new IllegalArgumentException("Expected Timestamp property");
        }
    }

    @Override
    public HakunaPropertyType getPublicType() {
        return HakunaPropertyType.DATE;
    }

    @Override
    public Object toInner(String value) throws IllegalArgumentException {
        try {
            LocalDate date = LocalDate.parse(value);
            LocalDate next = date.plusDays(1);
            Instant start = date.atStartOfDay().atZone(zone).toInstant();
            Instant end = next.atStartOfDay().atZone(zone).toInstant();
            return Arrays.asList(start, end);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public Object toPublic(Object value) {
        if (value == null) {
            return null;
        }
        return ((LocalDateTime) value).atZone(zone).toLocalDate();
    }

    @Override
    public Schema<?> getPublicSchema() {
        return new DateSchema(); 
    }

}
