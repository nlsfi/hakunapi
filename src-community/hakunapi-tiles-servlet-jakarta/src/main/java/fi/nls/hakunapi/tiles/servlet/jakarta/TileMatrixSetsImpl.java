package fi.nls.hakunapi.tiles.servlet.jakarta;

import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.schemas.Link;
import fi.nls.hakunapi.simple.servlet.jakarta.ResponseUtil;
import fi.nls.hakunapi.simple.servlet.jakarta.operation.ResponseClass;
import fi.nls.hakunapi.tiles.TileMatrixSet;
import fi.nls.hakunapi.tiles.TilesServiceConfig;
import fi.nls.hakunapi.tiles.schemas.TileMatrixSetItem;
import fi.nls.hakunapi.tiles.schemas.TileMatrixSetJSON;
import fi.nls.hakunapi.tiles.schemas.TileMatrixSets;

@Path("/tileMatrixSets")
public class TileMatrixSetsImpl {
    @Inject
    private FeatureServiceConfig service;

    @Inject
    private TilesServiceConfig tiles;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public TileMatrixSets list(@Context HttpHeaders headers) {
        String base = service.getCurrentServerURL(headers::getHeaderString);
        List<TileMatrixSetItem> items = new ArrayList<>();
        for (TileMatrixSet tms : tiles.getTileMatrixSets().list()) {
            String self = base + "/tileMatrixSets/" + tms.getId();
            List<Link> links = new ArrayList<>();
            links.add(new Link(self, "self", MediaType.APPLICATION_JSON, tms.getTitle()));
            items.add(new TileMatrixSetItem(tms.getId(), tms.getTitle(),
                    "http://www.opengis.net/def/tilematrixset/OGC/1.0/" + tms.getId(), links));
        }
        return new TileMatrixSets(items);
    }

    @GET
    @Path("/{tileMatrixSetId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ResponseClass(TileMatrixSetJSON.class)
    public Response byId(@PathParam("tileMatrixSetId") String tileMatrixSetId) {
        return tiles.getTileMatrixSets().get(tileMatrixSetId)
                .map(tms -> Response.ok(new TileMatrixSetJSON(tms)).build())
                .orElseGet(() -> ResponseUtil.exception(Status.NOT_FOUND, "Unknown tileMatrixSetId"));
    }
}
