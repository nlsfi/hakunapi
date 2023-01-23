package fi.nls.hakunapi.core.transformer;

import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyTransformed;
import fi.nls.hakunapi.core.property.HakunaPropertyType;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

public class NullIf implements ValueTransformer {

    private HakunaPropertyType innerType;
    private Object nullValue;

    @Override
    public void init(HakunaProperty property, String arg) {
        if (!(property instanceof HakunaPropertyTransformed)) {
            throw new IllegalArgumentException("Expected HakunaPropertyTransformed");
        }
        HakunaPropertyTransformed t = (HakunaPropertyTransformed) property;
        this.innerType = t.getInnerType();
        switch (innerType) {
        case INT:
            nullValue = Integer.valueOf(arg);
            break;
        case LONG:
            nullValue = Long.valueOf(arg);
            break;
        case STRING:
            nullValue = arg;
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
        if ("null".equals(value)) {
            return nullValue;
        }
        return value;
    }

    @Override
    public Object toPublic(Object value) {
        return nullValue.equals(value) ? null : value;
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
