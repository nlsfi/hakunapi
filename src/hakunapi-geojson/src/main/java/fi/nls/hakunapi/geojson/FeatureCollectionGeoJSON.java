package fi.nls.hakunapi.geojson;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;

import fi.nls.hakunapi.core.schemas.Component;
import fi.nls.hakunapi.core.schemas.Link;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

public class FeatureCollectionGeoJSON implements Component {

    @Override
    public String getComponentName() {
        return "featureCollectionGeoJSON";
    }

    @Override
    public Schema toSchema(Map<String, Schema> components) {
        if (!components.containsKey(getComponentName())) {
            Schema schema = new ObjectSchema()
                    .required(Arrays.asList("type", "features"))
                    .addProperty("type", new StringSchema()._enum(Arrays.asList("FeatureCollection")))
                    .addProperty("features", new ArraySchema().items(new FeatureGeoJSON().toSchema(components)))
                    .addProperty("links", new ArraySchema().items(new Link().toSchema(components)))
                    .addProperty("timeStamp", new DateTimeSchema())
                    .addProperty("numberMatched", new IntegerSchema().minimum(BigDecimal.ZERO))
                    .addProperty("numberReturned", new IntegerSchema().minimum(BigDecimal.ZERO));
            components.put(getComponentName(), schema);
        }
        return new Schema<>().$ref(getComponentName());
    }

}
