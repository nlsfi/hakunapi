package fi.nls.hakunapi.tiles.servlet.jakarta;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.config.HakunaConfigParser;
import fi.nls.hakunapi.core.extension.ApiExtension;
import fi.nls.hakunapi.core.extension.ApiExtensionContext;
import fi.nls.hakunapi.core.extension.ApiExtensionFactory;
import fi.nls.hakunapi.tiles.AcceleratedTileLayer;
import fi.nls.hakunapi.tiles.AcceleratedTilesets;
import fi.nls.hakunapi.tiles.TileLocator;
import fi.nls.hakunapi.tiles.VectorTileSource;
import fi.nls.hakunapi.tiles.MapTileLayer;
import fi.nls.hakunapi.tiles.TileMatrixSetRegistry;
import fi.nls.hakunapi.tiles.MapTileSource;
import fi.nls.hakunapi.tiles.TilesServiceConfig;
import fi.nls.hakunapi.tiles.VectorTileLayer;
import fi.nls.hakunapi.tiles.config.TilesConfigParser;

/** Builds {@link TilesApiExtension} from the tiles configuration; publishes nothing when no layer is configured. */
public class TilesApiExtensionFactory implements ApiExtensionFactory {
    private static final Logger LOG = LoggerFactory.getLogger(TilesApiExtensionFactory.class);

    private static final Set<String> AUTO_PUBLISHABLE = Set.of("vectortile-generator");

    @Override
    public ApiExtension create(ApiExtensionContext context) throws Exception {
        HakunaConfigParser config = context.config();
        Path configPath = context.configPath();
        Map<String, FeatureType> collections = context.collections();
        TilesConfigParser parser = new TilesConfigParser(config);
        if (!parser.hasTileLayers() && !parser.isVectorAutoPublishEnabled()) {
            return null;
        }

        Map<String, MapTileSource> mapSourcesByType = new HashMap<>();
        Map<String, VectorTileSource> vectorSourcesByType = new HashMap<>();
        for (MapTileSource source : ServiceLoader.load(MapTileSource.class)) {
            LOG.info("MapTileSource (map): {} -> {}", source.getType(), source);
            mapSourcesByType.put(source.getType(), source);
        }
        for (VectorTileSource source : ServiceLoader.load(VectorTileSource.class)) {
            LOG.info("MapTileSource (vector): {} -> {}", source.getType(), source);
            vectorSourcesByType.put(source.getType(), source);
        }

        TileMatrixSetRegistry tileMatrixSets = parser.readTileMatrixSets();
        List<VectorTileLayer> vectorLayers = new ArrayList<>();
        List<MapTileLayer> mapLayers = new ArrayList<>();
        for (String layerId : parser.readTileLayerIds()) {
            String type = parser.readTileLayerType(layerId);
            boolean isVector = vectorSourcesByType.size() == 1 && mapSourcesByType.isEmpty()
                    || (type != null && vectorSourcesByType.containsKey(type));
            boolean isMap = mapSourcesByType.size() == 1 && vectorSourcesByType.isEmpty()
                    || (type != null && mapSourcesByType.containsKey(type));
            if (isVector) {
                vectorLayers.add(parser.readVectorTileLayer(configPath, vectorSourcesByType,
                        tileMatrixSets, layerId));
            } else if (isMap) {
                mapLayers.add(parser.readMapTileLayer(configPath, mapSourcesByType,
                        tileMatrixSets, layerId));
            } else {
                throw new IllegalArgumentException("Unknown or missing tile source type for layer: "
                        + layerId + " (type=" + type + ")");
            }
        }

        if (parser.isVectorAutoPublishEnabled()) {
            addAutoPublishedLayers(parser, configPath, vectorSourcesByType, tileMatrixSets,
                    collections, vectorLayers);
        }

        if (vectorLayers.isEmpty() && mapLayers.isEmpty()) {
            LOG.info("No tile layers configured, OGC API - Tiles not published");
            return null;
        }

        Set<String> collectionless = new HashSet<>();
        for (VectorTileLayer layer : vectorLayers) {
            if (layer.isCollectionTileset() && !collections.containsKey(layer.getId())) {
                collectionless.add(layer.getId());
            }
        }

        AcceleratedTilesets accelerated = readAcceleratedTilesets(vectorLayers);

        LOG.info("OGC API - Tiles: {} vector layer(s) ({} tiles-only), {} map layer(s), {} sent as a range",
                vectorLayers.size(), collectionless.size(), mapLayers.size(),
                accelerated.getTilesetIds().size());
        return new TilesApiExtension(
                new TilesServiceConfig(vectorLayers, mapLayers, tileMatrixSets), collectionless,
                accelerated);
    }

    private AcceleratedTilesets readAcceleratedTilesets(List<VectorTileLayer> layers) {
        AcceleratedTilesets.Builder builder = AcceleratedTilesets.builder();
        for (VectorTileLayer layer : layers) {
            if (!(layer instanceof AcceleratedTileLayer accelerable)) {
                continue;
            }
            TileLocator locator = accelerable.getTileLocator();
            if (locator == null) {
                continue;
            }
            String internal = accelerable.getInternalLocation();

            java.nio.file.Path file = accelerable.getTileFile();
            if (file != null && !java.nio.file.Files.isReadable(file)) {
                LOG.warn("Tileset {}: sendfile was asked for but {} is not readable;"
                        + " serving it the ordinary way", layer.getId(), file);
                file = null;
            }
            if (file == null && internal == null) {
                LOG.warn("Tileset {} is indexed but has neither sendfile nor a front-end"
                        + " location; serving it the ordinary way", layer.getId());
                continue;
            }
            LOG.info("Tileset {} is served from its index: {}", layer.getId(),
                    file != null && internal != null ? "sendfile, or a front end at " + internal
                            : file != null ? "sendfile" : "a front end at " + internal);
            builder.add(layer.getId(), locator, internal, file);
        }
        return builder.build();
    }

    private void addAutoPublishedLayers(TilesConfigParser parser, Path configPath,
            Map<String, VectorTileSource> vectorSourcesByType, TileMatrixSetRegistry tileMatrixSets,
            Map<String, FeatureType> collections, List<VectorTileLayer> vectorLayers) throws Exception {
        Map<String, VectorTileSource> candidates = new LinkedHashMap<>(vectorSourcesByType);
        candidates.keySet().removeIf(type -> !AUTO_PUBLISHABLE.contains(type));

        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("tiles.vector=true requires a vector tile source that"
                    + " renders from feature collections; on the classpath: "
                    + vectorSourcesByType.keySet());
        }
        if (candidates.size() != 1 && parser.readDefaultTileLayerType() == null) {
            throw new IllegalArgumentException("tiles.vector=true with " + candidates.size()
                    + " renderable vector tile sources on the classpath (" + candidates.keySet()
                    + ") requires default.tiles.layers.type to name the one that renders"
                    + " auto-published layers");
        }
        Set<String> explicit = new HashSet<>();
        for (VectorTileLayer layer : vectorLayers) {
            explicit.add(layer.getId());
        }
        for (FeatureType ft : collections.values()) {
            if (ft.getGeom() == null || explicit.contains(ft.getName())) {
                continue;
            }
            vectorLayers.add(parser.readVectorTileLayer(configPath, candidates,
                    tileMatrixSets, ft.getName()));
        }
    }
}
