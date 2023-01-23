package fi.nls.hakunapi.core.transformer;

import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyTransformed;
import fi.nls.hakunapi.core.property.HakunaPropertyType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

public class FixedLengthStringToIntegerTransformer implements ValueTransformer {

    private HakunaProperty property;
    private HakunaPropertyType innerType;
    private int numDigits;

    @Override
    public void init(HakunaProperty property, String arg) {
        if (!(property instanceof HakunaPropertyTransformed)) {
            throw new IllegalArgumentException("Expected HakunaPropertyTransformed");
        }
        this.property = property;
        HakunaPropertyTransformed t = (HakunaPropertyTransformed) property;
        this.innerType = t.getInnerType();
        if (innerType != HakunaPropertyType.INT
                && innerType != HakunaPropertyType.LONG) {
            throw new IllegalArgumentException("Invalid type");
        }
        this.numDigits = Integer.parseInt(arg);
    }

    @Override
    public HakunaPropertyType getPublicType() {
        return HakunaPropertyType.STRING;
    }

    @Override
    public Object toInner(String value) throws IllegalArgumentException {
        if (value == null) {
            return null;
        }
        if (value.length() != numDigits) {
            String err = String.format("Invalid value %s, expected string of length %d", property.getName(), numDigits);
            throw new IllegalArgumentException(err);
        }
        switch (innerType) {
        case INT:
            return Integer.parseInt(value);
        case LONG:
            return Long.parseLong(value);
        default:
            throw new IllegalArgumentException("Invalid type");
        }
    }

    @Override
    public Object toPublic(Object value) {
        if (value == null) {
            return null;
        }
        long val = ((Number) value).longValue();
        return longToString(val, numDigits);
    }

    protected static String longToString(long value, int numDigits) {
        char[] str = new char[numDigits];

        int i = 0;
        if (value < 0) {
            str[i++] = '-';
            value = -value;
        }
        int j = numDigits - 1;
        do {
            str[j--] = (char) ('0' + (value % 10));
            value /= 10;
        } while (j >= i && value != 0);
        for (; j >= i; j--) {
            str[j] = '0';
        }

        return new String(str);
    }

    @Override
    public Schema<?> getPublicSchema() {
        return new StringSchema().minLength(numDigits).maxLength(numDigits);
    }

}
