package fi.nls.hakunapi.gml;

public class Namespace {

    public final String prefix;
    public final String uri;
    public final String schemaLocation;

    public Namespace(String prefix, String uri, String schemaLocation) {
        this.prefix = prefix;
        this.uri = uri;
        this.schemaLocation = schemaLocation;
    }

}
