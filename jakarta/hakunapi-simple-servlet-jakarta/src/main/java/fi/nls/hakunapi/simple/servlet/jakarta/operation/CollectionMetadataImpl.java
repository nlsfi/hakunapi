package fi.nls.hakunapi.simple.servlet.jakarta.operation;

import java.util.Map;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.schemas.CollectionInfo;
import fi.nls.hakunapi.html.model.HTMLContext;

@Path("/collections")
public class CollectionMetadataImpl {

    @Inject
    private FeatureServiceConfig service;

    @GET
    @Path("/{collectionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public CollectionInfo handle(
            @PathParam("collectionId") String collectionId,
            @Context UriInfo uriInfo,
            @Context HttpHeaders headers) {
        FeatureType ft = service.getCollection(collectionId);
        if (ft == null) {
            throw new NotFoundException("Unknown collection");
        }
        Map<String, String> queryParams = OperationUtil.getQueryParams(service, uriInfo);
        return CollectionMetadataUtil.toCollectionInfo(headers, service, ft, queryParams);
    }
    
    @GET
    @Path("/{collectionId}")
    @Produces(MediaType.TEXT_HTML)
    public HTMLContext<CollectionInfo> handleHTML(
            @PathParam("collectionId") String collectionId,
            @Context UriInfo uriInfo,
            @Context HttpHeaders headers) {
        String basePath = service.getCurrentServerURL(headers::getHeaderString);
        return new HTMLContext<>(service, basePath, handle(collectionId, uriInfo, headers));
    }

}
