package fi.nls.hakunapi.simple.servlet.operation.param;

import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.param.APIParam;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.PathParameter;

public class SimplePathParam implements APIParam {

    private final String name;

    public SimplePathParam(String name) {
        this.name = name;
    }

    @Override
    public Parameter toParameter(FeatureServiceConfig service) {
        return new PathParameter()
                .name(getParamName())
                .required(true)
                .schema(new StringSchema());
    }

    @Override
    public boolean isCommon() {
        return false;
    }

    @Override
    public String getParamName() {
        return name;
    }

}
