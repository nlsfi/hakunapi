package fi.nls.hakunapi.core;

import java.util.List;

public enum MetadataFormat {

    JSON("json", List.of("application/json", "application/schema+json", "application/vnd.oai.openapi+json;version=3.0")),
    HTML("html", List.of("text/html"));

    public final String id;
    public final List<String> contentTypes;

    private MetadataFormat(String id, List<String> contentTypes) {
        this.id = id;
        this.contentTypes = contentTypes;
    }

}
