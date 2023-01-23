package fi.nls.hakunapi.core.property;

import java.util.ArrayList;
import java.util.Set;

import io.swagger.v3.oas.models.media.Schema;

public class HakunaPropertyStringEnum extends HakunaPropertyWrapper {

    private final Set<String> enumeration;

    public HakunaPropertyStringEnum(HakunaProperty wrapped, Set<String> enumeration) {
        super(wrapped);
        switch (wrapped.getType()) {
        case STRING:
        case DATE:
        case TIMESTAMP:
        case UUID:
            break;
        default:
            throw new IllegalArgumentException("Illegal type for string enumeration");
        }
        if (enumeration.isEmpty()) {
            throw new IllegalArgumentException("Enumeration is empty!");
        }
        this.enumeration = enumeration;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Schema<String> getSchema() {
        Schema<String> schema = (Schema<String>) wrapped.getSchema();
        schema.setEnum(new ArrayList<>(enumeration));
        return schema;
    }

    private boolean contains(String value) {
        return enumeration.contains(value);
    }

    @Override
    public Object toInner(String value) {
        String v = (String) wrapped.toInner(value);
        if (!contains(v)) {
            throw new IllegalArgumentException("Value not in enumeration");
        }
        return v;
    }
}
