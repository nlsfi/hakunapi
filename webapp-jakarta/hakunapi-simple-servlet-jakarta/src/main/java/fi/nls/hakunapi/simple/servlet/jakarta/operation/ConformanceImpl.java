package fi.nls.hakunapi.simple.servlet.jakarta.operation;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;

import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.param.LangParam;
import fi.nls.hakunapi.core.schemas.ConformanceClasses;
import fi.nls.hakunapi.html.model.HTMLContext;

@Path("/conformance")
public class ConformanceImpl {

    @Inject
    private FeatureServiceConfig service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ConformanceClasses handle() {
        return new ConformanceClasses(service.getConformanceClasses());
    }

    /**
     * This resource has no localized content of its own, but it carries the
     * language so that its breadcrumbs and its language picker keep it: without
     * that, navigating here from a localized page and back would silently drop
     * the language.
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public HTMLContext<ConformanceClasses> handleHTML(
            @QueryParam("lang") @ParamClass(LangParam.class) String langParam,
            @Context HttpHeaders headers) {
        String basePath = service.getCurrentServerURL(headers::getHeaderString);
        String lang = OperationUtil.resolveLang(service, langParam, headers);
        return new HTMLContext<>(service, basePath,
                new ConformanceClasses(service.getConformanceClasses()), lang, service.getLanguages());
    }

}
