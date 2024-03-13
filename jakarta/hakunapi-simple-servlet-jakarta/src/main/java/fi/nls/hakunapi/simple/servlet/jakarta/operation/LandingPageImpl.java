package fi.nls.hakunapi.simple.servlet.jakarta.operation;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;

import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.schemas.Root;
import fi.nls.hakunapi.html.model.HTMLContext;

@Path("/")
public class LandingPageImpl {

    @Inject
    private FeatureServiceConfig service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Root handleJSON(@Context UriInfo uriInfo, @Context HttpHeaders headers) {
        return handle(uriInfo, headers, MediaType.APPLICATION_JSON, MediaType.TEXT_HTML);
    }
    
    @GET
    @Produces(MediaType.TEXT_HTML)
    public HTMLContext<Root> handleHTML(@Context UriInfo uriInfo, @Context HttpHeaders headers) {
        String basePath = service.getCurrentServerURL(headers::getHeaderString);
        return new HTMLContext<>(service, basePath, handle(uriInfo, headers, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON));
    }
    
    private Root handle(UriInfo uriInfo, HttpHeaders headers, String contentType, String alternateContentType) {
        String title = service.getTitle();
        String description = service.getDescription();
        String url = service.getCurrentServerURL(headers::getHeaderString);
        String query = OperationUtil.getQuery(service, uriInfo);
        
        return new Root.Builder(title, description, url, query, contentType)
                .alternate(alternateContentType)
                .api("application/vnd.oai.openapi+json;version=3.0")
                .collections(MediaType.APPLICATION_JSON)
                .collections(MediaType.TEXT_HTML)
                .conformance(MediaType.APPLICATION_JSON)
                .conformance(MediaType.TEXT_HTML)
                .build();
    }

}
