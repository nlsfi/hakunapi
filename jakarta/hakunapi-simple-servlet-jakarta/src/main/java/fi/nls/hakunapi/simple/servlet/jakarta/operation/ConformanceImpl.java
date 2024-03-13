package fi.nls.hakunapi.simple.servlet.jakarta.operation;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;

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
