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
import fi.nls.hakunapi.core.extension.ApiExtension;
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
        Map<String, String> queryParams = OperationUtil.getQueryParams(service, uriInfo);
        FeatureType ft = service.getCollection(collectionId);
        if (ft != null) {
            return CollectionMetadataUtil.toCollectionInfo(headers, service, ft, queryParams);
        }
        CollectionInfo extra = findExtraCollection(headers, collectionId, queryParams);
        if (extra != null) {
            return extra;
        }
        throw new NotFoundException("Unknown collection");
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

    private CollectionInfo findExtraCollection(HttpHeaders headers, String collectionId,
            Map<String, String> queryParams) {
        String baseUrl = service.getCurrentServerURL(headers::getHeaderString);
        for (ApiExtension extension : service.getApiExtensions()) {
            for (CollectionInfo info : extension.getCollections(baseUrl, queryParams)) {
                if (collectionId.equals(info.getId())) {
                    return info;
                }
            }
        }
        return null;
    }

}
