package fi.nls.hakunapi.html.model;

import java.io.IOException;

import fi.nls.hakunapi.core.geom.HakunaGeometryType;
import fi.nls.hakunapi.geojson.hakuna.HakunaGeoJSON;
import fi.nls.hakunapi.geojson.hakuna.HakunaGeoJSONGeometryWriter;
import fi.nls.hakunapi.geojson.hakuna.HakunaJsonWriter;

public class HakunaGeoJSONGeometryWriterNoField extends HakunaGeoJSONGeometryWriter {

    public HakunaGeoJSONGeometryWriterNoField(HakunaJsonWriter json) {
        super(json, null);
    }

    @Override
    public void init(HakunaGeometryType type, int srid, int dimension) throws IOException {
        // json.writeFieldName(fieldName);
        json.writeStartObject();
        json.writeFieldName(HakunaGeoJSON.TYPE);
        json.writeString(getTypeName(type));
        json.writeFieldName(HakunaGeoJSON.COORDINATES);
    }

    protected String getTypeName(HakunaGeometryType type) {
        switch (type) {
        case POINT: return "Point";
        case LINESTRING: return "LineString";
        case POLYGON: return "Polygon";
        case MULTIPOINT: return "MultiPoint";
        case MULTILINESTRING: return "MultiLineString";
        case MULTIPOLYGON: return "MultiPolygon";
        default: throw new IllegalArgumentException();
        }
    }

    @Override
    public void writeCoordinate(double x, double y, double z) throws IOException {
        json.writeCoordinate(x, y);
    }

    @Override
    public void writeCoordinate(double x, double y, double z, double m) throws IOException {
        json.writeCoordinate(x, y);
    }

}
