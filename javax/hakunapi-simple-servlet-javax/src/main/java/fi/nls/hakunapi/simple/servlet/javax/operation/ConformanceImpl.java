package fi.nls.hakunapi.simple.servlet.javax.operation;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.schemas.ConformanceClasses;
import fi.nls.hakunapi.html.model.HTMLContext;

@Path("/conformance")
public class ConformanceImpl {

    @Inject
    private FeatureServiceConfig service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ConformanceClasses handle() {
        return new ConformanceClasses(service.getConformanceClasses());
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public HTMLContext<ConformanceClasses> handleHTML(@Context HttpHeaders headers) {
        String basePath = service.getCurrentServerURL(headers::getHeaderString);
        return new HTMLContext<>(service, basePath, new ConformanceClasses(service.getConformanceClasses()));
    }

}
