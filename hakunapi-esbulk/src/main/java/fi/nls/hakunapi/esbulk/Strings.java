package fi.nls.hakunapi.esbulk;

import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.SerializedString;

class Strings {

    public static final SerializableString NEW_LINE = new SerializedString("\n");

    public static final SerializableString INDEX = new SerializedString("index");
    public static final SerializableString _INDEX = new SerializedString("_index");

    public static final SerializableString ID = new SerializedString("id");
    public static final SerializableString _ID = new SerializedString("_id");

    public static final SerializableString GEOMETRY = new SerializedString("geometry");
    public static final SerializableString TYPE = new SerializedString("type");
    public static final SerializableString GEOMETRIES = new SerializedString("geometries");
    public static final SerializableString COORDINATES = new SerializedString("coordinates");

    public static final SerializableString POINT = new SerializedString("point");
    public static final SerializableString LINESTRING = new SerializedString("linestring");
    public static final SerializableString POLYGON = new SerializedString("polygon");
    public static final SerializableString MULTI_POINT = new SerializedString("multipoint");
    public static final SerializableString MULTI_LINESTRING = new SerializedString("multilinestring");
    public static final SerializableString MULTI_POLYGON = new SerializedString("multipolygon");
    public static final SerializableString GEOMETRY_COLLECTION = new SerializedString("geometrycollection");

}
