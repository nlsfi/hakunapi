package fi.nls.hakunapi.simple.servlet.javax.operation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.MetadataFormat;
import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.schemas.CollectionInfo;
import fi.nls.hakunapi.core.schemas.CollectionsContent;
import fi.nls.hakunapi.core.schemas.Link;
import fi.nls.hakunapi.core.util.Links;
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

        String path = service.getCurrentServerURL() + "/collections";
        List<Link> links = new ArrayList<>();
        links.add(Links.getSelfLink(path, queryParams, contentType));
        links.addAll(getAlternateLinks(path, queryParams, contentType));

        List<CollectionInfo> collections = new ArrayList<>();
        for (FeatureType ft : service.getCollections()) {
            CollectionInfo info = CollectionMetadataUtil.toCollectionInfo(service, ft, queryParams);
            collections.add(info);
        }

        return new CollectionsContent(links, collections);
    }

    private List<Link> getAlternateLinks(String path, Map<String, String> queryParams, String contentType) {
        return service.getMetadataFormats().stream()
                .filter(it -> !it.contentTypes.contains(contentType))
                .map(it -> toAlternateLink(path, queryParams, it))
                .collect(Collectors.toList());
    }

    private Link toAlternateLink(String path, Map<String, String> queryParams, MetadataFormat format) {
        String mimeTypeHuman = format.id;
        String mimeType;
        if (format == MetadataFormat.JSON) {
            mimeType = MediaType.APPLICATION_JSON;
        } else { // if (format == MetadataFormat.JSON) {
            mimeType = MediaType.TEXT_HTML;
        }
        return Links.getAlternateLink(path, queryParams, mimeType, mimeTypeHuman);
    }

}
