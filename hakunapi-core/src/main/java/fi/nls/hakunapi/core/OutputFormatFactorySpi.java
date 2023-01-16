package fi.nls.hakunapi.core;

import java.util.Map;

public interface OutputFormatFactorySpi {

    public boolean canCreate(Map<String, String> params);
    public OutputFormat create(Map<String, String> params);

}
