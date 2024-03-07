package fi.nls.hakunapi.geojson.hakuna;

import java.io.IOException;

import fi.nls.hakunapi.core.FloatingPointFormatter;
import fi.nls.hakunapi.core.geom.HakunaGeometryDimension;

public class HakunaGeoJSONGeometryXYZMWriter extends HakunaGeoJSONGeometryWriter {

    private final HakunaGeometryDimension mode;

    public HakunaGeoJSONGeometryXYZMWriter(HakunaJsonWriter json, FloatingPointFormatter formatter, byte[] fieldName,
            boolean lonLat,
            HakunaGeometryDimension mode) {
        super(json, formatter, fieldName, lonLat);
        this.mode = mode;
    }

    @Override
    public void writeCoordinate(double x, double y, double z) throws IOException {
        switch(mode) {
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
        switch(mode) {
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
