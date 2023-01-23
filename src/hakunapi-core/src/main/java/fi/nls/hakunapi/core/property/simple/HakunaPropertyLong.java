package fi.nls.hakunapi.core.property.simple;

import java.util.function.BiConsumer;

import fi.nls.hakunapi.core.QueryContext;
import fi.nls.hakunapi.core.ValueContainer;
import fi.nls.hakunapi.core.ValueProvider;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyDynamic;
import fi.nls.hakunapi.core.property.HakunaPropertyType;
import fi.nls.hakunapi.core.property.HakunaPropertyWriter;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.Schema;

public class HakunaPropertyLong extends HakunaPropertyDynamic {

    public HakunaPropertyLong(String name, String table, String column, boolean nullable, boolean unique, HakunaPropertyWriter propWriter) {
        super(name, table, column, HakunaPropertyType.LONG, nullable, unique, propWriter);
    }

    @Override
    public Schema<?> getSchema() {
        return new IntegerSchema().format("int64");
    }

    @Override
    public Object toInner(String value) {
        if (nullable && "null".equalsIgnoreCase(value)) {
            return null;
        }

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            try {
                int i = value.indexOf('.');
                if (i >= 0) {
                    long v = 0L;
                    if (i > 0) {
                        v = Long.parseLong(value.substring(0, i));
                    }

                    // Check if the part after decimal point is 0
                    if (i < value.length() - 1) {
                        long v2 = Long.parseLong(value.substring(i + 1));
                        if (v2 != 0) {
                            return HakunaProperty.UNKNOWN;
                        }
                    }

                    return v;
                }
            } catch (Exception ignore) {}
            throw e;
        }
    }

    @Override
    public BiConsumer<ValueProvider, ValueContainer> getMapperFunction(int iValueProvider, int iValueContainer, QueryContext ctx) {
        return (vp, vc) -> {
            Long v = vp.getLong(iValueProvider);
            if (v == null) {
                vc.setNull(iValueContainer);
            } else {
                vc.setLong(iValueContainer, v);
            }
        };
    }

}
