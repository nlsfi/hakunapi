package fi.nls.hakunapi.simple.servlet.operation;

import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.WFS3Service;
import fi.nls.hakunapi.core.schemas.CollectionInfo;
import fi.nls.hakunapi.html.model.HTMLContext;

@Path("/collections")
public class CollectionMetadataImpl {

    @Inject
    private WFS3Service service;

    @GET
    @Path("/{collectionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public CollectionInfo handle(@PathParam("collectionId") String collectionId, @Context UriInfo uriInfo) {
        FeatureType ft = service.getCollection(collectionId);
        if (ft == null) {
            throw new NotFoundException("Unknown collection");
        }
        Map<String, String> queryParams = OperationUtil.getQueryParams(service, uriInfo);
        return CollectionMetadataUtil.toCollectionInfo(service, ft, queryParams);
    }
    
    @GET
    @Path("/{collectionId}")
    @Produces(MediaType.TEXT_HTML)
    public HTMLContext<CollectionInfo> handleHTML(@PathParam("collectionId") String collectionId, @Context UriInfo uriInfo) {
        return new HTMLContext<>(service, handle(collectionId, uriInfo));
    }

}
