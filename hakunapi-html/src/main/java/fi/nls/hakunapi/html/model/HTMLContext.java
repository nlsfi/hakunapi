package fi.nls.hakunapi.html.model;

import fi.nls.hakunapi.core.WFS3Service;

public class HTMLContext<T> {

    private final WFS3Service service;
    private final T model;

    public HTMLContext(WFS3Service service, T model) {
        this.service = service;
        this.model = model;
    }

    public WFS3Service getService() {
        return service;
    }

    public T getModel() {
        return model;
    }

}
