package fi.nls.hakunapi.smile;

import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.SerializedString;

public class GeoJSONStrings {

    public static final SerializableString TYPE = new SerializedString("type");
    public static final SerializableString FEATURE_COLLECTION = new SerializedString("FeatureCollection");
    public static final SerializableString FEATURE = new SerializedString("Feature");

    public static final SerializableString FEATURES = new SerializedString("features");

    public static final SerializableString ID = new SerializedString("id");

    public static final SerializableString GEOMETRY = new SerializedString("geometry");
    public static final SerializableString GEOMETRIES = new SerializedString("geometries");
    public static final SerializableString COORDINATES = new SerializedString("coordinates");
    public static final SerializableString BBOX = new SerializedString("bbox");

    public static final SerializableString POINT = new SerializedString("Point");
    public static final SerializableString LINESTRING = new SerializedString("LineString");
    public static final SerializableString POLYGON = new SerializedString("Polygon");
    public static final SerializableString MULTI_POINT = new SerializedString("MultiPoint");
    public static final SerializableString MULTI_LINESTRING = new SerializedString("MultiLineString");
    public static final SerializableString MULTI_POLYGON = new SerializedString("MultiPolygon");
    public static final SerializableString GEOMETRY_COLLECTION = new SerializedString("GeometryCollection");

    public static final SerializableString PROPERTIES = new SerializedString("properties");

    public static final SerializableString LINKS = new SerializedString("links");
    public static final SerializableString TIMESTAMP = new SerializedString("timestamp");
    public static final SerializableString NUMBER_RETURNED = new SerializedString("numberReturned");

    public static final SerializableString HREF = new SerializedString("href");
    public static final SerializableString REL = new SerializedString("rel");
    public static final SerializableString TITLE = new SerializedString("title");
    public static final SerializableString HREFLANG = new SerializedString("hreflang");

}
