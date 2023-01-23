package fi.nls.hakunapi.core.tiles;

import java.util.Collections;
import java.util.List;

import fi.nls.hakunapi.core.WFS3Service;
import fi.nls.hakunapi.core.schemas.Link;

public class TilingSchemeInfo {

    private final String identifier;
    private final List<Link> links;
    
    public TilingSchemeInfo(WFS3Service service, TilingScheme scheme, String query) {
        this.identifier = scheme.getIdentifier();
        String href = service.getCurrentServerURL() + "/tiles/" + scheme.getIdentifier() + query;
        String rel = "tilingScheme";
        String type = "application/json";
        String title = scheme.getTitle();
        this.links = Collections.singletonList(new Link(href, rel, type, title));
    }

    public String getIdentifier() {
        return identifier;
    }

    public List<Link> getLinks() {
        return links;
    }

}
