package fi.nls.hakunapi.tiles.servlet.jakarta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.operation.DynamicResponseOperation;
import fi.nls.hakunapi.simple.servlet.jakarta.ResponseUtil;
import fi.nls.hakunapi.tiles.AcceleratedTilesets;
import fi.nls.hakunapi.tiles.Tile;
import fi.nls.hakunapi.tiles.TileLocator;
import fi.nls.hakunapi.tiles.TilesServiceConfig;
import fi.nls.hakunapi.tiles.VectorTileLayer;

@Path("/tiles")
public class GetDatasetTileImpl implements DynamicResponseOperation {
    private static final Logger LOG = LoggerFactory.getLogger(GetDatasetTileImpl.class);

    public static final String X_ACCEL_REDIRECT = "X-Accel-Redirect";

    private static final String SENDFILE_FILENAME = "org.apache.tomcat.sendfile.filename";
    private static final String SENDFILE_START = "org.apache.tomcat.sendfile.start";
    private static final String SENDFILE_END = "org.apache.tomcat.sendfile.end";

    private static final CacheControl IMMUTABLE = immutable();

    private static CacheControl immutable() {
        CacheControl cc = new CacheControl();
        cc.setNoTransform(false);
        cc.setMaxAge(604800);

        cc.getCacheExtension().put("immutable", null);
        return cc;
    }

    @Context
    private HttpServletRequest request;

    @Inject
    private FeatureServiceConfig service;

    @Inject
    private TilesServiceConfig tiles;

    @Inject
    private AcceleratedTilesets accelerated;

    @Override
    public Map<String, Class<?>> getResponsesByContentType(FeatureServiceConfig service) {
        Map<String, Class<?>> map = new HashMap<>();
        map.put(GetCollectionTileOperation.VECTOR_TILE, byte[].class);
        return map;
    }

    @GET
    @Path("/{tileMatrixSetId}/{tileMatrix}/{tileRow}/{tileCol}")
    public Response handle(
            @PathParam("tileMatrixSetId") String tileMatrixSetId,
            @PathParam("tileMatrix") String tileMatrix,
            @PathParam("tileRow") long tileRow,
            @PathParam("tileCol") long tileCol,
            @QueryParam("collections") String collectionsParam,
            @Context UriInfo uriInfo) {
        List<VectorTileLayer> layers = participatingLayers(tileMatrixSetId);
        if (layers.isEmpty()) {
            return ResponseUtil.exception(Status.NOT_FOUND, "Unsupported tileMatrixSetId");
        }
        Set<String> requested = parseCollections(collectionsParam);

        if (requested != null) {
            VectorTileLayer stored = firstStoredLayer(layers, requested);
            if (stored != null) {
                return ResponseUtil.exception(Status.BAD_REQUEST, "Tileset " + stored.getId()
                        + " serves stored tiles and cannot be narrowed by collections");
            }
        } else if (layers.size() == 1) {
            AcceleratedTilesets.Entry entry = accelerated.get(layers.get(0).getId());
            if (entry != null) {
                Response range = range(entry, tileMatrix, tileRow, tileCol);
                if (range != null) {
                    return range;
                }
            }
        }

        String mediaType = layers.get(0).getMediaTypes().isEmpty()
                ? GetCollectionTileOperation.VECTOR_TILE : layers.get(0).getMediaTypes().get(0);
        try {
            Set<String> rendered = new LinkedHashSet<>();
            List<Tile> parts = new ArrayList<>(layers.size());
            for (int i = 0; i < layers.size(); i++) {
                VectorTileLayer layer = layers.get(i);
                if (requested != null && !contributes(layer, requested)) {
                    continue;
                }
                List<FeatureType> collections = resolveCollections(layer, rendered, requested);
                if (collections.isEmpty() && layer.getSource().needsCollections(layer)) {
                    continue;
                }
                Map<String, String> requestParams = whitelistedParams(layer, uriInfo);
                layer.getSource()
                        .getTile(layer, tileMatrixSetId, tileMatrix, tileRow, tileCol, collections,
                                mediaType, requestParams)
                        .ifPresent(parts::add);
            }
            if (parts.isEmpty()) {
                return ResponseUtil.exception(Status.NOT_FOUND, "Tile not found");
            }
            String encoding = parts.size() > 1 ? firstEncoding(parts) : null;
            if (encoding != null) {
                return ResponseUtil.exception(Status.CONFLICT, "This dataset composes a tile from"
                        + " several tilesets, one of which serves " + encoding + "-encoded tiles"
                        + " that cannot be composed");
            }
            return TileResponse.ok(parts, mediaType);
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
            return ResponseUtil.exception(Status.INTERNAL_SERVER_ERROR,
                    "Error occured, message: " + e.getMessage());
        }
    }

