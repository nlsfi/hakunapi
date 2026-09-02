package fi.nls.hakunapi.simple.servlet.jakarta.operation;

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

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.param.LangParam;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.schemas.Queryables;
import fi.nls.hakunapi.html.model.HTMLContext;
import fi.nls.hakunapi.simple.servlet.jakarta.MediaTypes;

@Path("/collections/{collectionId}/queryables")
public class GetCollectionQueryablesImpl {


    @Inject
    private FeatureServiceConfig service;

    @GET
    @Produces(MediaTypes.APPLICATION_SCHEMA)
    @ResponseClass(Queryables.class)
    public Response handle(
            @PathParam("collectionId") String collectionId,
            @QueryParam("lang") @ParamClass(LangParam.class) String langParam,
            @Context UriInfo uriInfo,
            @Context HttpHeaders headers) {
        String lang = OperationUtil.resolveLang(service, langParam, headers);
        Queryables queryables = toQueryables(collectionId, lang, headers);

        Response.ResponseBuilder rb = Response.ok(queryables);
        if (lang != null) {
            rb.header("Content-Language", lang);
        }
        return rb.build();
    }

    /**
     * The queryable property names are identifiers rather than linguistic text,
     * but the collection title and description this document carries are the
     * same descriptive elements /collections/{collectionId} localizes, so they
     * are read through the localized service.
     */
    private Queryables toQueryables(String collectionId, String lang, HttpHeaders headers) {
        FeatureType ft = service.localized(lang).getCollection(collectionId);
        if (ft == null) {
            throw new NotFoundException("Unknown collection");
        }
        String id = String.format("%s/collections/%s/queryables", service.getCurrentServerURL(headers::getHeaderString), collectionId);
        Queryables queryables = new Queryables(ft.getName(), id, ft.getTitle(), ft.getDescription());
        for (HakunaProperty queryable : ft.getQueryableProperties()) {
            queryables.addProperty(queryable.getName(), queryable.getSchema());
        }
        return queryables;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    // Response rather than HTMLContext so that Content-Language can be set;
    // OpenAPI30Generator reads the schema off ResponseClass when it sees Response
    @ResponseClass(Queryables.class)
    public Response handleHTML(
            @PathParam("collectionId") String collectionId,
            @QueryParam("lang") @ParamClass(LangParam.class) String langParam,
            @Context UriInfo uriInfo,
            @Context HttpHeaders headers) {
        String lang = OperationUtil.resolveLang(service, langParam, headers);
        Queryables queryables = toQueryables(collectionId, lang, headers);

        String basePath = service.getCurrentServerURL(headers::getHeaderString);
        HTMLContext<Queryables> html = new HTMLContext<>(service, basePath, queryables, lang,
                service.getLanguages());

        // GenericEntity preserves HTMLContext<Queryables> as a ParameterizedType.
        // HTMLMessageBodyWriter picks the template off that type argument, so a
        // bare Response.ok(html) erases it and no writer matches.
        Response.ResponseBuilder rb = Response.ok(new GenericEntity<HTMLContext<Queryables>>(html) {
        });
        if (lang != null) {
            rb.header("Content-Language", lang);
        }
        return rb.build();
    }

}
