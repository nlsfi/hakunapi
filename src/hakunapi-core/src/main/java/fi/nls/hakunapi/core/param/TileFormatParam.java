package fi.nls.hakunapi.core.param;

import fi.nls.hakunapi.core.FeatureServiceConfig;
import io.swagger.v3.oas.models.parameters.Parameter;

public class TileFormatParam implements APIParam {

    @Override
    public String getParamName() {
        return "f";
    }

    @Override
    public Parameter toParameter(FeatureServiceConfig service) {
        return null;
    }

}
