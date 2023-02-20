package fi.nls.hakunapi.html.model;

import fi.nls.hakunapi.core.FeatureServiceConfig;

public class HTMLContext<T> {

    private final FeatureServiceConfig service;
    private final T model;

    public HTMLContext(FeatureServiceConfig service, T model) {
        this.service = service;
        this.model = model;
    }

    public FeatureServiceConfig getService() {
        return service;
    }

    public T getModel() {
        return model;
    }

}
