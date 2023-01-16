package fi.nls.hakunapi.esbulk;

import java.util.Map;

import fi.nls.hakunapi.core.OutputFormat;
import fi.nls.hakunapi.core.OutputFormatFactorySpi;

public class OutputFormatFactoryESbulk implements OutputFormatFactorySpi {

    @Override
    public boolean canCreate(Map<String, String> params) {
        return OutputFormatESbulk.ID.equals(params.get("type"));
    }

    @Override
    public OutputFormat create(Map<String, String> params) {
        return OutputFormatESbulk.INSTANCE;
    }

}
