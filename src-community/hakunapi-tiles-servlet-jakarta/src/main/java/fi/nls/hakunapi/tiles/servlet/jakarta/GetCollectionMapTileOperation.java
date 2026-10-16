package fi.nls.hakunapi.tiles.servlet.jakarta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.extension.ApiExtension;
import fi.nls.hakunapi.core.operation.DynamicPathOperation;
import fi.nls.hakunapi.core.operation.DynamicResponseOperation;
import fi.nls.hakunapi.core.param.APIParam;
import fi.nls.hakunapi.simple.servlet.jakarta.ResponseUtil;
import fi.nls.hakunapi.tiles.MapTileLayer;
import fi.nls.hakunapi.tiles.Tile;
import fi.nls.hakunapi.tiles.TilesServiceConfig;

@Path("/collections/{collectionId}/map/tiles")
public class GetCollectionMapTileOperation implements DynamicPathOperation, DynamicResponseOperation {
    private static final Logger LOG = LoggerFactory.getLogger(GetCollectionMapTileOperation.class);

    static final String PNG = "image/png";

    @Inject
    private TilesServiceConfig tiles;

    @Override
    public List<String> getValidPaths(FeatureServiceConfig service) {
        List<String> paths = new ArrayList<>();
        for (ApiExtension extension : service.getApiExtensions()) {
            for (String id : extension.getCollectionIds(TilesApiExtension.MAP_TILES)) {
                paths.add("/collections/" + id + "/map/tiles/{tileMatrixSetId}/{tileMatrix}/{tileRow}/{tileCol}");
            }
        }
        return paths;
    }

    @Override
    public List<? extends APIParam> getParameters(String path, FeatureServiceConfig service) {
        return List.of();
    }

    @Override
    public Map<String, Class<?>> getResponsesByContentType(FeatureServiceConfig service) {
        Map<String, Class<?>> map = new HashMap<>();
        map.put(PNG, byte[].class);
        return map;
    }

    @GET
    @Path("/{tileMatrixSetId}/{tileMatrix}/{tileRow}/{tileCol}")
    public Response handle(
            @PathParam("collectionId") String collectionId,
            @PathParam("tileMatrixSetId") String tileMatrixSetId,
            @PathParam("tileMatrix") String tileMatrix,
            @PathParam("tileRow") long tileRow,
            @PathParam("tileCol") long tileCol,
            @Context UriInfo uriInfo) {
        MapTileLayer layer = tiles.getMapLayer(collectionId).orElse(null);
        if (layer == null) {
            return ResponseUtil.exception(Status.NOT_FOUND, "Collection has no map tiles");
        }
        if (!layer.supportsTileMatrixSet(tileMatrixSetId)) {
            return ResponseUtil.exception(Status.NOT_FOUND, "Unsupported tileMatrixSetId");
        }
        String mediaType = layer.getMediaTypes().isEmpty() ? PNG : layer.getMediaTypes().get(0);
        try {
            Map<String, String> requestParams = whitelistedParams(layer, uriInfo);
            Tile tile = layer.getSource()
                    .getTile(layer, tileMatrixSetId, tileMatrix, tileRow, tileCol, mediaType, requestParams)
                    .orElse(null);
            if (tile == null) {
                return ResponseUtil.exception(Status.NOT_FOUND, "Tile not found");
            }
            return TileResponse.ok(tile, mediaType);
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
            return ResponseUtil.exception(Status.INTERNAL_SERVER_ERROR,
                    "Error occured, message: " + e.getMessage());
        }
    }

    private static Map<String, String> whitelistedParams(MapTileLayer layer, UriInfo uriInfo) {
        Set<String> allowed = layer.getSource().getPassthroughParams(layer);
        if (allowed.isEmpty()) {
            return Map.of();
        }
        MultivaluedMap<String, String> query = uriInfo.getQueryParameters();
        Map<String, String> out = new LinkedHashMap<>();
        for (String name : allowed) {
            String value = query.getFirst(name);
            if (value != null) {
                out.put(name, value);
            }
        }
        return out;
    }
}
