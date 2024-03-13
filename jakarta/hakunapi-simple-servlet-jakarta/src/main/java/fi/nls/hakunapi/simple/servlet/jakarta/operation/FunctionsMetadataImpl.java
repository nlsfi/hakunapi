package fi.nls.hakunapi.simple.servlet.jakarta.operation;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.schemas.FunctionsContent;

@Path("/functions")
public class FunctionsMetadataImpl {

    @Inject
    private FeatureServiceConfig service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public FunctionsContent handle() {
        return service.getFunctions();
    }

}
