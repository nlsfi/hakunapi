package fi.nls.hakunapi.core.operation;

public enum ApiTag {

    Capabilities("Essential characteristics of this API including information about the data."),
    Features("Access to data (features)."),
    Tiles("Access to data (features), partitioned into a hierarchy of tiles."),
    ;

    public final String description;

    private ApiTag(String description) {
        this.description = description;
    }

}
