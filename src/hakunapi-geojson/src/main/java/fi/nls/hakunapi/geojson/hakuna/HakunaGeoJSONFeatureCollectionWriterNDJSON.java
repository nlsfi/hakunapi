package fi.nls.hakunapi.geojson.hakuna;

import java.io.IOException;
import java.util.List;

import fi.nls.hakunapi.core.schemas.Link;

public class HakunaGeoJSONFeatureCollectionWriterNDJSON extends HakunaGeoJSONFeatureCollectionWriter {
    
    
    @Override
    protected void writeFeatureCollection() {
        // Don't write {"type": "FeatureCollection", "features": [
    }
    
    @Override
    public void end(boolean timeStamp, List<Link> links, int numberReturned) throws IOException {
        // Don't close features array or the FeatureCollection object
    }
    
    @Override
    public void endFeature() throws Exception {
        super.endFeature();
        json.writeRecordSeparator((byte) '\n');
    }
    
}
