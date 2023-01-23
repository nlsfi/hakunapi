package fi.nls.hakunapi.core.property;

import java.util.ArrayList;
import java.util.Set;

import io.swagger.v3.oas.models.media.Schema;

public class HakunaPropertyNumberEnum extends HakunaPropertyWrapper {

    private final Set<Long> enumeration;

    public HakunaPropertyNumberEnum(HakunaProperty wrapped, Set<Long> enumeration) {
        super(wrapped);
        switch (wrapped.getType()) {
        case INT:
        case LONG:
            break;
        default:
            throw new IllegalArgumentException("Illegal type for number enumeration");
        }
        if (enumeration.isEmpty()) {
            throw new IllegalArgumentException("Enumeration is empty!");
        }
        this.enumeration = enumeration;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Schema<Number> getSchema() {
        Schema<Number> schema = (Schema<Number>) wrapped.getSchema();
        schema.setEnum(new ArrayList<>(enumeration));
        return schema;
    }

    private boolean contains(Long n) {
        return enumeration.contains(n);
    }

    @Override
    public Object toInner(String value) {
        long v = Long.parseLong(value);
        if (!contains(v)) {
            throw new IllegalArgumentException("Value not in enumeration");
        }
        return wrapped.toInner(value);
    }

}
