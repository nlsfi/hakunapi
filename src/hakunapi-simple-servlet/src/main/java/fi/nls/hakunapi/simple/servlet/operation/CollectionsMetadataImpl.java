package fi.nls.hakunapi.simple.servlet.operation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.schemas.CollectionInfo;
import fi.nls.hakunapi.core.schemas.CollectionsContent;
import fi.nls.hakunapi.core.schemas.Link;
import fi.nls.hakunapi.core.util.U;
import fi.nls.hakunapi.html.model.HTMLContext;

@Path("/collections")
public class CollectionsMetadataImpl {

    @Inject
    private FeatureServiceConfig service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public CollectionsContent handleJSON(@Context UriInfo uriInfo) {
        return handle(uriInfo, MediaType.APPLICATION_JSON);
    }
    
    @GET
    @Produces(MediaType.TEXT_HTML)
    public HTMLContext<CollectionsContent> handleHTML(@Context UriInfo uriInfo) {
        return new HTMLContext<>(service, handle(uriInfo, MediaType.TEXT_HTML));
    }
    
    private CollectionsContent handle(UriInfo uriInfo, String contentType) {
        Map<String, String> queryParams = OperationUtil.getQueryParams(service, uriInfo);
        String query = U.toQuery(queryParams);
        
        Link self = getSelfLink(query, contentType);
        List<Link> links = Collections.singletonList(self);

        List<CollectionInfo> collections = new ArrayList<>();
        for (FeatureType ft : service.getCollections()) {
            CollectionInfo info = CollectionMetadataUtil.toCollectionInfo(service, ft, queryParams);
            collections.add(info);
        }

        return new CollectionsContent(links, collections);
    }

    private Link getSelfLink(String query, String contentType) {
        String href = service.getCurrentServerURL() + "/collections" + query;
        String rel = "self";
        String type = contentType;
        return new Link(href, rel, type, "This document");
    }

}
