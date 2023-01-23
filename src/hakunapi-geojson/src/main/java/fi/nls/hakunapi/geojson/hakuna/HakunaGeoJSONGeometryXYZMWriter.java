package fi.nls.hakunapi.geojson.hakuna;

import java.io.IOException;

import fi.nls.hakunapi.core.GeometryWriter;
import fi.nls.hakunapi.core.geom.HakunaGeometryType;
import fi.nls.hakunapi.core.geom.HakunaGeometryDimension;


public class HakunaGeoJSONGeometryXYZMWriter implements GeometryWriter {

    private final HakunaGeometryDimension mode;
    private final HakunaJsonWriter json;
    private final byte[] fieldName;

    public HakunaGeoJSONGeometryXYZMWriter(HakunaJsonWriter json, byte[] fieldName,
            HakunaGeometryDimension mode) {
        this.json = json;
        this.fieldName = fieldName;
        this.mode = mode;
    }

    public void init(HakunaGeometryType type, int srid, int dimension) throws IOException {
        json.writeFieldName(fieldName);
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
        json.writeCoordinate(x, y);
    }

    public void writeCoordinate(double x, double y, double z) throws IOException {
        switch(mode) {
        case XY:
            json.writeCoordinate(x, y);
            break;
        case DEFAULT:
        case GEOMETRY:
        case XYZ:
        case XYZM:
        default:
            json.writeCoordinate(x, y, z);
            break;
        
        }
    }

    public void writeCoordinate(double x, double y, double z, double m) throws IOException {
        switch(mode) {
        case XY:
            json.writeCoordinate(x, y);
            break;
        case XYZ:
            json.writeCoordinate(x, y, z);
            break;
        case DEFAULT:
        case GEOMETRY:
        case XYZM:
        default:
            json.writeCoordinate(x, y, z, m);
            break;
        }
    }


    public void startRing() throws IOException {
        json.writeStartArray();
    }

    public void endRing() throws IOException {
        json.writeEndArray();
    }

}

