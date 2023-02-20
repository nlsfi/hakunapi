package fi.nls.hakunapi.simple.servlet.operation;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.schemas.Queryables;
import fi.nls.hakunapi.html.model.HTMLContext;

@Path("/collections/{collectionId}/queryables")
public class GetCollectionQueryablesImpl {

    @Inject
    private FeatureServiceConfig service;

    @GET
    @Produces("application/schema+json")
    public Queryables handle(@PathParam("collectionId") String collectionId, @Context UriInfo uriInfo) {
        FeatureType ft = service.getCollection(collectionId);
        if (ft == null) {
            throw new NotFoundException("Unknown collection");
        }
        String id = String.format("%s/collections/%s/queryables", service.getCurrentServerURL(), collectionId);
        Queryables queryables = new Queryables(ft.getName(), id, ft.getTitle(), ft.getDescription());
        for (HakunaProperty queryable : ft.getQueryableProperties()) {
            queryables.addProperty(queryable.getName(), queryable.getSchema());
        }
        return queryables;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public HTMLContext<Queryables> handleHTML(@PathParam("collectionId") String collectionId, @Context UriInfo uriInfo) {
        return new HTMLContext<>(service, handle(collectionId, uriInfo));
    }

}
