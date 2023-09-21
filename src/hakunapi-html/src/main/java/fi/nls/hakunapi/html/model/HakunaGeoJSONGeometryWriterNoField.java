package fi.nls.hakunapi.html.model;

import java.io.IOException;

import fi.nls.hakunapi.geojson.hakuna.HakunaGeoJSONGeometryWriter;
import fi.nls.hakunapi.geojson.hakuna.HakunaJsonWriter;

public class HakunaGeoJSONGeometryWriterNoField extends HakunaGeoJSONGeometryWriter {

    public HakunaGeoJSONGeometryWriterNoField(HakunaJsonWriter json) {
        super(json, null, true);
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
