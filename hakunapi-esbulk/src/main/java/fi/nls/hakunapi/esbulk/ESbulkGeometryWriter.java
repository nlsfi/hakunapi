package fi.nls.hakunapi.esbulk;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.SerializedString;

import fi.nls.hakunapi.core.GeometryWriter;
import fi.nls.hakunapi.core.geom.HakunaGeometryType;

public class ESbulkGeometryWriter implements GeometryWriter {

    protected final SerializableString fieldName;
    protected JsonGenerator json;

    public ESbulkGeometryWriter(JsonGenerator json, SerializableString fieldName) {
        this.json = json;
        this.fieldName = fieldName;
    }

    public ESbulkGeometryWriter(String fieldName) {
        this.fieldName = new SerializedString(fieldName);
    }

    public void setJsonGenerator(JsonGenerator json) {
        this.json = json;
    }

    @Override
    public void init(HakunaGeometryType type, int srid, int dimension) throws Exception {
        json.writeFieldName(fieldName);
        json.writeStartObject();
        json.writeFieldName(Strings.TYPE);
        json.writeString(getGeoJSONType(type));
        json.writeFieldName(Strings.COORDINATES);
    }

    private SerializableString getGeoJSONType(HakunaGeometryType type) {
        switch (type) {
        case POINT:
            return Strings.POINT;
        case LINESTRING:
            return Strings.LINESTRING;
        case POLYGON:
            return Strings.POLYGON;
        case MULTIPOINT:
            return Strings.MULTI_POINT;
        case MULTILINESTRING:
            return Strings.MULTI_LINESTRING;
        case MULTIPOLYGON:
            return Strings.MULTI_POLYGON;
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
        json.writeEndArray();
    }

    @Override
    public void writeCoordinate(double x, double y, double z, double m) throws Exception {
        json.writeStartArray();
        json.writeNumber(x);
        json.writeNumber(y);
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
