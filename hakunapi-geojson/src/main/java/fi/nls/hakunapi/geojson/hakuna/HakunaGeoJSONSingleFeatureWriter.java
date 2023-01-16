package fi.nls.hakunapi.geojson.hakuna;

import java.util.List;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.SingleFeatureWriter;
import fi.nls.hakunapi.core.schemas.Link;

public class HakunaGeoJSONSingleFeatureWriter extends HakunaGeoJSONWriter implements SingleFeatureWriter {

    private void startFeature(FeatureType ft) throws Exception {
        json.writeStartObject();
        json.writeFieldName(HakunaGeoJSON.TYPE);
        json.writeStringUnsafe(HakunaGeoJSON.FEATURE, 0, HakunaGeoJSON.FEATURE.length);
        json.writeFieldName(HakunaGeoJSON.ID);
    }
    
    @Override
    public void startFeature(FeatureType ft, String layername, String fid)
            throws Exception {
        startFeature(ft);
        json.writeString(fid);
    }

    @Override
    public void startFeature(FeatureType ft, String layername, long fid)
            throws Exception {
        startFeature(ft);
        json.writeNumber(fid);
    }
    
    @Override
    public void endFeature() throws Exception {
        closeProperties();
        if (geometryCached) {
            writeGeometry(null, cachedGeometry);
            geometryCached = false;
            cachedGeometry = null;
        }
    }

    @Override
    public void end(boolean timeStamp, List<Link> links, int numberReturned) throws Exception {
        if (timeStamp) {
            writeTimeStamp();
        }
        writeLinks(links);
    }

}
