package fi.nls.hakunapi.core.schema;

import fi.nls.hakunapi.core.geom.HakunaGeometryType;

/**
 * Utility for transforming geometry types from x-geometry-type extension to HakunaGeometryType.
 */
public class GeometryTypeTransformer {

    private GeometryTypeTransformer() {
    }

    public static HakunaGeometryType fromWKT(String wktType) {
        if (wktType == null) {
            return null;
        }

        String baseType = wktType.toUpperCase().replaceAll("\\s*(Z|M|ZM)$", "");

        switch (baseType) {
            case "POINT":
                return HakunaGeometryType.POINT;
            case "LINESTRING":
                return HakunaGeometryType.LINESTRING;
            case "POLYGON":
                return HakunaGeometryType.POLYGON;
            case "MULTIPOINT":
                return HakunaGeometryType.MULTIPOINT;
            case "MULTILINESTRING":
                return HakunaGeometryType.MULTILINESTRING;
            case "MULTIPOLYGON":
                return HakunaGeometryType.MULTIPOLYGON;
            case "GEOMETRY":
            case "GEOMETRYCOLLECTION":
                return HakunaGeometryType.GEOMETRY;
            default:
                throw new IllegalArgumentException("Unknown WKT geometry type: " + wktType);
        }
    }

    public static boolean isCompatible(HakunaGeometryType stored, HakunaGeometryType constraint) {
        if (stored == null || constraint == null) {
            return false;
        }
        if (stored == HakunaGeometryType.GEOMETRY) {
            return true;
        }
        return stored == constraint;
    }

}
