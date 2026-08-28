package fi.nls.hakunapi.simple.servlet.jakarta.operation;

import java.util.List;
import java.util.Map;

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
import fi.nls.hakunapi.core.i18n.LangNegotiation;
import fi.nls.hakunapi.core.param.LangParam;
import fi.nls.hakunapi.core.schemas.Root;
import fi.nls.hakunapi.core.util.Links;
import fi.nls.hakunapi.core.util.U;
import fi.nls.hakunapi.html.model.HTMLContext;

@Path("/")
public class LandingPageImpl {

    @Inject
    private FeatureServiceConfig service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ResponseClass(Root.class)
    public Response handleJSON(
            @QueryParam("lang") @ParamClass(LangParam.class) String langParam,
            @Context UriInfo uriInfo,
            @Context HttpHeaders headers) {
        Localized localized = localize(langParam, uriInfo, headers, MediaType.APPLICATION_JSON,
                MediaType.TEXT_HTML);

        Response.ResponseBuilder rb = Response.ok(localized.root);
        if (localized.lang != null) {
            rb.header("Content-Language", localized.lang);
        }
        return rb.build();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    // Response rather than HTMLContext so that Content-Language can be set;
    // OpenAPI30Generator reads the schema off ResponseClass when it sees Response
    @ResponseClass(Root.class)
    public Response handleHTML(
            @QueryParam("lang") @ParamClass(LangParam.class) String langParam,
            @Context UriInfo uriInfo,
            @Context HttpHeaders headers) {
        Localized localized = localize(langParam, uriInfo, headers, MediaType.TEXT_HTML,
                MediaType.APPLICATION_JSON);

        String basePath = service.getCurrentServerURL(headers::getHeaderString);
        HTMLContext<Root> html = new HTMLContext<>(service, basePath, localized.root, localized.lang,
                localized.available);

        // GenericEntity preserves HTMLContext<Root> as a ParameterizedType.
        // HTMLMessageBodyWriter picks the template off that type argument, so a bare
        // Response.ok(html) erases it and no writer matches.
        Response.ResponseBuilder rb = Response.ok(new GenericEntity<HTMLContext<Root>>(html) {
        });
        if (localized.lang != null) {
            rb.header("Content-Language", localized.lang);
        }
        return rb.build();
    }

    /**
     * Negotiates the language and builds the landing page.
     *
     * The landing page advertises the service's declared languages only. A
     * collection's schema-file languages are independent of locale=, so a
     * service may localize a collection without localizing its landing page,
     * and each resource advertises what it can actually serve.
     */
    private Localized localize(String langParam, UriInfo uriInfo, HttpHeaders headers, String contentType,
            String alternateContentType) {
        List<String> available = service.getLanguages();
        String lang = LangNegotiation.resolve(available, langParam, OperationUtil.getAcceptableLanguages(headers));

        FeatureServiceConfig localizedService = service.localized(lang);

        String title = localizedService.getTitle();
        String description = localizedService.getDescription();
        String url = service.getCurrentServerURL(headers::getHeaderString);

        // lang is added to the query string rather than to
        // OperationUtil.getQueryParams, which is shared with feature-item
        // operations where lang means nothing. Root.Builder appends query to
        // every link it builds, so this propagates lang throughout.
        Map<String, String> queryParams = OperationUtil.getQueryParams(localizedService, uriInfo);
        if (lang != null) {
            queryParams.put(LangParam.PARAM_NAME, lang);
        }
        String query = U.toQuery(queryParams);

        Root root = new Root.Builder(title, description, url, query, contentType, lang)
                .alternate(alternateContentType)
                .api("application/vnd.oai.openapi+json;version=3.0")
                .collections(MediaType.APPLICATION_JSON)
                .collections(MediaType.TEXT_HTML)
                .conformance(MediaType.APPLICATION_JSON)
                .conformance(MediaType.TEXT_HTML)
                .additionalLinks(localizedService.getAdditionalLinks())
                .additionalLinks(Links.getAlternateLangLinks(url + "/", queryParams, contentType, available, lang))
                .build();

        return new Localized(root, lang, available);
    }


    private static class Localized {
        private final Root root;
        private final String lang;
        private final List<String> available;

        Localized(Root root, String lang, List<String> available) {
            this.root = root;
            this.lang = lang;
            this.available = available;
        }
    }

}