    private Response range(AcceleratedTilesets.Entry entry, String tileMatrix, long tileRow, long tileCol) {
        int z;
        try {
            z = Integer.parseInt(tileMatrix);
        } catch (NumberFormatException e) {
            return ResponseUtil.exception(Status.NOT_FOUND, "Tile not found");
        }

        long packed;
        try {
            packed = entry.locator().find(z, tileCol, tileRow);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.exception(Status.NOT_FOUND, "Tile not found");
        }
        if (packed == TileLocator.NOT_FOUND) {
            return ResponseUtil.exception(Status.NOT_FOUND, "Tile not found");
        }

        long offset = TileLocator.offset(packed);
        int length = TileLocator.length(packed);
        String encoding = entry.contentEncoding();

        EntityTag etag = new EntityTag(offset + "-" + length);

        if (entry.sendfilePath() != null && request != null) {
            sendfile(entry, offset, length);
            Response.ResponseBuilder sf = Response.ok()
                    .type(entry.mediaType())
                    .header(HttpHeaders.CONTENT_LENGTH, length)
                    .tag(etag)
                    .cacheControl(IMMUTABLE);
            if (encoding != null) {
                sf.header(HttpHeaders.CONTENT_ENCODING, encoding);
            }
            return sf.build();
        }

        if (entry.internalLocation() == null) {
            return null;
        }

        Response.ResponseBuilder rb = Response.ok()
                .type(entry.mediaType())
                .tag(etag)
                .cacheControl(IMMUTABLE)
                .header(X_ACCEL_REDIRECT, entry.redirectUri(offset, length, encoding));

        if (encoding != null) {
            rb.header(HttpHeaders.CONTENT_ENCODING, encoding);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("range {}/{}/{} -> {} +{}", tileMatrix, tileRow, tileCol, offset, length);
        }
        return rb.build();
    }

    private void sendfile(AcceleratedTilesets.Entry entry, long offset, int length) {
        request.setAttribute(SENDFILE_FILENAME, entry.sendfilePath());
        request.setAttribute(SENDFILE_START, offset);
        request.setAttribute(SENDFILE_END, offset + length);
    }

    private static Set<String> parseCollections(String value) {
        if (value == null) {
            return null;
        }
        Set<String> ids = new LinkedHashSet<>();
        int from = 0;
        while (from <= value.length()) {
            int comma = value.indexOf(',', from);
            int to = comma < 0 ? value.length() : comma;
            String id = value.substring(from, to).trim();
            if (!id.isEmpty()) {
                ids.add(id);
            }
            if (comma < 0) {
                break;
            }
            from = comma + 1;
        }
        return ids;
    }

    private static VectorTileLayer firstStoredLayer(List<VectorTileLayer> layers,
            Set<String> requested) {
        for (int i = 0; i < layers.size(); i++) {
            VectorTileLayer layer = layers.get(i);
            if (layer.isStoredTileset() && contributes(layer, requested)) {
                return layer;
            }
        }
        return null;
    }

    private static boolean contributes(VectorTileLayer layer, Set<String> requested) {
        List<String> ids = layer.getCollectionIds();
        for (int i = 0; i < ids.size(); i++) {
            if (requested.contains(ids.get(i))) {
                return true;
            }
        }
        return false;
    }

    private static String firstEncoding(List<Tile> parts) {
        for (int i = 0; i < parts.size(); i++) {
            if (parts.get(i).getContentEncoding() != null) {
                return parts.get(i).getContentEncoding();
            }
        }
        return null;
    }

    private List<VectorTileLayer> participatingLayers(String tileMatrixSetId) {
        List<VectorTileLayer> layers = new ArrayList<>();
        for (VectorTileLayer layer : tiles.getVectorLayers()) {
            if (layer.supportsTileMatrixSet(tileMatrixSetId)) {
                layers.add(layer);
            }
        }
        return layers;
    }

    private List<FeatureType> resolveCollections(VectorTileLayer layer, Set<String> rendered,
            Set<String> requested) {
        List<String> ids = layer.getCollectionIds();
        List<FeatureType> collections = new ArrayList<>(ids.size());
        for (int i = 0; i < ids.size(); i++) {
            String id = ids.get(i);
            if (requested != null && !requested.contains(id)) {
                continue;
            }
            if (!rendered.add(id)) {
                continue;
            }
            FeatureType ft = service.getCollection(id);
            if (ft != null) {
                collections.add(ft);
            }
        }
        return collections;
    }

    private static Map<String, String> whitelistedParams(VectorTileLayer layer, UriInfo uriInfo) {
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
