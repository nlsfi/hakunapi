package fi.nls.hakunapi.simple.servlet.operation;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import fi.nls.hakunapi.core.WFS3Service;
import fi.nls.hakunapi.core.schemas.FunctionsContent;

@Path("/functions")
public class FunctionsMetadataImpl {

    @Inject
    private WFS3Service service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public FunctionsContent handle() {
        return service.getFunctions();
    }

}
