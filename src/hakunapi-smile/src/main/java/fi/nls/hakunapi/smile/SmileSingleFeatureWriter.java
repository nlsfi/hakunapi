package fi.nls.hakunapi.smile;

import java.io.IOException;
import java.util.List;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.SingleFeatureWriter;
import fi.nls.hakunapi.core.schemas.Link;

public class SmileSingleFeatureWriter extends SmileFeatureWriterBase implements SingleFeatureWriter {

    @Override
    public void startFeature(FeatureType ft, String layername, String fid) throws Exception {
        startFeature();
        w.writeString(fid);
    }

    @Override
    public void startFeature(FeatureType ft, String layername, long fid) throws Exception {
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
    }

    @Override
    public void end(boolean timeStamp, List<Link> links, int numberReturned) throws Exception {
        if (timeStamp) {
            writeTimeStamp();
        }
        writeLinks(links);
        w.writeEndObject();
    }

}
