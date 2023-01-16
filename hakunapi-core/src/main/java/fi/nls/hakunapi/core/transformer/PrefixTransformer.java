package fi.nls.hakunapi.core.transformer;

import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyTransformed;
import fi.nls.hakunapi.core.property.HakunaPropertyType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

public class PrefixTransformer implements ValueTransformer {

    private HakunaPropertyType innerType;
    private String prefix;

    @Override
    public void init(HakunaProperty property, String arg) {
        if (!(property instanceof HakunaPropertyTransformed)) {
            throw new IllegalArgumentException("Expected HakunaPropertyTransformed");
        }
        HakunaPropertyTransformed t = (HakunaPropertyTransformed) property;
        this.innerType = t.getInnerType();
        this.prefix = arg;
        if (innerType != HakunaPropertyType.INT
                && innerType != HakunaPropertyType.LONG
                && innerType != HakunaPropertyType.STRING) {
            throw new IllegalArgumentException("Invalid type");
        }
    }

    @Override
    public HakunaPropertyType getPublicType() {
        return HakunaPropertyType.STRING;
    }

    @Override
    public Object toInner(String v) throws IllegalArgumentException {
        if (!v.startsWith(prefix)) {
            return HakunaProperty.UNKNOWN;
        }
        v = v.substring(prefix.length());
        switch (innerType) {
        case INT:
            return Integer.parseInt(v);
        case LONG:
            return Long.parseLong(v);
        case STRING:
            return v;
        default:
            throw new RuntimeException("Contact service administrator!");
        }
    }

    @Override
    public Object toPublic(Object value) {
        return prefix + value.toString();
    }

    @Override
    public Schema<?> getPublicSchema() {
        return new StringSchema();
    }

}
