package fi.nls.hakunapi.core.param;

import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import io.swagger.v3.oas.models.parameters.Parameter;

public class SortbyParam implements GetFeatureParam {

    public static final String PARAM_NAME = "sortby";

    @Override
    public String getParamName() {
        return PARAM_NAME;
    }

    @Override
    public Parameter toParameter(FeatureServiceConfig service) {
        // Currently hidden parameter -- waiting for Sorting extension
        return null;
    }

    @Override
    public void modify(FeatureServiceConfig service, GetFeatureRequest request, String value)
            throws IllegalArgumentException {
        request.getCollections().forEach(c -> c.setOrderBy(c.getFt().getDefaultOrderBy()));
    }

    @Override
    public int priority() {
        return -1; // Run before next/offset params
    }

}
