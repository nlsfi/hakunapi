package fi.nls.hakunapi.core;

import java.util.Map;
import java.util.ServiceLoader;

public class OutputFormatProvider {

    private static final ServiceLoader<OutputFormatFactorySpi> LOADER = ServiceLoader.load(OutputFormatFactorySpi.class);

    public static OutputFormat getOutputFormat(Map<String, String> params) {
        LOADER.reload();
        for (OutputFormatFactorySpi factory : LOADER) {
            if (factory.canCreate(params)) {
                return factory.create(params);
            }
        }
        return null;
    }

}
