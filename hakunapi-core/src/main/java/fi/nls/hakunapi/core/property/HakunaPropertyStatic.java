package fi.nls.hakunapi.core.property;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import fi.nls.hakunapi.core.FeatureWriter;
import fi.nls.hakunapi.core.QueryContext;
import fi.nls.hakunapi.core.ValueMapper;
import fi.nls.hakunapi.core.ValueProvider;
import fi.nls.hakunapi.core.util.StringPair;
import fi.nls.hakunapi.core.util.U;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

/**
 * Property with static/literal value
 * (same value for each feature in the collection)
 */
public class HakunaPropertyStatic extends HakunaPropertyBase {

    private static final String NULL_PREFIX = "null$";

    private final Object value;

    private HakunaPropertyStatic(String name, String table, HakunaPropertyType type, Object value) {
        super(name, table, type, value == null, false, new StaticPropertyWriter(name, type, value));
        this.value = value;
    }

    public static HakunaPropertyStatic create(String name, String table, String value) {
        HakunaPropertyType type = getType(value);
        Object val = parse(type, value);
        return new HakunaPropertyStatic(name, table, type, val);
    }

    public boolean matches(Object o) {
        if (value == null) {
            return o == null;
        }
        return value.equals(o);
    }

    private static HakunaPropertyType getType(String value) {
        if (U.startsWithIgnoreCase(value, NULL_PREFIX)) {
            String t = value.substring(NULL_PREFIX.length());
            return HakunaPropertyType.valueOf(t);
        }
        try {
            Integer.parseInt(value);
            return HakunaPropertyType.INT;
        } catch (NumberFormatException ignore) {}
        try {
            Long.parseLong(value);
            return HakunaPropertyType.LONG;
        } catch (NumberFormatException ignore) {}
        try {
            Double.parseDouble(value);
            return HakunaPropertyType.DOUBLE;
        } catch (NumberFormatException ignore) {}

        if ("false".equals(value.toLowerCase()) || "true".equals(value.toLowerCase())) {
            return HakunaPropertyType.BOOLEAN;
        }

        return HakunaPropertyType.STRING;
    }

    private static Object parse(HakunaPropertyType type, String value) {
        if (U.startsWithIgnoreCase(value, NULL_PREFIX)) {
            return null;
        }
        switch (type) {
        case INT:
            return Integer.parseInt(value);
        case LONG:
            return Long.parseLong(value);
        case DOUBLE:
            return Double.parseDouble(value);
        case BOOLEAN:
            return Boolean.parseBoolean(value);
        default:
            return value;
        }
    }

    @Override
    public final boolean isStatic() {
        return true;
    }

    @Override
    public Schema<?> getSchema() {
        switch (type) {
        case BOOLEAN:
            return new BooleanSchema();
        case INT:
            return new IntegerSchema();
        case LONG:
            return new IntegerSchema().format("int64");
        case DOUBLE:
            return new NumberSchema().format("double");
        default:
            return new StringSchema();
        }
    }

    @Override
    public String getTable() {
        return null;
    }

    @Override
    public String getColumn() {
        return null;
    }

    @Override
    public List<String> getColumns() {
        return Collections.emptyList();
    }

    static class StaticPropertyWriter implements HakunaPropertyWriter {

        private final String name;
        private final HakunaPropertyType type;
        private final Object value;

        public StaticPropertyWriter(String name, HakunaPropertyType type, Object value) {
            this.name = name;
            this.type = type;
            this.value = value;
        }

        @Override
        public void write(ValueProvider vp, int i, FeatureWriter writer) throws Exception {
            write(writer);
        }

        public void write(FeatureWriter writer) throws Exception {
            if (value == null) {
                writer.writeNullProperty(name);
                return;
            }
            switch (type) {
            case INT:
                writer.writeProperty(name, (Integer) value);
                break;
            case LONG:
                writer.writeProperty(name, (Long) value);
                break;
            case DOUBLE:
                writer.writeProperty(name, (Double) value);
                break;
            case BOOLEAN:
                writer.writeProperty(name, (Boolean) value);
                break;
            default:
                writer.writeProperty(name, (String) value);
                break;
            }
        }
    }

    @Override
    public Object toInner(String value) {
        return value;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public ValueMapper getMapper(Map<StringPair, Integer> columnToIndex, int iValueContainer, QueryContext ctx) {
        return new ValueMapper(this, (vp, vc) -> vc.setObject(iValueContainer, value));
    }

}
