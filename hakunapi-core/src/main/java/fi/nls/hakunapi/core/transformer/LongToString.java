package fi.nls.hakunapi.core.transformer;

import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyTransformed;
import fi.nls.hakunapi.core.property.HakunaPropertyType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

public class LongToString implements ValueTransformer {

    @Override
    public void init(HakunaProperty property, String arg) {
        if (!(property instanceof HakunaPropertyTransformed)) {
            throw new IllegalArgumentException("Expected HakunaPropertyTransformed");
        }
        HakunaPropertyTransformed t = (HakunaPropertyTransformed) property;
        if (t.getInnerType() != HakunaPropertyType.LONG) {
            throw new IllegalArgumentException("Invalid type");
        }
    }

    @Override
    public HakunaPropertyType getPublicType() {
        return HakunaPropertyType.STRING;
    }

    @Override
    public Object toInner(String value) throws IllegalArgumentException {
        return value == null ? null : Long.parseLong(value);
    }

    @Override
    public Object toPublic(Object value) {
        return value == null ? null : value.toString();
    }

    @Override
    public Schema<?> getPublicSchema() {
        return new StringSchema();
    }

}
