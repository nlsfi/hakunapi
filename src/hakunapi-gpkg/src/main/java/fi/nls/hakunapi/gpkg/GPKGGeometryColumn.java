package fi.nls.hakunapi.gpkg;

public class GPKGGeometryColumn {

    public static final String GPKG_TABLE_GEOMETRY_COLUMNS = "gpkg_geometry_columns";

    public enum GeometryTypeName {
        GEOMETRY,
        POINT,
        LINESTRING,
        POLYGON,
        MULTIPOINT,
        MULTILINESTRING,
        MULTIPOLYGON,
        GEOMETRYCOLLECTION
    }

    private final String table;
    private final String column;
    private final GeometryTypeName geometryTypeName;
    private final int srid;
    private final boolean hasZ;
    private final boolean hasM;

    public GPKGGeometryColumn(String table, String column, GeometryTypeName geometryTypeName, int srid, boolean hasZ, boolean hasM) {
        this.table = table;
        this.column = column;
        this.geometryTypeName = geometryTypeName;
        this.srid = srid;
        this.hasZ = hasZ;
        this.hasM = hasM;
    }

    public String getTable() {
        return table;
    }

    public String getColumn() {
        return column;
    }

    public GeometryTypeName getGeometryTypeName() {
        return geometryTypeName;
    }

    public int getSrid() {
        return srid;
    }

    public boolean isHasZ() {
        return hasZ;
    }

    public boolean isHasM() {
        return hasM;
    }

}
