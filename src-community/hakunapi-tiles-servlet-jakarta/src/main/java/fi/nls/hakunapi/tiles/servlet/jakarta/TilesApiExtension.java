package fi.nls.hakunapi.tiles.servlet.jakarta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.ConformanceClass;
import fi.nls.hakunapi.core.extension.ApiExtension;
import fi.nls.hakunapi.core.operation.OperationImpl;
import fi.nls.hakunapi.core.schemas.CollectionInfo;
import fi.nls.hakunapi.core.schemas.Crs;
import fi.nls.hakunapi.core.schemas.Extent;
import fi.nls.hakunapi.core.schemas.Link;
import fi.nls.hakunapi.core.schemas.SpatialExtent;
import fi.nls.hakunapi.core.util.U;
import fi.nls.hakunapi.tiles.MapTileLayer;
import fi.nls.hakunapi.tiles.TilesLinkRel;
import fi.nls.hakunapi.tiles.TilesServiceConfig;
import fi.nls.hakunapi.tiles.AcceleratedTilesets;
import fi.nls.hakunapi.tiles.VectorTileLayer;

/** Adds the OGC API - Tiles links, conformance classes and map tile collections to the Features service. */
public class TilesApiExtension implements ApiExtension {
    public static final String VECTOR_TILES = "tiles";

    public static final String MAP_TILES = "map/tiles";

    private static final String JSON = "application/json";

    private static final String MVT = "mvt";

    private final TilesServiceConfig tiles;

    private final Set<String> collectionlessVectorLayers;

    private final AcceleratedTilesets accelerated;

    public TilesApiExtension(TilesServiceConfig tiles) {
        this(tiles, Set.of(), AcceleratedTilesets.NONE);
    }

    public TilesApiExtension(TilesServiceConfig tiles, Set<String> collectionlessVectorLayers) {
        this(tiles, collectionlessVectorLayers, AcceleratedTilesets.NONE);
    }

    public TilesApiExtension(TilesServiceConfig tiles, Set<String> collectionlessVectorLayers,
            AcceleratedTilesets accelerated) {
        if (!tiles.hasVectorLayers() && !tiles.hasMapLayers()) {
            throw new IllegalArgumentException("No tile layers, do not publish OGC API - Tiles!");
        }
        this.tiles = tiles;
        this.collectionlessVectorLayers = Set.copyOf(collectionlessVectorLayers);
        this.accelerated = accelerated;
    }

    @Override
    public String getName() {
        return "OGC API - Tiles";
    }

    @Override
    public List<ConformanceClass> getConformanceClasses() {
        List<ConformanceClass> classes = new ArrayList<>();
        classes.add(ConformanceClass.TILES_CORE);
        classes.add(ConformanceClass.TILES_TILESET);
        classes.add(ConformanceClass.TILES_TILESETS_LIST);

        if (hasGeodataTilesets()) {
            classes.add(ConformanceClass.TILES_GEODATA_TILESETS);
        }
        if (tiles.hasVectorLayers()) {
            classes.add(ConformanceClass.TILES_DATASET_TILESETS);
        }
        classes.add(ConformanceClass.TILES_OAS30);
        classes.add(ConformanceClass.TILEMATRIXSET);

        if (servesMvt()) {
            classes.add(ConformanceClass.TILES_MVT);
        }
        return classes;
    }

    @Override
    public List<OperationImpl> getOperations() {
        List<OperationImpl> operations = new ArrayList<>();
        operations.add(new OperationImpl(new GetTileMatrixSetsOperation(), TileMatrixSetsImpl.class));
        if (tiles.hasVectorLayers()) {
            operations.add(new OperationImpl(new GetDatasetTilesetsListOperation(), GetDatasetTilesetsImpl.class));
            operations.add(new OperationImpl(new GetDatasetTileOperation(), GetDatasetTileImpl.class));
        }

        if (hasCollectionVectorTilesets()) {
            operations.add(new OperationImpl(new GetTilesetsListOperation(), GetCollectionTilesetsImpl.class));
            operations.add(new OperationImpl(new GetTileOperation(), GetCollectionTileOperation.class));
        }
        if (tiles.hasMapLayers()) {
            operations.add(new OperationImpl(new GetMapTilesetsListOperation(), GetCollectionMapTilesetsImpl.class));
            operations.add(new OperationImpl(new GetMapTileOperation(), GetCollectionMapTileOperation.class));
        }
        return operations;
    }

    @Override
    public Map<Class<?>, Object> getInjectables() {
        return Map.of(TilesServiceConfig.class, tiles,
                AcceleratedTilesets.class, accelerated);
    }

