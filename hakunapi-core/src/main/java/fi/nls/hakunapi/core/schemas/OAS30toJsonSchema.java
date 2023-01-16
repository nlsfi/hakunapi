package fi.nls.hakunapi.core.schemas;

import java.math.BigDecimal;

import io.swagger.v3.oas.models.media.Schema;

public class OAS30toJsonSchema {

    public static Schema<?> toJsonSchema(Schema<?> schema) {
        if ("integer".equals(schema.getType())) {
            schema.setFormat(null);
            if (schema.getMinimum() != null) {
                BigDecimal min = schema.getMinimum();
                if (schema.getExclusiveMinimum()) {
                    min = min.add(BigDecimal.ONE);
                }
                schema.setMinimum(min);
            }
            if (schema.getMinimum() != null) {
                BigDecimal max = schema.getMaximum();
                if (schema.getExclusiveMaximum()) {
                    max = max.subtract(BigDecimal.ONE);
                }
                schema.setMaximum(max);
            }
        } else if ("number".equals(schema.getType())) {
            schema.setFormat(null);
        }
        return schema;
    }

}
