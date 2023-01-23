package fi.nls.hakunapi.core.property.simple;

import java.time.Instant;
import java.util.function.BiConsumer;

import fi.nls.hakunapi.core.QueryContext;
import fi.nls.hakunapi.core.ValueContainer;
import fi.nls.hakunapi.core.ValueProvider;
import fi.nls.hakunapi.core.property.HakunaPropertyDynamic;
import fi.nls.hakunapi.core.property.HakunaPropertyType;
import fi.nls.hakunapi.core.property.HakunaPropertyWriter;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.Schema;

/**
 * Timestamp with time zone
 */
public class HakunaPropertyTimestamptz extends HakunaPropertyDynamic {

    public HakunaPropertyTimestamptz(String name, String table, String column, boolean nullable, boolean unique, HakunaPropertyWriter propWriter) {
        super(name, table, column, HakunaPropertyType.TIMESTAMPTZ, nullable, unique, propWriter);
    }

    @Override
    public Schema<?> getSchema() {
        return new DateTimeSchema();
    }

    @Override
    public Object toInner(String value) {
        if (nullable && "null".equalsIgnoreCase(value)) {
            return null;
        }
        return Instant.parse(value);
    }

    @Override
    public BiConsumer<ValueProvider, ValueContainer> getMapperFunction(int iValueProvider, int iValueContainer, QueryContext ctx) {
        return (vp, vc) -> {
            vc.setObject(iValueContainer, vp.getInstant(iValueProvider));
        };
    }

}