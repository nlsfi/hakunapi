package fi.nls.hakunapi.core.param;

import fi.nls.hakunapi.core.FeatureServiceConfig;
import io.swagger.v3.oas.models.parameters.Parameter;

public interface APIParam {

    public String getParamName();
    public Parameter toParameter(FeatureServiceConfig service);

    /**
     * Whether or not this parameter is common throughout the
     * whole API. Parameters that are specific to a certain path
     * may be marked as common, but probably shouldn't. If the
     * parameter is used in a number of different paths it should
     * be marked as common.
     * 
     * Technically this means that common parameters appear in the
     * components section of the API document
     * 
     * @return true if parameter is common, false if not
     */
    public default boolean isCommon() {
        return true;
    }

}
