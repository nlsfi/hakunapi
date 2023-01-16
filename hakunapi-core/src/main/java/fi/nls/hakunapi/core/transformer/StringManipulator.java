package fi.nls.hakunapi.core.transformer;

import java.util.ArrayList;
import java.util.List;

import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyTransformed;
import fi.nls.hakunapi.core.property.HakunaPropertyType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

/**
 * String to String using special pattern
 */
public class StringManipulator implements ValueTransformer {

    private List<Part> parts;

    @Override
    public void init(HakunaProperty property, String arg) {
        if (!(property instanceof HakunaPropertyTransformed)) {
            throw new IllegalArgumentException("Expected HakunaPropertyTransformed");
        }
        HakunaPropertyTransformed t = (HakunaPropertyTransformed) property;
        if (t.getInnerType() != HakunaPropertyType.STRING) {
            throw new IllegalArgumentException("Invalid type");
        }
        parseParts(arg);
    }

    protected void parseParts(String arg) {
        this.parts = new ArrayList<>();
        String[] patternParts = arg.split(",");
        for (String pattern : patternParts) {
            parts.add(parsePart(pattern));
        }
    }

    private Part parsePart(String pattern) {
        char c = pattern.charAt(0);
        switch (c) {
        case '"':
        case '\'':
            return new StaticPart(pattern.substring(1, pattern.length() - 1));
        case '%':
            char d = pattern.charAt(1);
            switch (d) {
            case 'd':
                int i = pattern.indexOf('[', 2);
                int j = pattern.indexOf(':', i + 1);
                int k = pattern.indexOf(']', j + 1);
                if (i < 0 || j < 0 || k < 0 || i + 1 >= j || j + 1 >= k) {
                    throw new IllegalArgumentException("Can't handle pattern " + pattern);
                }
                int from = Integer.parseInt(pattern.substring(i + 1, j));
                int to = Integer.parseInt(pattern.substring(j + 1, k));
                return new IntegerSlicePart(from, to);
            }
        }
        throw new IllegalArgumentException("Can't handle pattern " + pattern);
    }

    @Override
    public HakunaPropertyType getPublicType() {
        return HakunaPropertyType.STRING;
    }

    @Override
    public Object toInner(String value) {
        if (value == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Part part : parts) {
            Object ret = part.toInner(value, sb);
            if (ret == HakunaProperty.UNKNOWN) {
                return ret;
            }
            value = ret.toString();
        }
        return sb.toString();
    }

    @Override
    public String toPublic(Object value) {
        if (value == null) {
            return null;
        }
        String input = value.toString();
        StringBuilder sb = new StringBuilder();
        for (Part part : parts) {
            part.toOuter(input, sb);
        }
        return sb.toString();
    }

    @Override
    public Schema<?> getPublicSchema() {
        return new StringSchema();
    }

    protected interface Part {
        Object toInner(String input, StringBuilder out);
        void toOuter(String input, StringBuilder out);
    }

    protected class StaticPart implements Part {

        private final String value;

        private StaticPart(String value) {
            this.value = value;
        }

        @Override
        public Object toInner(String input, StringBuilder sb) {
            // Skip static part
            String seg = input.substring(0, value.length());
            return value.equals(seg) ? input.substring(value.length()) : HakunaProperty.UNKNOWN;
        }

        @Override
        public void toOuter(String input, StringBuilder sb) {
            sb.append(value);
        }

    }

    protected class IntegerSlicePart implements Part {

        private final int from;
        private final int to;

        private IntegerSlicePart(int from, int to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public Object toInner(String input, StringBuilder sb) {
            int digits = to - from;
            int end = 0;
            for (; end < input.length() && end < digits; end++) {
                if (!Character.isDigit(input.charAt(end))) {
                    break;
                }
            }
            if (end == 0) {
                return HakunaProperty.UNKNOWN;
            }
            long value = Long.parseLong(input.substring(0, end));
            String string = String.valueOf(value);
            int pad = digits - string.length();
            for (int i = 0; i < pad; i++) {
                sb.append('0');
            }
            sb.append(string);
            return input.substring(end);
        }

        @Override
        public void toOuter(String input, StringBuilder sb) {
            int i = from;
            for (; i < to; i++) {
                char c = input.charAt(i);
                if (c != '0') {
                    break;
                }
            }
            if (i == to) {
                sb.append('0');
            } else {
                sb.append(input, i, to);
            }
        }

    }

}
