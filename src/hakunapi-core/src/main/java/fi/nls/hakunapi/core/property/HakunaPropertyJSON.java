package fi.nls.hakunapi.core.property;

import java.util.function.BiConsumer;

import fi.nls.hakunapi.core.QueryContext;
import fi.nls.hakunapi.core.ValueContainer;
import fi.nls.hakunapi.core.ValueProvider;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

public class HakunaPropertyJSON extends HakunaPropertyDynamic {

    public HakunaPropertyJSON(String name, String table, String column, HakunaPropertyType type, boolean nullable,
            boolean unique, HakunaPropertyWriter propWriter) {
        super(name, table, column, type, nullable, unique, propWriter);
    }

    @Override
    public Schema<?> getSchema() {

        return new ObjectSchema();
    }

    @Override
    public Object toInner(String value) {
        if ("null".equals(value)) {
            return null;
        }
        return value;
    }

    @Override
    public BiConsumer<ValueProvider, ValueContainer> getMapperFunction(int iValueProvider, int iValueContainer,
            QueryContext ctx) {
        return (vp, vc) -> {
            byte[] json = vp.getJSON(iValueProvider);
            if (json == null) {
                vc.setNull(iValueContainer);
            } else {
                vc.setObject(iValueContainer, json);
            }
        };
    }

}
