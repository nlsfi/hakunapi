package fi.nls.hakunapi.core.schemas;

import java.util.Collections;
import java.util.Map;

import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

public class WFS3Exception implements Component {

    private final String code;
    private final String description;

    public WFS3Exception() {
        code = null;
        description = null;
    }

    public WFS3Exception(int code, String description) {
        this.code = Integer.toString(code);
        this.description = description;
    }

    @Override
    public String getComponentName() {
        return "exception";
    }

    @Override
    public Schema toSchema(Map<String, Schema> components) {
        if (!components.containsKey(getComponentName())) {
            Schema schema = new ObjectSchema()
                    .name(getComponentName())
                    .addProperties("code", new StringSchema().name("code"))
                    .addProperties("description", new StringSchema().name("description"));
            schema.setRequired(Collections.singletonList("code"));
            components.put(getComponentName(), schema);
        }
        return new Schema<>().$ref(getComponentName());
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

}
