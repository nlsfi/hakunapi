package fi.nls.hakunapi.tiles.schemas;

import java.util.List;

/** TileJSON 2.2.0 (https://github.com/mapbox/tilejson-spec/tree/master/2.2.0); getter names are the JSON names, as there are no Jackson annotations. */
public class TileJSON {

    public static final double[] WORLD = { -180.0, -85.051129, 180.0, 85.051129 };

    private final String tilejson;
    private final String name;
    private final String description;
    private final String scheme;
    private final String format;
    private final List<String> tiles;
    private final int minzoom;
    private final int maxzoom;
    private final double[] bounds;
    private final List<VectorLayerJSON> vectorLayers;

    public TileJSON(String name, String description, List<String> tiles, int minzoom, int maxzoom,
            double[] bounds, List<VectorLayerJSON> vectorLayers) {
        this.tilejson = "2.2.0";
        this.scheme = "xyz";
        this.format = "pbf";
        this.name = name;
        this.description = description;
        this.tiles = tiles;
        this.minzoom = minzoom;
        this.maxzoom = maxzoom;
        this.bounds = bounds;
        this.vectorLayers = vectorLayers;
    }

    public String getTilejson() {
        return tilejson;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getScheme() {
        return scheme;
    }

    public String getFormat() {
        return format;
    }

    public List<String> getTiles() {
        return tiles;
    }

    public int getMinzoom() {
        return minzoom;
    }

    public int getMaxzoom() {
        return maxzoom;
    }

    public double[] getBounds() {
        return bounds;
    }

    public List<VectorLayerJSON> getVector_layers() {
        return vectorLayers;
    }

}
