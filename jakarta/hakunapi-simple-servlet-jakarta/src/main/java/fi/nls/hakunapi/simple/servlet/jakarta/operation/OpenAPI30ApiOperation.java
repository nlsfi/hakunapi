package fi.nls.hakunapi.simple.servlet.jakarta.operation;

import java.util.List;

import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;

import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.operation.OperationImpl;
import fi.nls.hakunapi.html.model.HTMLContext;
import io.swagger.v3.oas.models.OpenAPI;

@Singleton
@Path("/")
public class OpenAPI30ApiOperation {

    private final FeatureServiceConfig service;
    private final OpenAPI30Generator api;

    public OpenAPI30ApiOperation(FeatureServiceConfig service, List<OperationImpl> opToImpl) {
        this.service = service;
        this.api = new OpenAPI30Generator(service, opToImpl);
    }

    @GET
    @Path("/api")
    @Produces("application/vnd.oai.openapi+json;version=3.0")
    public OpenAPI json(@Context HttpHeaders headers) {
        return api.create(headers);
    }

    @GET
    @Path("/api.json")
    @Produces("application/vnd.oai.openapi+json;version=3.0")
    public OpenAPI jsonExt(@Context HttpHeaders headers) {
        return api.create(headers);
    }
    
    @GET
    @Path("/api")
    @Produces(MediaType.TEXT_HTML)
    public HTMLContext<OpenAPI> html(@Context HttpHeaders headers) {
        String basePath = service.getCurrentServerURL(headers::getHeaderString);
        return new HTMLContext<>(service, basePath, new OpenAPI());
    }
    
    @GET
    @Path("/api.html")
    @Produces(MediaType.TEXT_HTML)
    public HTMLContext<OpenAPI> htmlExt(@Context HttpHeaders headers) {
        String basePath = service.getCurrentServerURL(headers::getHeaderString);
        return new HTMLContext<>(service, basePath, new OpenAPI());
    }

}