    @Override
    public List<Link> getLandingPageLinks(String baseUrl, Map<String, String> queryParams) {
        String query = U.toQuery(queryParams);
        List<Link> links = new ArrayList<>();
        links.add(new Link(baseUrl + "/tileMatrixSets" + query, TilesLinkRel.TILING_SCHEMES, JSON,
                "The tile matrix sets supported by this service"));
        if (tiles.hasVectorLayers()) {
            links.add(new Link(baseUrl + "/tiles" + query, TilesLinkRel.TILESETS_VECTOR, JSON,
                    "Vector tilesets of the dataset"));
        }
        return links;
    }

    @Override
    public List<Link> getCollectionLinks(FeatureType ft, String baseUrl, Map<String, String> queryParams) {
        if (!hasVectorLayer(ft.getName())) {
            return List.of();
        }
        String href = baseUrl + "/collections/" + ft.getName() + "/tiles" + U.toQuery(queryParams);
        return List.of(new Link(href, TilesLinkRel.TILESETS_VECTOR, JSON,
                "Vector tilesets for " + ft.getName()));
    }

    @Override
    public List<CollectionInfo> getCollections(String baseUrl, Map<String, String> queryParams) {
        List<CollectionInfo> collections = new ArrayList<>();
        for (MapTileLayer layer : tiles.getMapLayers()) {
            collections.add(toCollectionInfo(layer, baseUrl));
        }

        for (VectorTileLayer layer : tiles.getVectorLayers()) {
            if (collectionlessVectorLayers.contains(layer.getId())) {
                collections.add(toCollectionInfo(layer, baseUrl));
            }
        }
        return collections;
    }

    @Override
    public List<String> getCollectionIds(String resource) {
        List<String> ids;
        switch (resource) {
        case VECTOR_TILES:
            ids = new ArrayList<>();
            for (VectorTileLayer layer : tiles.getVectorLayers()) {
                if (layer.isCollectionTileset()) {
                    ids.add(layer.getId());
                }
            }
            return ids;
        case MAP_TILES:
            ids = new ArrayList<>();
            for (MapTileLayer layer : tiles.getMapLayers()) {
                ids.add(layer.getId());
            }
            return ids;
        default:
            return List.of();
        }
    }

    @Override
    public Map<String, List<String>> getFormatMediaTypes() {
        if (!servesMvt()) {
            return Map.of();
        }
        return Map.of(MVT, List.of(GetCollectionTileOperation.VECTOR_TILE));
    }

    @Override
    public void close() {
    }

    private boolean hasGeodataTilesets() {
        return hasCollectionVectorTilesets() || tiles.hasMapLayers();
    }

    private boolean hasCollectionVectorTilesets() {
        for (VectorTileLayer layer : tiles.getVectorLayers()) {
            if (layer.isCollectionTileset()) {
                return true;
            }
        }
        return false;
    }

    private boolean servesMvt() {
        for (VectorTileLayer layer : tiles.getVectorLayers()) {
            if (layer.getMediaTypes().contains(GetCollectionTileOperation.VECTOR_TILE)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasVectorLayer(String collectionId) {
        VectorTileLayer layer = tiles.getVectorLayer(collectionId).orElse(null);
        return layer != null && layer.isCollectionTileset();
    }

    private CollectionInfo toCollectionInfo(MapTileLayer layer, String baseUrl) {
        return toCollectionInfo(layer.getId(), layer.getTitle(), layer.getDescription(),
                layer.getBbox(), baseUrl, "map/tiles", TilesLinkRel.TILESETS_MAP, "Map", "map");
    }

    private CollectionInfo toCollectionInfo(VectorTileLayer layer, String baseUrl) {
        return toCollectionInfo(layer.getId(), layer.getTitle(), layer.getDescription(),
                layer.getBbox(), baseUrl, "tiles", TilesLinkRel.TILESETS_VECTOR, "Vector", "feature");
    }

    private CollectionInfo toCollectionInfo(String id, String title, String description,
            double[] bbox, String baseUrl, String tilesPath, String tilesRel,
            String tilesLabel, String itemType) {
        List<Link> links = new ArrayList<>();
        links.add(new Link(baseUrl + "/collections/" + id, "self", JSON, "This document"));
        links.add(new Link(baseUrl + "/collections/" + id + "/" + tilesPath, tilesRel,
                JSON, tilesLabel + " tilesets for " + id));

        SpatialExtent spatialExtent = bbox == null ? null : new SpatialExtent(bbox, Crs.CRS84);
        Extent extent = new Extent(spatialExtent, null);

        CollectionInfo ci = new CollectionInfo(id, title, description, links, extent, null, null);
        ci.setItemType(itemType);
        return ci;
    }
}
