package fi.nls.hakunapi.simple.servlet.operation;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import fi.nls.hakunapi.core.WFS3Service;
import fi.nls.hakunapi.core.schemas.ConformanceClasses;
import fi.nls.hakunapi.html.model.HTMLContext;

@Path("/conformance")
public class ConformanceImpl {

    @Inject
    private WFS3Service service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ConformanceClasses handle() {
        return new ConformanceClasses(service.getConformanceClasses());
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public HTMLContext<ConformanceClasses> handleHTML() {
        return new HTMLContext<>(service, new ConformanceClasses(service.getConformanceClasses()));
    }

}
