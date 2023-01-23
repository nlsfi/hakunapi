package fi.nls.hakunapi.core.property.simple;

import java.util.function.BiConsumer;

import fi.nls.hakunapi.core.QueryContext;
import fi.nls.hakunapi.core.ValueContainer;
import fi.nls.hakunapi.core.ValueProvider;
import fi.nls.hakunapi.core.property.HakunaPropertyDynamic;
import fi.nls.hakunapi.core.property.HakunaPropertyType;
import fi.nls.hakunapi.core.property.HakunaPropertyWriter;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.Schema;

public class HakunaPropertyFloat extends HakunaPropertyDynamic {

    public HakunaPropertyFloat(String name, String table, String column, boolean nullable, boolean unique, HakunaPropertyWriter propWriter) {
        super(name, table, column, HakunaPropertyType.FLOAT, nullable, unique, propWriter);
    }

    @Override
    public Schema<?> getSchema() {
        return new NumberSchema().format("float");
    }

    @Override
    public Object toInner(String value) {
        if (nullable && "null".equalsIgnoreCase(value)) {
            return null;
        }
        return Float.valueOf(value);
    }
    
    @Override
    public BiConsumer<ValueProvider, ValueContainer> getMapperFunction(int iValueProvider, int iValueContainer, QueryContext ctx) {
        return (vp, vc) -> {
            Float v = vp.getFloat(iValueProvider);
            if (v == null) {
                vc.setNull(iValueContainer);
            } else {
                vc.setFloat(iValueContainer, v);
            }
        };
    }

}
