package fi.nls.hakunapi.core;

import java.util.Optional;
import java.util.ServiceLoader;

public class CRSRegistryProvider {

    private static final ServiceLoader<CRSRegistry> LOADER = ServiceLoader.load(CRSRegistry.class);

    public static Optional<CRSRegistry> getCRSRegistry() {
        LOADER.reload();
        return LOADER.findFirst();
    }

}
