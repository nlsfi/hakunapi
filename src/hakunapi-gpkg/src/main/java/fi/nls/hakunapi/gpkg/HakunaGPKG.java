package fi.nls.hakunapi.gpkg;

import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;
import fi.nls.hakunapi.gpkg.GPKGGeometryColumn.GeometryTypeName;

public class HakunaGPKG {
    
    public static GPKGGeometryColumn toGeometryColumn(String tableName, HakunaPropertyGeometry geom, int type, int dimension, int srid) {
        GeometryTypeName typeName = getTypeName(type);
        boolean hasZ = dimension >= 3;
        boolean hasM = dimension >= 4;
        return new GPKGGeometryColumn(tableName, geom.getName(), typeName, srid, hasZ, hasM);
    }

    private static GeometryTypeName getTypeName(int type) {
        switch (type) {
        case 0: return GeometryTypeName.GEOMETRY;
        case 1: return GeometryTypeName.POINT;
        case 2: return GeometryTypeName.LINESTRING;
        case 3: return GeometryTypeName.POLYGON;
        case 4: return GeometryTypeName.MULTIPOINT;
        case 5: return GeometryTypeName.MULTILINESTRING;
        case 6: return GeometryTypeName.MULTIPOLYGON;
        case 7: return GeometryTypeName.GEOMETRYCOLLECTION;
        default: throw new RuntimeException("Type not supported");
        }
    }

}
