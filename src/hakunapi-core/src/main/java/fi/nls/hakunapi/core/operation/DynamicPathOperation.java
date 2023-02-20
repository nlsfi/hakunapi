package fi.nls.hakunapi.core.operation;

import java.util.List;

import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.param.APIParam;

public interface DynamicPathOperation {

    public List<String> getValidPaths(FeatureServiceConfig service);
    public List<? extends APIParam> getParameters(String path, FeatureServiceConfig service);

}
