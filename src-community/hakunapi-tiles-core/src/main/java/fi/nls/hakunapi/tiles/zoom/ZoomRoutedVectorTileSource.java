package fi.nls.hakunapi.tiles.zoom;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.config.HakunaConfigParser;
import fi.nls.hakunapi.tiles.BytesTile;
import fi.nls.hakunapi.tiles.Tile;
import fi.nls.hakunapi.tiles.TileMatrixSetRegistry;
import fi.nls.hakunapi.tiles.VectorTileLayer;
import fi.nls.hakunapi.tiles.VectorTileSource;
import fi.nls.hakunapi.tiles.config.TilesConfigParser;
import fi.nls.hakunapi.tiles.mvt.MvtLayerExtract;

/** Delegates per zoom to the first configured backing covering it; a zoom no backing covers yields an empty tile, not a 404. */
public class ZoomRoutedVectorTileSource implements VectorTileSource {

    private static final Logger LOG = LoggerFactory.getLogger(ZoomRoutedVectorTileSource.class);

    public static final String TYPE = "zoom-routed-vector";

    public static final String DEFAULT_MEDIA_TYPE = "application/vnd.mapbox-vector-tile";

    /** A protobuf Tile with no layers is zero bytes: a well-formed empty tile. */
    private static final byte[] EMPTY_MVT = new byte[0];

    private final Map<String, List<ZoomBacking>> backingsByLayer = new LinkedHashMap<>();

    /** Resolved once and on first use: ServiceLoader finds this class too, so resolving in the constructor recurses. */
    private Map<String, VectorTileSource> backingSources;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public VectorTileLayer parse(HakunaConfigParser cfg, Path path, TileMatrixSetRegistry tms, String layerId)
            throws Exception {
        TilesConfigParser tiles = new TilesConfigParser(cfg);
        String p = "tiles.layers." + layerId + ".";

        String title = cfg.get(p + "title", layerId);
        String description = cfg.get(p + "description", title);
        List<String> tmsIds = tiles.readTileMatrixSetIds(layerId);
        List<String> collectionIds = List.of(cfg.getMultiple(p + "collections", new String[] { layerId }));

        String[] backingIds = cfg.getMultiple(p + "backings");
        if (backingIds.length == 0) {
            throw new IllegalArgumentException("tile layer " + layerId + ": " + p
                    + "backings must name at least one backing");
        }

        HakunaConfigParser backingCfg = withInheritedDefaults(cfg, layerId, backingIds, tmsIds);

        if (backingSources == null) {
            backingSources = sourcesByType();
        }
        List<ZoomBacking> backings = new ArrayList<>(backingIds.length);
        List<String> mediaTypes = new ArrayList<>();
        for (String backingId : backingIds) {
            ZoomBacking backing = parseBacking(backingCfg, path, tms, layerId, backingId, backingSources);
            backings.add(backing);
            for (String mediaType : backing.layer().getMediaTypes()) {
                if (!mediaTypes.contains(mediaType)) {
                    mediaTypes.add(mediaType);
                }
            }
        }
        backingsByLayer.put(layerId, backings);

        LOG.info("zoom-routed vector tile layer {} -> {} backing(s), collections={}",
                layerId, backings.size(), collectionIds);
        return new VectorTileLayer(layerId, title, description, this, collectionIds, tmsIds,
                mediaTypes, null);
    }

    private ZoomBacking parseBacking(HakunaConfigParser cfg, Path path, TileMatrixSetRegistry tms,
            String layerId, String backingId, Map<String, VectorTileSource> byType) throws Exception {
        String bp = "tiles.layers." + layerId + "." + backingId + ".";

        String type = cfg.get(bp + "type");
        if (type == null) {
            throw new IllegalArgumentException("tile layer " + layerId + " backing " + backingId
                    + ": " + bp + "type is required");
        }
        if (TYPE.equals(type)) {
            throw new IllegalArgumentException("tile layer " + layerId + " backing " + backingId
                    + ": a " + TYPE + " backing cannot itself be " + TYPE);
        }
        VectorTileSource source = byType.get(type);
        if (source == null) {
            throw new IllegalArgumentException("tile layer " + layerId + " backing " + backingId
                    + ": unknown vector tile source type '" + type + "'; on the classpath: "
                    + byType.keySet());
        }

        int[] zoom = parseZoomRange(layerId, backingId, cfg.get(bp + "zoom"));
        String mvtLayer = cfg.get(bp + "layer");

        // The backing's source sees the backing prefix as its layer id.
        VectorTileLayer backingLayer = source.parse(cfg, path, tms, layerId + "." + backingId);

        LOG.info("  backing {} type={} zoom={}-{}{}", backingId, type, zoom[0], zoom[1],
                mvtLayer == null ? "" : " layer=" + mvtLayer);
        return new ZoomBacking(backingId, zoom[0], zoom[1], mvtLayer, backingLayer);
    }

    /** Inherits tileMatrixSets from the layer, else a backing falls back to WebMercatorQuad; a copy because the parser is shared. */
    private static HakunaConfigParser withInheritedDefaults(HakunaConfigParser cfg, String layerId,
            String[] backingIds, List<String> tmsIds) {
        Properties p = new Properties();
        p.putAll(cfg.getProperties());
        String tms = String.join(",", tmsIds);
        for (String backingId : backingIds) {
            String bp = "tiles.layers." + layerId + "." + backingId + ".";
            p.putIfAbsent(bp + "tileMatrixSets", tms);
        }
        return new HakunaConfigParser(p);
    }

