package fi.nls.hakunapi.tiles.schemas;

import java.util.List;

import fi.nls.hakunapi.core.schemas.Link;

public class TileSetItem {

    private final String title;
    private final String dataType;
    private final String crs;
    private final String tileMatrixSetId;
    private final String tileMatrixSetURI;
    private final List<Link> links;

    public TileSetItem(String title, String dataType, String crs, String tileMatrixSetId,
            String tileMatrixSetURI, List<Link> links) {
        this.title = title;
        this.dataType = dataType;
        this.crs = crs;
        this.tileMatrixSetId = tileMatrixSetId;
        this.tileMatrixSetURI = tileMatrixSetURI;
        this.links = links;
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

    public String getTileMatrixSetId() {
        return tileMatrixSetId;
    }

    public String getTileMatrixSetURI() {
        return tileMatrixSetURI;
    }

    public List<Link> getLinks() {
        return links;
    }

}
