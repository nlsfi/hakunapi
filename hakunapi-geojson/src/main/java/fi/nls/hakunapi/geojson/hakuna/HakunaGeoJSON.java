package fi.nls.hakunapi.geojson.hakuna;

import java.nio.charset.StandardCharsets;

public final class HakunaGeoJSON {

    public static final byte[] TYPE = "type".getBytes(StandardCharsets.UTF_8);
    public static final byte[] FEATURES = "features".getBytes(StandardCharsets.UTF_8);
    public static final byte[] ID = "id".getBytes(StandardCharsets.UTF_8);
    public static final byte[] GEOMETRY = "geometry".getBytes(StandardCharsets.UTF_8);
    public static final byte[] GEOMETRIES = "geometries".getBytes(StandardCharsets.UTF_8);
    public static final byte[] PROPERTIES = "properties".getBytes(StandardCharsets.UTF_8);
    public static final byte[] COORDINATES = "coordinates".getBytes(StandardCharsets.UTF_8);
    public static final byte[] BBOX = "bbox".getBytes(StandardCharsets.UTF_8);

    public static final byte[] FEATURE_COLLECTION = "FeatureCollection".getBytes(StandardCharsets.UTF_8);
    public static final byte[] FEATURE = "Feature".getBytes(StandardCharsets.UTF_8);
    public static final byte[] POINT = "Point".getBytes(StandardCharsets.UTF_8);
    public static final byte[] LINESTRING = "LineString".getBytes(StandardCharsets.UTF_8);
    public static final byte[] POLYGON = "Polygon".getBytes(StandardCharsets.UTF_8);
    public static final byte[] MULTI_POINT = "MultiPoint".getBytes(StandardCharsets.UTF_8);
    public static final byte[] MULTI_LINESTRING = "MultiLineString".getBytes(StandardCharsets.UTF_8);
    public static final byte[] MULTI_POLYGON = "MultiPolygon".getBytes(StandardCharsets.UTF_8);
    public static final byte[] GEOMETRY_COLLECTION = "GeometryCollection".getBytes(StandardCharsets.UTF_8);

    private HakunaGeoJSON() {}

}
