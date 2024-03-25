package fi.nls.hakunapi.geojson;

import java.util.Arrays;
import java.util.Map;

import fi.nls.hakunapi.core.schemas.Component;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

public class GeometryCollectionGeoJSON implements Component {

    @Override
    public String getComponentName() {
        return "geometrycollectionGeoJSON";
    }

    @Override
    public Schema toSchema(Map<String, Schema> components) {
        if (!components.containsKey(getComponentName())) {
            ObjectSchema schema = new ObjectSchema();
            components.put(getComponentName(), schema);
            schema.required(Arrays.asList("type", "geometries"));
            schema.addProperty("type", new StringSchema()._enum(Arrays.asList("GeometryCollection")));
            schema.addProperty("geometries", new ArraySchema().items(new GeometryGeoJSON().toSchema(components)));
        }
        return new Schema<>().$ref(getComponentName());
    }

}
