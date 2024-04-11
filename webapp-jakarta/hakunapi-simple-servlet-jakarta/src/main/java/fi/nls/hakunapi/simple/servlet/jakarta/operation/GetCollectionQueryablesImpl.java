package fi.nls.hakunapi.simple.servlet.jakarta.operation;

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
    public Queryables handle(
            @PathParam("collectionId") String collectionId,
            @Context UriInfo uriInfo,
            @Context HttpHeaders headers) {
        FeatureType ft = service.getCollection(collectionId);
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
    public HTMLContext<Queryables> handleHTML(
            @PathParam("collectionId") String collectionId,
            @Context UriInfo uriInfo,
            @Context HttpHeaders headers) {
        String basePath = service.getCurrentServerURL(headers::getHeaderString);
        return new HTMLContext<>(service, basePath, handle(collectionId, uriInfo, headers));
    }

}
