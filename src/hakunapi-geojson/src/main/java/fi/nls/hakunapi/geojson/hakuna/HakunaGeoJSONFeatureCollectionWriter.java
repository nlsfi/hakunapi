package fi.nls.hakunapi.geojson.hakuna;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import fi.nls.hakunapi.core.FeatureCollectionWriter;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.FloatingPointFormatter;
import fi.nls.hakunapi.core.schemas.Link;

public class HakunaGeoJSONFeatureCollectionWriter extends HakunaGeoJSONWriter implements FeatureCollectionWriter {

    @Override
    public void init(OutputStream out, FloatingPointFormatter formatter,int srid) throws IOException {
        super.init(out, formatter, srid);
        writeFeatureCollection();
    }
    
    protected void writeFeatureCollection() throws IOException {
        json.writeStartObject();
        json.writeFieldName(HakunaGeoJSON.TYPE);
        json.writeStringUnsafe(HakunaGeoJSON.FEATURE_COLLECTION, 0, HakunaGeoJSON.FEATURE_COLLECTION.length);
        json.writeFieldName(HakunaGeoJSON.FEATURES);
        json.writeStartArray();
    }

    @Override
    public void startFeatureCollection(FeatureType ft, String layername) throws Exception {
        // NOP
    }

    @Override
    public void endFeatureCollection() throws IOException {
        // NOP
    }

    @Override
    public void end(boolean timeStamp, List<Link> links, int numberReturned) throws IOException {
        // close "features":[ ]
        json.writeEndArray();
        if (timeStamp) {
            writeTimeStamp();
        }
        writeLinks(links);
        writeNumberReturned(numberReturned);
    }

    @Override
    public void startFeature(String fid) throws IOException {
        startFeature();
        json.writeString(fid);
    }

    @Override
    public void startFeature(long fid) throws IOException {
        startFeature();
        json.writeNumber(fid);
    }

    private void startFeature() throws IOException {
        json.writeStartObject();
        json.writeFieldName(HakunaGeoJSON.TYPE);
        json.writeStringUnsafe(HakunaGeoJSON.FEATURE, 0, HakunaGeoJSON.FEATURE.length);
        json.writeFieldName(HakunaGeoJSON.ID);
    }

    @Override
    public void endFeature() throws Exception {
        closeProperties();
        if (geometryCached) {
            writeGeometry(null, cachedGeometry);
            geometryCached = false;
            cachedGeometry = null;
        }
        json.writeEndObject();
    }

}
