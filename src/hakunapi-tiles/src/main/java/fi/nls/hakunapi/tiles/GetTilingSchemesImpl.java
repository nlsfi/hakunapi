package fi.nls.hakunapi.tiles;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.tiles.TilingScheme;
import fi.nls.hakunapi.core.tiles.TilingSchemeInfo;
import fi.nls.hakunapi.core.tiles.TilingSchemes;
import fi.nls.hakunapi.simple.servlet.operation.OperationUtil;

@Path("/tiles")
public class GetTilingSchemesImpl {

    @Inject
    private FeatureServiceConfig service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public TilingSchemes getTilingSchemes(@Context UriInfo uriInfo) {
        String query = OperationUtil.getQuery(service, uriInfo);
        Collection<TilingScheme> tilingSchemes = service.getTilingSchemes();
        return new TilingSchemes(tilingSchemes.stream()
                .map(scheme -> new TilingSchemeInfo(service, scheme, query))
                .collect(Collectors.toList()));
    }
    
    @GET
    @Path("/{tilingSchemeId}")
    @Produces(MediaType.APPLICATION_JSON)
    public TilingScheme getTilingScheme(@PathParam("tilingSchemeId") String tilingSchemeId) {
        return GetTilesUtil.getTilingScheme(service, tilingSchemeId);
    }

}
