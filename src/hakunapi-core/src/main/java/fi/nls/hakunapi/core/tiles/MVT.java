package fi.nls.hakunapi.core.tiles;

import java.util.Map;

import fi.nls.hakunapi.core.schemas.Component;
import io.swagger.v3.oas.models.media.BinarySchema;
import io.swagger.v3.oas.models.media.Schema;

public class MVT implements Component {

    @Override
    public String getComponentName() {
        return "mvt";
    }

    @Override
    public Schema toSchema(Map<String, Schema> components) {
        if (!components.containsKey(getComponentName())) {
            components.put(getComponentName(), new BinarySchema());
        }
        return new Schema<>().$ref(getComponentName());
    }

}
