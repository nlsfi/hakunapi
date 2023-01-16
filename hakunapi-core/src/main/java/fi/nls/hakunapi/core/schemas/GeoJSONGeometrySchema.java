package fi.nls.hakunapi.core.schemas;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.stream.Collectors;

import fi.nls.hakunapi.core.geom.HakunaGeometryType;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

public final class GeoJSONGeometrySchema {

    private GeoJSONGeometrySchema() {}

    private static final EnumSet<HakunaGeometryType> GEOMETRY_ONE_OF = EnumSet.of(
            HakunaGeometryType.POINT,
            HakunaGeometryType.LINESTRING,
            HakunaGeometryType.POLYGON,
            HakunaGeometryType.MULTIPOINT,
            HakunaGeometryType.MULTILINESTRING,
            HakunaGeometryType.MULTIPOLYGON
    );

    public static final Schema getSchema(HakunaGeometryType type) {
        if (type == HakunaGeometryType.GEOMETRY) {
            return new ComposedSchema().oneOf(GEOMETRY_ONE_OF.stream()
                    .map(GeoJSONGeometrySchema::getSchema)
                    .collect(Collectors.toList()));
        } else if (type == HakunaGeometryType.GEOMETRYCOLLECTION) {
            ObjectSchema schema = new ObjectSchema();
            schema.required(Arrays.asList("type", "geometries"));
            schema.addProperties("type", new StringSchema()._enum(Collections.singletonList(type.geoJsonType)));
            schema.addProperties("geometries", getSchema(HakunaGeometryType.GEOMETRY));
            return schema;
        } else {
            ObjectSchema schema = new ObjectSchema();
            schema.required(Arrays.asList("type", "coordinates"));
            schema.addProperties("type", new StringSchema()._enum(Collections.singletonList(type.geoJsonType)));
            schema.addProperties("coordinates", getCoordinatesSchema(type));
            return schema;
        }
    }

    private static final Schema getCoordinatesSchema(HakunaGeometryType type) {
        switch (type) {
        case POINT:
            return getCoordinateSchema();
        case LINESTRING:
            return new ArraySchema().items(getCoordinateSchema()).minItems(2);
        case POLYGON:
            return new ArraySchema().items(new ArraySchema().items(getCoordinateSchema()).minItems(4));
        case MULTIPOINT:
            return new ArraySchema().items(getCoordinatesSchema(HakunaGeometryType.POINT));
        case MULTILINESTRING:
            return new ArraySchema().items(getCoordinatesSchema(HakunaGeometryType.LINESTRING));
        case MULTIPOLYGON:
            return new ArraySchema().items(getCoordinatesSchema(HakunaGeometryType.POLYGON));
        default:
            throw new RuntimeException();
        }
    }

    private static final Schema getCoordinateSchema() {
        return new ArraySchema().items(new NumberSchema()).minItems(2);
    }

}
