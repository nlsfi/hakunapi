package fi.nls.hakunapi.simple.servlet.operation.param;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.property.HakunaProperty;

public class DateRangeMapper extends DatetimeRangeMapper {

    @Override
    public Filter toSingleFilter(HakunaProperty property, String value) throws IllegalArgumentException {
        if ("null".equalsIgnoreCase(value)) {
            return Filter.isNull(property);
        }

        LocalDate start, end;

        int i = value.indexOf('/');
        if (i < 0) {
            start = parse(value, null, property.getName());
            end = start;
        } else {
            start = parse(value.substring(0, i), null, property.getName());
            end = parse(value.substring(i + 1), null, property.getName());
        }
        
        if (start != null && end != null) {
            if (start.isAfter(end)) {
                throw new IllegalArgumentException("Invalid range, begin must not be after end");
            } else if (start.equals(end)) {
                return Filter.equalTo(property, start.toString());
            }
        }

        if (start == null && end == null) {
            return null;
        }

        Filter a = null;
        Filter b = null;

        if (start != null) {
            if ((mode & MASK_GTE) == 0) {
                a = Filter.greaterThan(property, start.toString());
            } else {
                a = Filter.greaterThanOrEqualTo(property, start.toString());
            }
        }

        if (end != null) {
            if ((mode & MASK_LTE) == 0) {
                b = Filter.lessThan(property, end.toString());
            } else {
                b = Filter.lessThanThanOrEqualTo(property, end.toString());
            }
        }

        if (a == null) {
            return b;
        } else if (b == null) {
            return a;
        } else {
            return Filter.and(a, b);
        }
    }

    private static LocalDate parse(String str, LocalDate openValue, String paramName) {
        if (str.isEmpty() || "..".equals(str)) {
            return openValue;
        }
        try {
            return LocalDate.parse(str);
        } catch (DateTimeParseException e) {
            String msg = String.format("'%s' value could not be parsed", paramName);
            throw new IllegalArgumentException(msg);
        }
    }

}
