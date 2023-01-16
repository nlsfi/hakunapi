package fi.nls.hakunapi.geojson;

import fi.nls.hakunapi.core.geom.HakunaGeometryType;
import fi.nls.hakunapi.core.schemas.GeoJSONGeometrySchema;
import io.swagger.v3.oas.models.media.Schema;

public class MultiPolygonGeoJSON extends GeoJSONGeometryBase {

    @Override
    public String getComponentName() {
        return "multipolygonGeoJSON";
    }

    @Override
    public Schema getSchema() {
        return GeoJSONGeometrySchema.getSchema(HakunaGeometryType.MULTIPOLYGON);
    }

}
