package fi.nls.hakunapi.tiles.schemas;

import java.util.List;

import fi.nls.hakunapi.core.schemas.Link;

public class TileMatrixSetItem {

    private final String id;
    private final String title;
    private final String uri;
    private final List<Link> links;

    public TileMatrixSetItem(String id, String title, String uri, List<Link> links) {
        this.id = id;
        this.title = title;
        this.uri = uri;
        this.links = links;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getUri() {
        return uri;
    }

    public List<Link> getLinks() {
        return links;
    }

}
