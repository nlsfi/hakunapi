package fi.nls.hakunapi.geojson;

import java.util.Map;

import fi.nls.hakunapi.core.schemas.Component;
import io.swagger.v3.oas.models.media.Schema;

public abstract class GeoJSONGeometryBase implements Component {

    public abstract Schema getSchema();

    public Schema toSchema(Map<String, Schema> components) {
        if (!components.containsKey(getComponentName())) {
            components.put(getComponentName(), getSchema());
        }
        return new Schema<>().$ref(getComponentName());
    }

}
