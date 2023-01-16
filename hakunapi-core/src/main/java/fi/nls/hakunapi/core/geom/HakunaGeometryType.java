package fi.nls.hakunapi.core.geom;

public enum HakunaGeometryType {

    GEOMETRY(0, "Geometry"),
    POINT(1, "Point"),
    LINESTRING(2, "LineString"),
    POLYGON(3, "Polygon"),
    MULTIPOINT(4, "MultiPoint"),
    MULTILINESTRING(5, "MultiLineString"),
    MULTIPOLYGON(6, "MultiPolygon"),
    GEOMETRYCOLLECTION(7, "GeometryCollection");
    
    private static final HakunaGeometryType[] VALUES_CACHE = values();

    public final int wkbType;
    public final String geoJsonType;
    
    private HakunaGeometryType(int wkbType, String geoJsonType) {
        this.wkbType = wkbType;
        this.geoJsonType = geoJsonType;
    }

    public static HakunaGeometryType fromWKBType(int wkbType) {
        return VALUES_CACHE[wkbType];
    }

}
