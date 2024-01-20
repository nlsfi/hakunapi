package fi.nls.hakunapi.simple.servlet.javax.operation;

import java.util.List;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.operation.OperationImpl;
import fi.nls.hakunapi.html.model.HTMLContext;
import fi.nls.hakunapi.simple.servlet.javax.MediaTypes;
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
    @Produces(MediaTypes.APPLICATION_OPENAPI_V3)
    public OpenAPI json(@Context HttpHeaders headers) {
        return api.create(headers);
    }

    @GET
    @Path("/api.json")
    @Produces(MediaTypes.APPLICATION_OPENAPI_V3)
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
