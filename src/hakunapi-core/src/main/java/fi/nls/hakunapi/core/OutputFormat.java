package fi.nls.hakunapi.core;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import fi.nls.hakunapi.core.request.GetFeatureRequest;
import fi.nls.hakunapi.core.schemas.Link;

public interface OutputFormat {
    
    public String getId();

    public String getMediaMainType();
    public String getMediaSubType();
    public default Map<String, String> getMimeParameters() {
        return null;
    }

    public String getMimeType();

    public FeatureCollectionWriter getFeatureCollectionWriter();
    public SingleFeatureWriter getSingleFeatureWriter();
    public default Map<String, String> getResponseHeaders(GetFeatureRequest request, List<Link> links) {
        return Collections.singletonMap("Content-Type", getMimeType());
    }

}
