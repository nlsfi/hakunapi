package fi.nls.hakunapi.simple.servlet.jakarta.operation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.MetadataFormat;
import fi.nls.hakunapi.core.i18n.LangNegotiation;
import fi.nls.hakunapi.core.param.LangParam;
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
    @ResponseClass(CollectionsContent.class)
    public Response handleJSON(
            @QueryParam("lang") @ParamClass(LangParam.class) String langParam,
            @Context UriInfo uriInfo,
            @Context HttpHeaders headers) {
        Localized localized = localize(langParam, uriInfo, headers, MediaType.APPLICATION_JSON);

        Response.ResponseBuilder rb = Response.ok(localized.content);
        if (localized.lang != null) {
            rb.header("Content-Language", localized.lang);
        }
        return rb.build();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    // Response rather than HTMLContext so that Content-Language can be set;
    // OpenAPI30Generator reads the schema off ResponseClass when it sees Response
    @ResponseClass(CollectionsContent.class)
    public Response handleHTML(
            @QueryParam("lang") @ParamClass(LangParam.class) String langParam,
            @Context UriInfo uriInfo,
            @Context HttpHeaders headers) {
        Localized localized = localize(langParam, uriInfo, headers, MediaType.TEXT_HTML);

        String basePath = service.getCurrentServerURL(headers::getHeaderString);
        HTMLContext<CollectionsContent> html = new HTMLContext<>(service, basePath, localized.content,
                localized.lang, localized.available);

        // GenericEntity preserves HTMLContext<CollectionsContent> as a ParameterizedType.
        // HTMLMessageBodyWriter picks the template off that type argument, so a bare
        // Response.ok(html) erases it and no writer matches.
        Response.ResponseBuilder rb = Response.ok(
                new GenericEntity<HTMLContext<CollectionsContent>>(html) {
                });
        if (localized.lang != null) {
            rb.header("Content-Language", localized.lang);
        }
        return rb.build();
    }

    /**
     * Negotiates the language and builds the collections listing.
     *
     * Kept internal so that each public method builds its own Response and can
     * set Content-Language.
     */
    private Localized localize(String langParam, UriInfo uriInfo, HttpHeaders headers, String contentType) {
        List<String> available = service.getLanguages();
        String lang = LangNegotiation.resolve(available, langParam, OperationUtil.getAcceptableLanguages(headers));

        FeatureServiceConfig localizedService = service.localized(lang);
        Map<String, String> queryParams = OperationUtil.getQueryParams(localizedService, uriInfo);
        if (lang != null) {
            queryParams.put(LangParam.PARAM_NAME, lang);
        }

        String path = service.getCurrentServerURL(headers::getHeaderString) + "/collections";
        List<Link> links = new ArrayList<>();
        links.add(Links.getSelfLink(path, queryParams, contentType, lang));
        links.addAll(getAlternateLinks(path, queryParams, contentType));
        links.addAll(Links.getAlternateLangLinks(path, queryParams, contentType, available, lang));

        List<CollectionInfo> collections = new ArrayList<>();
        for (FeatureType ft : localizedService.getCollections()) {
            collections.add(CollectionMetadataUtil.toCollectionInfo(headers, localizedService, ft, queryParams, lang));
        }

        return new Localized(new CollectionsContent(links, collections), lang, available);
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

    private static class Localized {
        private final CollectionsContent content;
        private final String lang;
        private final List<String> available;

        Localized(CollectionsContent content, String lang, List<String> available) {
            this.content = content;
            this.lang = lang;
            this.available = available;
        }
    }

}
