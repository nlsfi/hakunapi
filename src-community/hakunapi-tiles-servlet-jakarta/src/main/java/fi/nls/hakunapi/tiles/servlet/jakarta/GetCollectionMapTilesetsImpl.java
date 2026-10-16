package fi.nls.hakunapi.tiles.servlet.jakarta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import fi.nls.hakunapi.core.extension.ApiExtension;
import fi.nls.hakunapi.core.operation.DynamicPathOperation;
import fi.nls.hakunapi.core.operation.DynamicResponseOperation;
import fi.nls.hakunapi.core.param.APIParam;
import fi.nls.hakunapi.core.schemas.Link;
import fi.nls.hakunapi.simple.servlet.jakarta.ResponseUtil;
import fi.nls.hakunapi.tiles.MapTileLayer;
import fi.nls.hakunapi.tiles.TileMatrixSet;
import fi.nls.hakunapi.tiles.TilesLinkRel;
import fi.nls.hakunapi.tiles.TilesServiceConfig;
import fi.nls.hakunapi.tiles.schemas.TileSet;
import fi.nls.hakunapi.tiles.schemas.TileSetItem;
import fi.nls.hakunapi.tiles.schemas.TileSetsList;

@Path("/collections/{collectionId}/map/tiles")
public class GetCollectionMapTilesetsImpl implements DynamicPathOperation, DynamicResponseOperation {
    static final String PNG = "image/png";
    static final String TMS_URI_PREFIX = "http://www.opengis.net/def/tilematrixset/OGC/1.0/";

    @Inject
    private FeatureServiceConfig service;

    @Inject
    private TilesServiceConfig tiles;

    @Override
    public List<String> getValidPaths(FeatureServiceConfig service) {
        List<String> paths = new ArrayList<>();
        for (ApiExtension extension : service.getApiExtensions()) {
            for (String id : extension.getCollectionIds(TilesApiExtension.MAP_TILES)) {
                paths.add("/collections/" + id + "/map/tiles");
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
        map.put(MediaType.APPLICATION_JSON, TileSetsList.class);
        return map;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response list(@PathParam("collectionId") String collectionId, @Context HttpHeaders headers) {
        MapTileLayer layer = tiles.getMapLayer(collectionId).orElse(null);
        if (layer == null) {
            return ResponseUtil.exception(Status.NOT_FOUND, "Collection has no map tiles");
        }
        String base = service.getCurrentServerURL(headers::getHeaderString);
        String tilesPath = base + "/collections/" + collectionId + "/map/tiles";

        List<Link> links = new ArrayList<>();
        links.add(new Link(tilesPath, TilesLinkRel.SELF, MediaType.APPLICATION_JSON, "This document"));

        List<TileSetItem> tilesets = new ArrayList<>();
        for (String tmsId : layer.getTileMatrixSetIds()) {
            TileMatrixSet tms = tiles.getTileMatrixSets().get(tmsId).orElse(null);
            if (tms == null) {
                continue;
            }
            String tilesetPath = tilesPath + "/" + tmsId;
            List<Link> tsLinks = new ArrayList<>();
            tsLinks.add(new Link(tilesetPath, TilesLinkRel.SELF, MediaType.APPLICATION_JSON,
                    "Tileset " + tmsId));
            tsLinks.add(new Link(base + "/tileMatrixSets/" + tmsId, TilesLinkRel.TILING_SCHEME,
                    MediaType.APPLICATION_JSON, "Definition of " + tmsId));
            tilesets.add(new TileSetItem(layer.getTitle(), "map", tms.getCrs(), tmsId,
                    TMS_URI_PREFIX + tmsId, tsLinks));
        }
        return Response.ok(new TileSetsList(links, tilesets)).build();
    }

    @GET
    @Path("/{tileMatrixSetId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response tileset(@PathParam("collectionId") String collectionId,
            @PathParam("tileMatrixSetId") String tileMatrixSetId, @Context HttpHeaders headers) {
        MapTileLayer layer = tiles.getMapLayer(collectionId).orElse(null);
        if (layer == null || !layer.supportsTileMatrixSet(tileMatrixSetId)) {
            return ResponseUtil.exception(Status.NOT_FOUND, "No such tileset");
        }
        TileMatrixSet tms = tiles.getTileMatrixSets().get(tileMatrixSetId).orElse(null);
        if (tms == null) {
            return ResponseUtil.exception(Status.NOT_FOUND, "Unknown tileMatrixSetId");
        }
        String base = service.getCurrentServerURL(headers::getHeaderString);
        String tilesetPath = base + "/collections/" + collectionId + "/map/tiles/" + tileMatrixSetId;
        String mediaType = layer.getMediaTypes().isEmpty() ? PNG : layer.getMediaTypes().get(0);

        List<Link> links = new ArrayList<>();
        links.add(new Link(tilesetPath, TilesLinkRel.SELF, MediaType.APPLICATION_JSON, "This document"));
        links.add(new Link(base + "/tileMatrixSets/" + tileMatrixSetId, TilesLinkRel.TILING_SCHEME,
                MediaType.APPLICATION_JSON, "Definition of " + tileMatrixSetId));
        String tileTemplate = tilesetPath + "/{tileMatrix}/{tileRow}/{tileCol}";
        links.add(new Link(tileTemplate, TilesLinkRel.ITEM, mediaType, "Tiles"));

        TileSet tileSet = new TileSet(layer.getTitle(), "map", tms.getCrs(),
                TMS_URI_PREFIX + tileMatrixSetId, links);
        return Response.ok(tileSet).build();
    }
}
