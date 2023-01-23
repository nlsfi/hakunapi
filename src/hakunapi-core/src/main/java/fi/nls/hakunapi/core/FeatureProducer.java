package fi.nls.hakunapi.core;

import fi.nls.hakunapi.core.request.GetFeatureCollection;
import fi.nls.hakunapi.core.request.GetFeatureRequest;

public interface FeatureProducer {

    public int getNumberMatched(GetFeatureRequest request, GetFeatureCollection col) throws Exception;
    public FeatureStream getFeatures(GetFeatureRequest request, GetFeatureCollection col) throws Exception;

}
