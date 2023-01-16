package fi.nls.hakunapi.smile;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.SerializableString;

import fi.nls.hakunapi.core.GeometryWriter;
import fi.nls.hakunapi.core.geom.HakunaGeometryType;

public class SmileGeometryWriter implements GeometryWriter {

    private final JsonGenerator json;
    private final SerializableString fieldName;

    public SmileGeometryWriter(JsonGenerator json, SerializableString fieldName) {
        this.json = json;
        this.fieldName = fieldName;
    }

    @Override
    public void init(HakunaGeometryType type, int srid, int dimension) throws Exception {
        json.writeFieldName(fieldName);
        json.writeStartObject();
        json.writeFieldName(GeoJSONStrings.TYPE);
        json.writeString(getGeoJSONType(type));
        json.writeFieldName(GeoJSONStrings.COORDINATES);
    }

    private SerializableString getGeoJSONType(HakunaGeometryType type) {
        switch (type) {
        case POINT:
            return GeoJSONStrings.POINT;
        case LINESTRING:
            return GeoJSONStrings.LINESTRING;
        case POLYGON:
            return GeoJSONStrings.POLYGON;
        case MULTIPOINT:
            return GeoJSONStrings.MULTI_POINT;
        case MULTILINESTRING:
            return GeoJSONStrings.MULTI_LINESTRING;
        case MULTIPOLYGON:
            return GeoJSONStrings.MULTI_POLYGON;
        case GEOMETRYCOLLECTION:
            throw new IllegalArgumentException("Geometry collection not yet supported!");
        default:
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void end() throws Exception {
        json.writeEndObject();
    }

    @Override
    public void writeCoordinate(double x, double y) throws Exception {
        json.writeStartArray();
        json.writeNumber(x);
        json.writeNumber(y);
        json.writeEndArray();
    }

    @Override
    public void writeCoordinate(double x, double y, double z) throws Exception {
        json.writeStartArray();
        json.writeNumber(x);
        json.writeNumber(y);
        json.writeNumber(z);
        json.writeEndArray();
    }

    @Override
    public void writeCoordinate(double x, double y, double z, double m) throws Exception {
        json.writeStartArray();
        json.writeNumber(x);
        json.writeNumber(y);
        json.writeNumber(z);
        json.writeNumber(m);
        json.writeEndArray();
    }

    @Override
    public void startRing() throws Exception {
        json.writeStartArray();
    }

    @Override
    public void endRing() throws Exception {
        json.writeEndArray();
    }

}
