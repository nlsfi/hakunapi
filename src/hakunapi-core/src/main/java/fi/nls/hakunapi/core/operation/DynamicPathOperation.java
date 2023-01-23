package fi.nls.hakunapi.core.operation;

import java.util.List;

import fi.nls.hakunapi.core.WFS3Service;
import fi.nls.hakunapi.core.param.APIParam;

public interface DynamicPathOperation {

    public List<String> getValidPaths(WFS3Service service);
    public List<? extends APIParam> getParameters(String path, WFS3Service service);

}
