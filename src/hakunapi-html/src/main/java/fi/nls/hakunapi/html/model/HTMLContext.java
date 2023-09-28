package fi.nls.hakunapi.html.model;

import fi.nls.hakunapi.core.FeatureServiceConfig;

public class HTMLContext<T> {

    private final FeatureServiceConfig service;
    private final String basePath;
    private final String basePathTrailingSlash;
    private final T model;

    public HTMLContext(FeatureServiceConfig service, String basePath, T model) {
        this.service = service;
        this.basePath = basePath;
        this.basePathTrailingSlash = basePath + (basePath.endsWith("/") ? "" : "/");
        this.model = model;
    }

    public FeatureServiceConfig getService() {
        return service;
    }

    public String getBasePath() {
        return basePath;
    }

    public String getBasePathTrailingSlash() {
        return basePathTrailingSlash;
    }

    public T getModel() {
        return model;
    }

}