    /** Parses n or n-m; required, since a backing with no zoom range would be a catch-all. */
    private static int[] parseZoomRange(String layerId, String backingId, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("tile layer " + layerId + " backing " + backingId
                    + ": zoom is required");
        }
        String s = value.trim();
        int dash = s.indexOf('-');
        try {
            if (dash < 0) {
                int z = Integer.parseInt(s);
                return new int[] { z, z };
            }
            int min = Integer.parseInt(s.substring(0, dash).trim());
            int max = Integer.parseInt(s.substring(dash + 1).trim());
            if (min > max) {
                throw new IllegalArgumentException("tile layer " + layerId + " backing " + backingId
                        + ": zoom '" + value + "' is inverted");
            }
            return new int[] { min, max };
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("tile layer " + layerId + " backing " + backingId
                    + ": zoom '" + value + "' is not a zoom level or an n-m range");
        }
    }

    private static Map<String, VectorTileSource> sourcesByType() {
        Map<String, VectorTileSource> byType = new LinkedHashMap<>();
        for (VectorTileSource source : ServiceLoader.load(VectorTileSource.class)) {
            if (!TYPE.equals(source.getType())) {
                byType.put(source.getType(), source);
            }
        }
        return byType;
    }

    @Override
    public Set<String> getPassthroughParams(VectorTileLayer layer) {
        Set<String> params = new LinkedHashSet<>();
        for (ZoomBacking backing : backings(layer)) {
            params.addAll(backing.layer().getSource().getPassthroughParams(backing.layer()));
        }
        return params;
    }

    @Override
    public boolean needsCollections(VectorTileLayer layer) {
        for (ZoomBacking backing : backings(layer)) {
            if (backing.layer().getSource().needsCollections(backing.layer())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Optional<Tile> getTile(VectorTileLayer layer, String tileMatrixSetId, String tileMatrix,
            long tileRow, long tileCol, List<FeatureType> collections,
            String mediaType, Map<String, String> requestParams) throws Exception {
        for (ZoomBacking backing : backings(layer)) {
            if (!backing.covers(tileMatrix)) {
                continue;
            }
            VectorTileLayer backingLayer = backing.layer();
            Optional<Tile> tile = backingLayer.getSource().getTile(backingLayer, tileMatrixSetId,
                    tileMatrix, tileRow, tileCol, collections, mediaType, requestParams);
            if (tile.isEmpty()) {
                // The backing owns this zoom: an address it holds no tile for is empty, not a fall-through.
                return empty(layer, tileMatrixSetId, tileMatrix, tileRow, tileCol, mediaType);
            }
            if (backing.layerName() == null) {
                // Relayed whole so a lazy tile stays lazy.
                return tile;
            }
            String encoding = tile.get().getContentEncoding();
            if (encoding != null) {
                // Extraction needs uncompressed tiles; a compressed backing is served whole, by a layer configured without layer=.
                throw new IllegalStateException("Backing " + backing.id() + " of tile layer "
                        + layer.getId() + " serves " + encoding + "-encoded tiles, which layer '"
                        + backing.layerName() + "' cannot be extracted from; drop the layer setting"
                        + " and serve the backing whole");
            }
            // Extraction needs the bytes, so a lazy tile is materialised.
            byte[] extracted = MvtLayerExtract.extract(tile.get().getBytes(), backing.layerName());
            if (extracted == null) {
                // The tile has no layer of that name: empty.
                return empty(layer, tileMatrixSetId, tileMatrix, tileRow, tileCol, mediaType);
            }
            return Optional.of(new BytesTile(tileMatrixSetId, tileMatrix, tileRow, tileCol,
                    tile.get().getMediaType(), extracted));
        }
        return empty(layer, tileMatrixSetId, tileMatrix, tileRow, tileCol, mediaType);
    }

    private static Optional<Tile> empty(VectorTileLayer layer, String tileMatrixSetId,
            String tileMatrix, long tileRow, long tileCol, String requestedMediaType) {
        String mediaType = requestedMediaType != null ? requestedMediaType
                : layer.getMediaTypes().isEmpty() ? DEFAULT_MEDIA_TYPE : layer.getMediaTypes().get(0);
        return Optional.of(new BytesTile(tileMatrixSetId, tileMatrix, tileRow, tileCol,
                mediaType, EMPTY_MVT));
    }

    private List<ZoomBacking> backings(VectorTileLayer layer) {
        List<ZoomBacking> backings = backingsByLayer.get(layer.getId());
        if (backings == null) {
            throw new IllegalStateException("No backings parsed for tile layer " + layer.getId());
        }
        return backings;
    }

    @Override
    public void close() throws Exception {
        Exception first = null;
        if (backingSources == null) {
            backingsByLayer.clear();
            return;
        }
        for (VectorTileSource source : backingSources.values()) {
            try {
                source.close();
            } catch (Exception e) {
                if (first == null) {
                    first = e;
                }
            }
        }
        backingSources.clear();
        backingsByLayer.clear();
        if (first != null) {
            throw first;
        }
    }

}
