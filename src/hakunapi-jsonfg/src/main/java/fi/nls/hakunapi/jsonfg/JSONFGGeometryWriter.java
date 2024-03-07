package fi.nls.hakunapi.jsonfg;

import java.io.IOException;

import fi.nls.hakunapi.core.FloatingPointFormatter;
import fi.nls.hakunapi.core.geom.HakunaGeometryDimension;
import fi.nls.hakunapi.core.geom.HakunaGeometryType;
import fi.nls.hakunapi.geojson.hakuna.HakunaGeoJSONGeometryWriter;
import fi.nls.hakunapi.geojson.hakuna.HakunaJsonWriter;

public class JSONFGGeometryWriter extends HakunaGeoJSONGeometryWriter {

    int srid;
    protected HakunaGeometryDimension mode;

    public JSONFGGeometryWriter(HakunaJsonWriter json, FloatingPointFormatter formatter, byte[] fieldName, boolean lonLat, HakunaGeometryDimension mode) {
        super(json, formatter, fieldName, lonLat);
        this.mode = mode;
    }

    @Override
    public void init(HakunaGeometryType type, int srid, int dimension) throws IOException {
        super.init(type, srid, dimension);
        this.srid = srid;
    }

    public void end() throws IOException {
        json.writeEndObject();
    }

    public int getSrid() {
        return srid;
    }

    @Override
    public void writeCoordinate(double x, double y, double z) throws IOException {
        switch (mode) {
        case XY:
            super.writeCoordinate(x, y);
            break;
        default:
            super.writeCoordinate(x, y, z);
            break;

        }
    }

    @Override
    public void writeCoordinate(double x, double y, double z, double m) throws IOException {
        switch (mode) {
        case XY:
            super.writeCoordinate(x, y);
            break;
        case XYZ:
            super.writeCoordinate(x, y, z);
            break;
        default:
            super.writeCoordinate(x, y, z, m);
            break;
        }
    }

}
