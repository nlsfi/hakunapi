package fi.nls.hakunapi.geojson;

import java.util.Arrays;
import java.util.Map;

import fi.nls.hakunapi.core.schemas.Component;
import fi.nls.hakunapi.core.schemas.Link;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

public class FeatureGeoJSON implements Component {

    @Override
    public String getComponentName() {
        return "featureGeoJSON";
    }

    @Override
    public Schema toSchema(Map<String, Schema> components) {
        if (!components.containsKey(getComponentName())) {
            Schema schema = new ObjectSchema()
                    .required(Arrays.asList("type", "geometry", "properties"))
                    .addProperty("type", new StringSchema()._enum(Arrays.asList("Feature")))
                    .addProperty("geometry", new GeometryGeoJSON().toSchema(components))
                    .addProperty("properties", new ObjectSchema().nullable(true))
                    .addProperty("id", new ComposedSchema()
                            .addOneOfItem(new StringSchema())
                            .addOneOfItem(new Schema<>().type("integer")))
                    .addProperty("links", new ArraySchema().items(new Link().toSchema(components)));
            components.put(getComponentName(), schema);
        }
        return new Schema<>().$ref(getComponentName());
    }

}
