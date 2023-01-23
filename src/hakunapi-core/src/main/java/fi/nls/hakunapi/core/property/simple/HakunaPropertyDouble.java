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

public class HakunaPropertyDouble extends HakunaPropertyDynamic {

    public HakunaPropertyDouble(String name, String table, String column, boolean nullable, boolean unique, HakunaPropertyWriter propWriter) {
        super(name, table, column, HakunaPropertyType.DOUBLE, nullable, unique, propWriter);
    }

    @Override
    public Schema<?> getSchema() {
        return new NumberSchema().format("double");
    }

    @Override
    public Object toInner(String value) {
        if (nullable && "null".equalsIgnoreCase(value)) {
            return null;
        }
        return Double.valueOf(value);
    }
    
    @Override
    public BiConsumer<ValueProvider, ValueContainer> getMapperFunction(int iValueProvider, int iValueContainer, QueryContext ctx) {
        return (vp, vc) -> {
            Double v = vp.getDouble(iValueProvider);
            if (v == null) {
                vc.setNull(iValueContainer);
            } else {
                vc.setDouble(iValueContainer, v);
            }
        };
    }

}
