package fi.nls.hakunapi.tiles.schemas;

import java.util.List;

import fi.nls.hakunapi.core.schemas.Link;

public class TileSet {

    private final String title;
    private final String dataType;
    private final String crs;
    private final String tileMatrixSetURI;
    private final List<Link> links;
    private final List<GeospatialData> layers;

    public TileSet(String title, String dataType, String crs, String tileMatrixSetURI, List<Link> links) {
        this(title, dataType, crs, tileMatrixSetURI, links, null);
    }

    public TileSet(String title, String dataType, String crs, String tileMatrixSetURI, List<Link> links,
            List<GeospatialData> layers) {
        this.title = title;
        this.dataType = dataType;
        this.crs = crs;
        this.tileMatrixSetURI = tileMatrixSetURI;
        this.links = links;
        this.layers = layers;
    }

    public String getTitle() {
        return title;
    }

    public String getDataType() {
        return dataType;
    }

    public String getCrs() {
        return crs;
    }

    public String getTileMatrixSetURI() {
        return tileMatrixSetURI;
    }

    public List<Link> getLinks() {
        return links;
    }

    public List<GeospatialData> getLayers() {
        return layers;
    }

}
