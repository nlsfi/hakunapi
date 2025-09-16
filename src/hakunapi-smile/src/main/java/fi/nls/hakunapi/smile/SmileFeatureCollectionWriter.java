package fi.nls.hakunapi.smile;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import fi.nls.hakunapi.core.FeatureCollectionWriter;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.SRIDCode;
import fi.nls.hakunapi.core.schemas.Link;

public class SmileFeatureCollectionWriter extends SmileFeatureWriterBase implements FeatureCollectionWriter {

    @Override
    public void init(OutputStream out, SRIDCode srid) throws Exception {
        super.init(out, srid);
        w.writeStartObject();
        w.writeFieldName(GeoJSONStrings.TYPE);
        w.writeString(GeoJSONStrings.FEATURE_COLLECTION);
        w.writeFieldName(GeoJSONStrings.FEATURES);
        w.writeStartArray();
    }

    @Override
    public void end(boolean timeStamp, List<Link> links, int numberReturned) throws Exception {
        // close "features":[ ]
        w.writeEndArray();
        if (timeStamp) {
            writeTimeStamp();
        }
        writeLinks(links);
        writeNumberReturned(numberReturned);
        // close FeatureCollection
        w.writeEndObject();
    }

    @Override
    public void startFeatureCollection(FeatureType ft, String layername) throws Exception {
        // NOP
    }

    @Override
    public void endFeatureCollection() throws Exception {
        // NOP
    }

    @Override
    public void startFeature(String fid) throws Exception {
        startFeature();
        w.writeString(fid);
    }

    @Override
    public void startFeature(long fid) throws Exception {
        startFeature();
        w.writeNumber(fid);
    }

    private void startFeature() throws IOException {
        w.writeStartObject();
        w.writeFieldName(GeoJSONStrings.TYPE);
        w.writeString(GeoJSONStrings.FEATURE);
        w.writeFieldName(GeoJSONStrings.ID);
    }

    @Override
    public void endFeature() throws Exception {
        closeProperties();
        if (cachedGeometry != null) {
            writeGeometry(null, cachedGeometry);
            cachedGeometry = null;
        }
        w.writeEndObject();
    }

}
