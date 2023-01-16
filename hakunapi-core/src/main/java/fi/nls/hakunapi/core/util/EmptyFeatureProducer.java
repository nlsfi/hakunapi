package fi.nls.hakunapi.core.util;

import fi.nls.hakunapi.core.FeatureProducer;
import fi.nls.hakunapi.core.FeatureStream;
import fi.nls.hakunapi.core.request.GetFeatureCollection;
import fi.nls.hakunapi.core.request.GetFeatureRequest;

public class EmptyFeatureProducer implements FeatureProducer {

    @Override
    public int getNumberMatched(GetFeatureRequest request, GetFeatureCollection col) throws Exception {
        return 0;
    }

    @Override
    public FeatureStream getFeatures(GetFeatureRequest request, GetFeatureCollection col) throws Exception {
        return new EmptyFeatureStream();
    }

}
