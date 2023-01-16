package fi.nls.hakunapi.core.param;

import fi.nls.hakunapi.core.WFS3Service;
import fi.nls.hakunapi.core.request.GetFeatureRequest;

public interface GetFeatureParam extends APIParam {

    public void modify(WFS3Service service, GetFeatureRequest request, String value) throws IllegalArgumentException;

    /**
     * Priority will be used to order in which GetFeatureParams for one request should be processed
     */
    public default int priority() {
        return 0;
    }

}
