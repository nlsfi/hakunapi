package fi.nls.hakunapi.gpkg;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import fi.nls.hakunapi.gpkg.extension.GPKGExtension;
import fi.nls.hakunapi.gpkg.function.ST_IsEmpty;
import fi.nls.hakunapi.gpkg.function.ST_MaxX;
import fi.nls.hakunapi.gpkg.function.ST_MaxY;
import fi.nls.hakunapi.gpkg.function.ST_MinX;
import fi.nls.hakunapi.gpkg.function.ST_MinY;

public class GPKG {

    private GPKG() {}
    
    public static void init(Connection c) throws SQLException {
        execute(c, "PRAGMA journal_mode = OFF");
        execute(c, "PRAGMA synchronous = OFF");
        execute(c, "PRAGMA locking_mode = EXCLUSIVE");
        execute(c, "PRAGMA page_size = 4096");
        execute(c, "PRAGMA application_id = 0x47503130");
        createSpatialRefSysTable(c);
        insertDefaultRefSys(c);
        createContentsTable(c);
        createGeometryColumnsTable(c);
        createExtensionsTable(c);
        new ST_IsEmpty().create(c);
        new ST_MinX().create(c);
        new ST_MinY().create(c);
        new ST_MaxX().create(c);
        new ST_MaxY().create(c);
    }

    private static void createSpatialRefSysTable(Connection c) throws SQLException {
        String sql = ""
                + "CREATE TABLE gpkg_spatial_ref_sys ("
                + "srs_name TEXT NOT NULL,"
                + "srs_id INTEGER NOT NULL PRIMARY KEY,"
                + "organization TEXT NOT NULL,"
                + "organization_coordsys_id INTEGER NOT NULL,"
                + "definition TEXT NOT NULL,"
                + "description TEXT"
                + ")";
        execute(c, sql);
    }

    private static void insertDefaultRefSys(Connection c) throws SQLException {
        insertSpatialRefSys(c, GPKGSpatialRefSystems.WGS84);
        insertSpatialRefSys(c, GPKGSpatialRefSystems.UNDEFINED_CARTESIAN);
        insertSpatialRefSys(c, GPKGSpatialRefSystems.UNDEFINED_GEOGRAPHIC);
    }

    public static int insertSpatialRefSys(Connection c, GPKGSpatialRefSys sys) throws SQLException {
        String sql = "INSERT OR IGNORE INTO gpkg_spatial_ref_sys "
                + " (srs_name, srs_id, organization, organization_coordsys_id, definition, description) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, sys.getSrsName());
            ps.setInt(2, sys.getSrsId());
            ps.setString(3, sys.getOrganisation());
            ps.setInt(4, sys.getOrganizationCoordsysId());
            ps.setString(5, sys.getDefinition());
            ps.setString(6, sys.getDescription());
            return ps.executeUpdate();
        }
    }

    private static void createContentsTable(Connection c) throws SQLException {
        String sql = ""
                + "CREATE TABLE gpkg_contents ("
                + "table_name TEXT NOT NULL PRIMARY KEY,"
                + "data_type TEXT NOT NULL,"
                + "identifier TEXT UNIQUE,"
                + "description TEXT DEFAULT '',"
                + "last_change DATETIME NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')),"
                + "min_x DOUBLE,"
                + "min_y DOUBLE,"
                + "max_x DOUBLE,"
                + "max_y DOUBLE,"
                + "srs_id INTEGER,"
                + "CONSTRAINT fk_gc_r_srs_id FOREIGN KEY (srs_id) REFERENCES gpkg_spatial_ref_sys(srs_id)"
                + ")"; 
        execute(c, sql);
    }

    private static void createGeometryColumnsTable(Connection c) throws SQLException {
        String sql = ""
                + "CREATE TABLE gpkg_geometry_columns ("
                + "table_name TEXT NOT NULL,"
                + "column_name TEXT NOT NULL,"
                + "geometry_type_name TEXT NOT NULL,"
                + "srs_id INTEGER NOT NULL,"
                + "z TINYINT NOT NULL,"
                + "m TINYINT NOT NULL,"
                + "CONSTRAINT pk_geom_cols PRIMARY KEY (table_name, column_name),"
                + "CONSTRAINT uk_gc_table_name UNIQUE (table_name),"
                + "CONSTRAINT fk_gc_tn FOREIGN KEY (table_name) REFERENCES gpkg_contents(table_name),"
                + "CONSTRAINT fk_gc_srs FOREIGN KEY (srs_id) REFERENCES gpkg_spatial_ref_sys(srs_id)"
                + ")";
        execute(c, sql);
    }

    private static void createExtensionsTable(Connection c) throws SQLException {
        String sql = ""
                + "CREATE TABLE gpkg_extensions ("
                + "table_name TEXT,"
                + "column_name TEXT,"
                + "extension_name TEXT NOT NULL,"
                + "definition TEXT NOT NULL,"
                + "scope TEXT NOT NULL,"
                + "CONSTRAINT ge_tce UNIQUE (table_name, column_name, extension_name)"
                + ")";
        execute(c, sql);
    }
    
    public static int insertFeaturesTableEntry(Connection c, GPKGFeaturesTable featuresTable) throws SQLException {
        String sql = "INSERT INTO gpkg_contents (table_name, data_type, identifier, description, min_x, min_y, max_x, max_y, srs_id)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, featuresTable.getTableName());
            ps.setString(2, GPKGFeaturesTable.DATA_TYPE);
            ps.setString(3, featuresTable.getIdentifier());
            ps.setString(4, featuresTable.getDescription());
            ps.setDouble(5, featuresTable.getMinX());
            ps.setDouble(6, featuresTable.getMinY());
            ps.setDouble(7, featuresTable.getMaxX());
            ps.setDouble(8, featuresTable.getMaxY());
            ps.setInt(9, featuresTable.getSrid());
            return ps.executeUpdate();
        }
    }
    
    public static int insertGeometryColumn(Connection c, GPKGGeometryColumn geometryColumn) throws SQLException {
        String sql = "INSERT INTO gpkg_geometry_columns (table_name, column_name, geometry_type_name, srs_id, z, m)"
                + " VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, geometryColumn.getTable());
            ps.setString(2, geometryColumn.getColumn());
            ps.setString(3, geometryColumn.getGeometryTypeName().toString());
            ps.setInt(4, geometryColumn.getSrid());
            ps.setInt(5, geometryColumn.isHasZ() ? 1 : 0);
            ps.setInt(6, geometryColumn.isHasM() ? 1 : 0);
            return ps.executeUpdate();
        }
    }

    public static int insertExtension(Connection c, GPKGExtension extension) throws SQLException {
        String sql = "INSERT OR REPLACE INTO gpkg_extensions (table_name, column_name, extension_name, definition, scope)"
                + " VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, extension.getTableName());
            ps.setString(2, extension.getColumnName());
            ps.setString(3, extension.getExtensionName());
            ps.setString(4, extension.getDefinition());
            ps.setString(5, extension.getScope());
            return ps.executeUpdate();
        }
    }

    protected static void execute(Connection c, String sql) throws SQLException {
        try (Statement s = c.createStatement()) {
            s.execute(sql);
        }
    }

}
