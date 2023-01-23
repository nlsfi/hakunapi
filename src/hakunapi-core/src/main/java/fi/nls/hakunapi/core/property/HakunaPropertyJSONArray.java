package fi.nls.hakunapi.core.property;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

public class HakunaPropertyJSONArray extends HakunaPropertyJSON {

    public HakunaPropertyJSONArray(String name, String table, String column, HakunaPropertyType type, boolean nullable,
            boolean unique, HakunaPropertyWriter propWriter) {
        super(name, table, column, type, nullable, unique, propWriter);
    }

    @Override
    public Schema<?> getSchema() {
        return new ArraySchema().items(new ObjectSchema());
    }
}
