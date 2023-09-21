package fi.nls.hakunapi.geojson.hakuna;

import java.io.IOException;

import fi.nls.hakunapi.core.GeometryWriter;
import fi.nls.hakunapi.core.geom.HakunaGeometryType;

public class HakunaGeoJSONGeometryWriter implements GeometryWriter {

    protected final HakunaJsonWriter json;
    protected final byte[] fieldName;
    protected final boolean lonLat;

    public HakunaGeoJSONGeometryWriter(HakunaJsonWriter json, byte[] fieldName, boolean lonLat) {
        this.json = json;
        this.fieldName = fieldName;
        this.lonLat = lonLat;
    }

    @Override
    public void init(HakunaGeometryType type, int srid, int dimension) throws IOException {
        if (fieldName != null) {
            json.writeFieldName(fieldName);
        }
        json.writeStartObject();
        json.writeFieldName(HakunaGeoJSON.TYPE);
        json.writeStringUnsafe(getTypeUTF8(type));
        json.writeFieldName(HakunaGeoJSON.COORDINATES);
    }

    public void end() throws IOException {
        json.writeEndObject();
    }

    private byte[] getTypeUTF8(HakunaGeometryType type) {
        switch (type) {
        case POINT: return HakunaGeoJSON.POINT;
        case LINESTRING: return HakunaGeoJSON.LINESTRING;
        case POLYGON: return HakunaGeoJSON.POLYGON;
        case MULTIPOINT: return HakunaGeoJSON.MULTI_POINT;
        case MULTILINESTRING: return HakunaGeoJSON.MULTI_LINESTRING;
        case MULTIPOLYGON: return HakunaGeoJSON.MULTI_POLYGON;
        default: throw new IllegalArgumentException();
        }
    }

    public void writeCoordinate(double x, double y) throws IOException {
        if (lonLat) {
            json.writeCoordinate(x, y);
        } else {
            json.writeCoordinate(y, x);
        }
    }

    public void writeCoordinate(double x, double y, double z) throws IOException {
        if (lonLat) {
            json.writeCoordinate(x, y, z);
        } else {
            json.writeCoordinate(y, x, z);
        }
    }

    public void writeCoordinate(double x, double y, double z, double m) throws IOException {
        if (lonLat) {
            json.writeCoordinate(x, y, z, m);
        } else {
            json.writeCoordinate(y, x, z, m);
        }
    }

    public void startRing() throws IOException {
        json.writeStartArray();
    }

    public void endRing() throws IOException {
        json.writeEndArray();
    }

}
