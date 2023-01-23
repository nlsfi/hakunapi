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

public class HakunaPropertyInt extends HakunaPropertyDynamic {

    public HakunaPropertyInt(String name, String table, String column, boolean nullable, boolean unique, HakunaPropertyWriter propWriter) {
        super(name, table, column, HakunaPropertyType.INT, nullable, unique, propWriter);
    }

    @Override
    public Schema<?> getSchema() {
        return new IntegerSchema();
    }

    @Override
    public Object toInner(String value) {
        if (nullable && "null".equalsIgnoreCase(value)) {
            return null;
        }

        try {
            long v = Long.parseLong(value);
            if (v < Integer.MIN_VALUE || v > Integer.MAX_VALUE) {
                return HakunaProperty.UNKNOWN;
            }
            return (int) v;
        } catch (NumberFormatException e) {
            try {
                int i = value.indexOf('.');
                if (i >= 0) {
                    long v = 0;
                    if (i > 0) {
                        v = Long.parseLong(value.substring(0, i));
                        if (v < Integer.MIN_VALUE || v > Integer.MAX_VALUE) {
                            return HakunaProperty.UNKNOWN;
                        }
                    }

                    // Check if the part after decimal point is 0
                    if (i < value.length() - 1) {
                        long v2 = Long.parseLong(value.substring(i + 1));
                        if (v2 != 0) {
                            return HakunaProperty.UNKNOWN;
                        }
                    }

                    return (int) v;
                }
            } catch (Exception ignore) {}
            throw e;
        }
    }
    
    @Override
    public BiConsumer<ValueProvider, ValueContainer> getMapperFunction(int iValueProvider, int iValueContainer, QueryContext ctx) {
        return (vp, vc) -> {
            Integer v = vp.getInt(iValueProvider);
            if (v == null) {
                vc.setNull(iValueContainer);
            } else {
                vc.setInt(iValueContainer, v);
            }
        };
    }

}
