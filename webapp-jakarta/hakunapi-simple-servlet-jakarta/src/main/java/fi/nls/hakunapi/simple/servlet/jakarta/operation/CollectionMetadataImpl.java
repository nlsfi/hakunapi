package fi.nls.hakunapi.simple.servlet.jakarta.operation;

import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
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
import fi.nls.hakunapi.core.i18n.LangNegotiation;
import fi.nls.hakunapi.core.param.LangParam;
import fi.nls.hakunapi.core.schemas.CollectionInfo;
import fi.nls.hakunapi.core.schemas.Link;
import fi.nls.hakunapi.core.util.Links;
import fi.nls.hakunapi.html.model.HTMLContext;

@Path("/collections")
public class CollectionMetadataImpl {

    @Inject
    private FeatureServiceConfig service;

    @GET
    @Path("/{collectionId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ResponseClass(CollectionInfo.class)
    public Response handle(
            @PathParam("collectionId") String collectionId,
            @QueryParam("lang") @ParamClass(LangParam.class) String langParam,
            @Context UriInfo uriInfo,
            @Context HttpHeaders headers) {
        Localized localized = localize(collectionId, langParam, uriInfo, headers,
                MediaType.APPLICATION_JSON);

        Response.ResponseBuilder rb = Response.ok(localized.info);
        if (localized.lang != null) {
            rb.header("Content-Language", localized.lang);
        }
        return rb.build();
    }

    @GET
    @Path("/{collectionId}")
    @Produces(MediaType.TEXT_HTML)
    // Response rather than HTMLContext so that Content-Language can be set;
    // OpenAPI30Generator reads the schema off ResponseClass when it sees Response
    @ResponseClass(CollectionInfo.class)
    public Response handleHTML(
            @PathParam("collectionId") String collectionId,
            @QueryParam("lang") @ParamClass(LangParam.class) String langParam,
            @Context UriInfo uriInfo,
            @Context HttpHeaders headers) {
        Localized localized = localize(collectionId, langParam, uriInfo, headers, MediaType.TEXT_HTML);

        String basePath = service.getCurrentServerURL(headers::getHeaderString);
        HTMLContext<CollectionInfo> html = new HTMLContext<>(service, basePath, localized.info,
                localized.lang, localized.available);

        // GenericEntity preserves HTMLContext<CollectionInfo> as a ParameterizedType.
        // HTMLMessageBodyWriter picks the template off that type argument, so a bare
        // Response.ok(html) erases it and no writer matches.
        Response.ResponseBuilder rb = Response.ok(
                new GenericEntity<HTMLContext<CollectionInfo>>(html) {
                });
        if (localized.lang != null) {
            rb.header("Content-Language", localized.lang);
        }
        return rb.build();
    }

    /**
     * Negotiates the language and builds the CollectionInfo.
     *
     * Kept internal so that each public method can build its own Response and
     * set Content-Language; handleHTML used to delegate to handle(), which
     * returned a bare CollectionInfo with nowhere to hang a header.
     */
    private Localized localize(String collectionId, String langParam, UriInfo uriInfo, HttpHeaders headers,
            String contentType) {
        FeatureType ft = service.getCollection(collectionId);
        if (ft == null) {
            throw new NotFoundException("Unknown collection");
        }

        List<String> available = service.getLanguages();
        String lang = LangNegotiation.resolve(available, langParam, OperationUtil.getAcceptableLanguages(headers));

        FeatureServiceConfig localizedService = service.localized(lang);
        FeatureType localizedFt = localizedService.getCollection(collectionId);

        Map<String, String> queryParams = OperationUtil.getQueryParams(localizedService, uriInfo);

        String path = service.getCurrentServerURL(headers::getHeaderString) + "/collections/" + collectionId;
        List<Link> alternates = Links.getAlternateLangLinks(path, queryParams, contentType, available, lang);

        CollectionInfo info = CollectionMetadataUtil.toCollectionInfo(headers, localizedService, localizedFt,
                queryParams, lang, alternates);

        return new Localized(info, lang, available);
    }


    private static class Localized {
        private final CollectionInfo info;
        private final String lang;
        private final List<String> available;

        Localized(CollectionInfo info, String lang, List<String> available) {
            this.info = info;
            this.lang = lang;
            this.available = available;
        }
    }

}
