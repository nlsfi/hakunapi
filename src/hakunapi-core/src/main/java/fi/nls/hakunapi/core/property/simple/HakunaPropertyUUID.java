package fi.nls.hakunapi.core.property.simple;

import java.util.UUID;
import java.util.function.BiConsumer;

import fi.nls.hakunapi.core.QueryContext;
import fi.nls.hakunapi.core.ValueContainer;
import fi.nls.hakunapi.core.ValueProvider;
import fi.nls.hakunapi.core.property.HakunaPropertyDynamic;
import fi.nls.hakunapi.core.property.HakunaPropertyType;
import fi.nls.hakunapi.core.property.HakunaPropertyWriter;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.UUIDSchema;

public class HakunaPropertyUUID extends HakunaPropertyDynamic {

    public HakunaPropertyUUID(String name, String table, String column, boolean nullable, boolean unique, HakunaPropertyWriter propWriter) {
        super(name, table, column, HakunaPropertyType.UUID, nullable, unique, propWriter);
    }

    @Override
    public Schema<?> getSchema() {
        return new UUIDSchema();
    }

    @Override
    public Object toInner(String value) {
        if (nullable && "null".equalsIgnoreCase(value)) {
            return null;
        }
        return UUID.fromString(value);
    }

    @Override
    public BiConsumer<ValueProvider, ValueContainer> getMapperFunction(int iValueProvider, int iValueContainer, QueryContext ctx) {
        return (vp, vc) -> {
            vc.setObject(iValueContainer, vp.getUUID(iValueProvider));
        };
    }

}
