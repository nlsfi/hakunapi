package fi.nls.hakunapi.tiles.schemas;

import java.util.List;

import fi.nls.hakunapi.core.schemas.Link;

public class TileSetsList {

    private final List<Link> links;
    private final List<TileSetItem> tilesets;

    public TileSetsList(List<Link> links, List<TileSetItem> tilesets) {
        this.links = links;
        this.tilesets = tilesets;
    }

    public List<Link> getLinks() {
        return links;
    }

    public List<TileSetItem> getTilesets() {
        return tilesets;
    }

}
