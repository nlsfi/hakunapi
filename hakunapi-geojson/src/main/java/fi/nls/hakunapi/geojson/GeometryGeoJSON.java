package fi.nls.hakunapi.geojson;

import java.util.Map;

import fi.nls.hakunapi.core.schemas.Component;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;

public class GeometryGeoJSON implements Component {

    @Override
    public String getComponentName() {
        return "geometryGeoJSON";
    }

    @Override
    public Schema toSchema(Map<String, Schema> components) {
        if (!components.containsKey(getComponentName())) {
            ComposedSchema schema = new ComposedSchema();
            // Avoid circular reference in GeometryCollection
            components.put(getComponentName(), schema);
            schema
            .addOneOfItem(new PointGeoJSON().toSchema(components))
            .addOneOfItem(new LineStringGeoJSON().toSchema(components))
            .addOneOfItem(new PolygonGeoJSON().toSchema(components))
            .addOneOfItem(new MultiPointGeoJSON().toSchema(components))
            .addOneOfItem(new MultiLineStringGeoJSON().toSchema(components))
            .addOneOfItem(new MultiPolygonGeoJSON().toSchema(components))
            .addOneOfItem(new GeometryCollectionGeoJSON().toSchema(components));
        }
        return new Schema<>().$ref(getComponentName());
    }

}
