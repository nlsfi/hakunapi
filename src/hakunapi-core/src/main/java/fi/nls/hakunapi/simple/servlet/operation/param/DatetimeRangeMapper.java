package fi.nls.hakunapi.simple.servlet.operation.param;

import java.time.Instant;
import java.util.function.IntUnaryOperator;

import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.param.DatetimeParam;
import fi.nls.hakunapi.core.property.HakunaProperty;

public class DatetimeRangeMapper implements FilterMapper {

    enum Flags {
        GT(mode -> mode & (~MASK_GTE)),
        GTE(mode -> mode | MASK_GTE),
        LT(mode -> mode & (~MASK_LTE)),
        LTE(mode -> mode | MASK_LTE);

        private final IntUnaryOperator f;

        private Flags(IntUnaryOperator f) {
            this.f = f;
        }
    }

    protected static final int MASK_GTE = 1;
    protected static final int MASK_LTE = 2;

    protected int mode;

    @Override
    public void init(String arg) {
        if (arg == null || arg.isEmpty()) {
            return;
        }

        mode = MASK_GTE | MASK_LTE;

        try {
            mode = Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            String[] args = arg.split(",");
            if (args.length != 2) {
                throw new IllegalArgumentException("Invalid arg for DatetimeRangeMapper! Expected either integer number or two string values for values");
            }
            for (String a : args) {
                Flags flag = Flags.valueOf(a.toUpperCase());
                mode = flag.f.applyAsInt(mode);
            }
        }
    }

    @Override
    public Filter toSingleFilter(HakunaProperty property, String value) throws IllegalArgumentException {
        if ("null".equalsIgnoreCase(value)) {
            return Filter.isNull(property);
        }

        Instant[] instant = DatetimeParam.parse(value, property.getName());
        if (instant == null) {
            return null;
        }

        Instant start = instant[0];
        Instant end = instant[1];
        
        if (start != null && end != null) {
            if (start.isAfter(end)) {
                throw new IllegalArgumentException("Invalid range on '" + property.getName() + "': start must not be after end");
            } else if (start.equals(end)) {
                return Filter.equalTo(property, start.toString());
            }
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

}
