package fi.nls.hakunapi.core.operation;

import java.util.Map;

import fi.nls.hakunapi.core.WFS3Service;

public interface DynamicResponseOperation {

    public Map<String, Class<?>> getResponsesByContentType(WFS3Service service);

}
