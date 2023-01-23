package fi.nls.hakunapi.core.property.simple;

import java.util.function.BiConsumer;

import fi.nls.hakunapi.core.QueryContext;
import fi.nls.hakunapi.core.ValueContainer;
import fi.nls.hakunapi.core.ValueProvider;
import fi.nls.hakunapi.core.property.HakunaPropertyDynamic;
import fi.nls.hakunapi.core.property.HakunaPropertyType;
import fi.nls.hakunapi.core.property.HakunaPropertyWriter;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.Schema;

public class HakunaPropertyBoolean extends HakunaPropertyDynamic {

    public HakunaPropertyBoolean(String name, String table, String column, boolean nullable, boolean unique, HakunaPropertyWriter propWriter) {
        super(name, table, column, HakunaPropertyType.BOOLEAN, nullable, unique, propWriter);
    }

    @Override
    public Schema<?> getSchema() {
        return new BooleanSchema();
    }

    @Override
    public Object toInner(String value) {
        if ("null".equals(value)) {
            return null;
        }
        return Boolean.valueOf(value);
    }
    
    @Override
    public BiConsumer<ValueProvider, ValueContainer> getMapperFunction(int iValueProvider, int iValueContainer, QueryContext ctx) {
        return (vp, vc) -> {
            Boolean b = vp.getBoolean(iValueProvider);
            if (b == null) {
                vc.setNull(iValueContainer);
            } else {
                vc.setBoolean(iValueContainer, b);
            }
        };
    }

}
