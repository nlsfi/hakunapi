package fi.nls.hakunapi.core.operation;

import java.util.Map;

import fi.nls.hakunapi.core.FeatureServiceConfig;

public interface DynamicResponseOperation {

    public Map<String, Class<?>> getResponsesByContentType(FeatureServiceConfig service);

}
