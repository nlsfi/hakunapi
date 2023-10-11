package fi.nls.hakunapi.jsonfg;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;
import fi.nls.hakunapi.core.schemas.Crs;

public interface JSONFG {
	public static final String TYPE = "jsonfg";

	public static final String MEDIA_MAIN_TYPE = "application";
	public static final String MEDIA_SUB_TYPE = "vnd.ogc.fg+json";
	public static final String MIME_TYPE = MEDIA_MAIN_TYPE + "/" + MEDIA_SUB_TYPE;

	public static final byte[] CONFORMS_TO = "conformsTo".getBytes();
	public static final byte[] FEATURE_TYPE = "featureType".getBytes();
	public static final byte[] GEOMETRY_DIMENSION = "geometryDimension".getBytes();
	public static final byte[] FEATURE_SCHEMA = "featureSchema".getBytes();
	public static final byte[] COORD_REF_SYS = "coordRefSys".getBytes();

	public static final byte[] TIME = "time".getBytes();
	public static final byte[] DATE = "date".getBytes();

	// writer already has camelCase timeStamp
	public static final byte[] _TIMESTAMP = "timestamp".getBytes();
	public static final byte[] INTERVAL = "interval".getBytes();
	public static final byte[] PLACE = "place".getBytes();

	public static final byte[][] CONF = new byte[][] {
			//
			"http://www.opengis.net/spec/json-fg-1/0.2/conf/core".getBytes(),
			// "http://www.opengis.net/spec/json-fg-1/0.2/conf/types-schemas".getBytes()
	};

	// TODO: JSONFG schema differs from JSON schema.
	// We'll need JSONFG schema resource or SchemaFormatFactorySpi or such to
	// support
	// providing one
	default String getJSONFGSchema(FeatureType ft) {
		return null;
	}

	// TODO: spec
	default boolean isWGS84(int srid) {
		return srid == Crs.CRS84_SRID || srid == 4326 || srid == 4258;
	}

	default Integer getGeometryDimension(HakunaPropertyGeometry geomType) {
		if (geomType == null) {
			return null;
		}

		Integer geometryDimension = null;

		switch (geomType.getGeometryType()) {
		case POINT:
		case MULTIPOINT:
			geometryDimension = 0;
			break;
		case LINESTRING:
		case MULTILINESTRING:
			geometryDimension = 1;
			break;
		case POLYGON:
		case MULTIPOLYGON:
			geometryDimension = 2;
			break;
		case GEOMETRY:
		case GEOMETRYCOLLECTION:
		default:
			break;

		}
		return geometryDimension;
	}
}
