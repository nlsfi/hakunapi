package fi.nls.hakunapi.tiles.servlet.jakarta;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyType;
import fi.nls.hakunapi.tiles.TileMatrix;
import fi.nls.hakunapi.tiles.TileMatrixSet;
import fi.nls.hakunapi.tiles.schemas.TileJSON;
import fi.nls.hakunapi.tiles.schemas.VectorLayerJSON;

public final class TileJSONBuilder {
    private static final String NUMBER = "Number";
    private static final String STRING = "String";
    private static final String BOOLEAN = "Boolean";

    private TileJSONBuilder() {}

    public static TileJSON build(String name, String description, String tilesetPath,
            TileMatrixSet tms, List<FeatureType> collections) {
        List<String> layerIds = new ArrayList<>(collections.size());
        for (int i = 0; i < collections.size(); i++) {
            layerIds.add(collections.get(i).getName());
        }
        return build(name, description, tilesetPath, tms, layerIds, collections);
    }

    public static TileJSON build(String name, String description, String tilesetPath,
            TileMatrixSet tms, List<String> layerIds, List<FeatureType> collections) {
        List<TileMatrix> matrices = tms.getTileMatrices();
        int minzoom = 0;
        int maxzoom = 0;
        if (!matrices.isEmpty()) {
            minzoom = zoomOf(matrices.get(0));
            maxzoom = zoomOf(matrices.get(matrices.size() - 1));
        }

        List<String> tiles = List.of(tilesetPath + "/{z}/{y}/{x}");

        List<VectorLayerJSON> layers = new ArrayList<>(layerIds.size());
        for (int i = 0; i < layerIds.size(); i++) {
            String layerId = layerIds.get(i);
            FeatureType ft = named(collections, layerId);
            layers.add(ft == null ? new VectorLayerJSON(layerId, null, Map.of())
                    : new VectorLayerJSON(ft.getName(), ft.getDescription(), fields(ft)));
        }
        return new TileJSON(name, description, tiles, minzoom, maxzoom,
                bounds(collections), layers);
    }

    private static FeatureType named(List<FeatureType> collections, String name) {
        for (int i = 0; i < collections.size(); i++) {
            FeatureType ft = collections.get(i);
            if (name.equals(ft.getName())) {
                return ft;
            }
        }
        return null;
    }

    private static int zoomOf(TileMatrix matrix) {
        try {
            return Integer.parseInt(matrix.getId());
        } catch (NumberFormatException ignore) {
            return 0;
        }
    }

    private static double[] bounds(List<FeatureType> collections) {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < collections.size(); i++) {
            double[] e = collections.get(i).getSpatialExtent();
            if (e == null || e.length < 4) {
                continue;
            }
            minX = Math.min(minX, e[0]);
            minY = Math.min(minY, e[1]);
            maxX = Math.max(maxX, e[2]);
            maxY = Math.max(maxY, e[3]);
        }
        if (minX > maxX || minY > maxY) {
            return TileJSON.WORLD;
        }
        return new double[] { minX, minY, maxX, maxY };
    }

    private static Map<String, String> fields(FeatureType ft) {
        Map<String, String> fields = new LinkedHashMap<>();
        HakunaProperty geom = ft.getGeom();
        HakunaProperty id = ft.getId();
        List<HakunaProperty> props = ft.getProperties();
        for (int i = 0; i < props.size(); i++) {
            HakunaProperty p = props.get(i);
            if (p == geom || p == id) {
                continue;
            }
            fields.put(p.getName(), fieldType(p.getType()));
        }
        return fields;
    }

    private static String fieldType(HakunaPropertyType type) {
        switch (type) {
        case INT:
        case LONG:
        case FLOAT:
        case DOUBLE:
            return NUMBER;
        case BOOLEAN:
            return BOOLEAN;
        default:
            return STRING;
        }
    }
}
