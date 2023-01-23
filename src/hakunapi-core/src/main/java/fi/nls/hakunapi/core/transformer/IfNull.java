package fi.nls.hakunapi.core.transformer;

import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyTransformed;
import fi.nls.hakunapi.core.property.HakunaPropertyType;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

public class IfNull implements ValueTransformer {

    private HakunaPropertyType innerType;
    private Object replacement;

    @Override
    public void init(HakunaProperty property, String arg) {
        if (!(property instanceof HakunaPropertyTransformed)) {
            throw new IllegalArgumentException("Expected HakunaPropertyTransformed");
        }
        HakunaPropertyTransformed t = (HakunaPropertyTransformed) property;
        this.innerType = t.getInnerType();
        switch (innerType) {
        case INT:
            replacement = Integer.valueOf(arg);
            break;
        case LONG:
            replacement = Long.valueOf(arg);
            break;
        case STRING:
            replacement = arg;
            break;
        default:
            throw new IllegalArgumentException("Invalid type");
        }
    }

    @Override
    public HakunaPropertyType getPublicType() {
        return innerType;
    }

    @Override
    public Object toInner(String value) throws IllegalArgumentException {
        Object v;
        switch (innerType) {
        case INT:
            v = Integer.parseInt(value);
            break;
        case LONG:
            v = Long.parseLong(value);
            break;
        default:
            v = value;
        }
        return replacement.equals(v) ? null : v;
    }

    @Override
    public Object toPublic(Object value) {
        return value == null ? replacement : value;
    }

    @Override
    public Schema<?> getPublicSchema() {
        switch (innerType) {
        case INT:
            return new IntegerSchema();
        case LONG:
            return new IntegerSchema().type("int64");
        case STRING:
            return new StringSchema();
        default:
            throw new IllegalArgumentException();
        }
    }

}
