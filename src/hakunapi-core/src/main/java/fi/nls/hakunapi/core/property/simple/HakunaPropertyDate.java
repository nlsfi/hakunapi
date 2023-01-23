package fi.nls.hakunapi.core.property.simple;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.function.BiConsumer;
import java.util.function.Function;

import fi.nls.hakunapi.core.QueryContext;
import fi.nls.hakunapi.core.ValueContainer;
import fi.nls.hakunapi.core.ValueProvider;
import fi.nls.hakunapi.core.property.HakunaPropertyDynamic;
import fi.nls.hakunapi.core.property.HakunaPropertyType;
import fi.nls.hakunapi.core.property.HakunaPropertyWriter;
import io.swagger.v3.oas.models.media.DateSchema;
import io.swagger.v3.oas.models.media.Schema;

public class HakunaPropertyDate extends HakunaPropertyDynamic {

    private final Function<String, LocalDate> parseLocalDate;

    public HakunaPropertyDate(String name, String table, String column, boolean nullable, boolean unique, HakunaPropertyWriter propWriter) {
        this(name, table, column, nullable, unique, propWriter, HakunaPropertyDate::defaultParseLocalDate);
    }

    public HakunaPropertyDate(String name, String table, String column, boolean nullable, boolean unique, HakunaPropertyWriter propWriter, Function<String, LocalDate> parseLocalDate) {
        super(name, table, column, HakunaPropertyType.DATE, nullable, unique, propWriter);
        this.parseLocalDate = parseLocalDate;
    }

    @Override
    public Schema<?> getSchema() {
        return new DateSchema();
    }

    @Override
    public Object toInner(String value) {
        if (nullable && "null".equalsIgnoreCase(value)) {
            return null;
        }
        return parseLocalDate.apply(value);
    }

    private static LocalDate defaultParseLocalDate(String value) {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.ofInstant(Instant.parse(value), ZoneOffset.UTC).toLocalDate();
            } catch (Exception e2) {
                throw e;
            }
        }
    }

    @Override
    public BiConsumer<ValueProvider, ValueContainer> getMapperFunction(int iValueProvider, int iValueContainer, QueryContext ctx) {
        return (vp, vc) -> {
            vc.setObject(iValueContainer, vp.getLocalDate(iValueProvider));
        };
    }

}
