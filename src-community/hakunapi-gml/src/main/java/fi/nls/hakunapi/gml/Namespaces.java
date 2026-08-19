package fi.nls.hakunapi.gml;

public enum Namespaces {

    GML_311("gml", "http://www.opengis.net/gml", "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd"),
    WFS_110("wfs", "http://www.opengis.net/wfs", "http://schemas.opengis.net/wfs/1.1.0/wfs.xsd"),
    XSI("xsi", "http://www.w3.org/2001/XMLSchema-instance", null);

    public final Namespace ns;

    private Namespaces(String prefix, String uri, String schemaLocation) {
        this.ns = new Namespace(prefix, uri, schemaLocation);
    }

}
