package fi.nls.hakunapi.gpkg.extension;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

import fi.nls.hakunapi.gpkg.GPKG;

public class RtreeIndexExtension extends GPKGExtension {

    public static final String EXTENSION_NAME = "gpkg_rtree_index";
    public static final String SCOPE = "write-only";
    public static final String DEFINITION = "https://www.geopackage.org/spec121/index.html#extension_rtree";

    private final String idColumn;

    public RtreeIndexExtension(String tableName, String columnName, String idColumn) {
        super(Objects.requireNonNull(tableName), Objects.requireNonNull(columnName), EXTENSION_NAME, DEFINITION, SCOPE);
        this.idColumn = Objects.requireNonNull(idColumn);
    }

    @Override
    public void createExtension(Connection c) throws SQLException {
        GPKG.insertExtension(c, this);
        createVirtualTable(c);
        load(c);
        createTriggers(c);
    }

    public static String getRtreeName(String tableName, String columnName) {
        return String.format("rtree_%s_%s", tableName, columnName);
    }

    private void createVirtualTable(Connection c) throws SQLException {
        String sql = String.format("CREATE VIRTUAL TABLE rtree_%s_%s USING rtree(id, minx, maxx, miny, maxy)",
                tableName, columnName);
        execute(c, sql);
    }

    private void load(Connection c) throws SQLException {
        String sql = String.format("INSERT OR REPLACE INTO rtree_%s_%s" + 
                " SELECT %s, st_minx(%s), st_maxx(%s), st_miny(%s), st_maxy(%s) FROM %s",
                tableName, columnName,
                idColumn, columnName, columnName, columnName, columnName, tableName);
        execute(c, sql);
    }

    private void createTriggers(Connection c) throws SQLException {
        createTriggerInsert(c);
        createTriggerUpdate1(c);
        createTriggerUpdate2(c);
        createTriggerUpdate3(c);
        createTriggerUpdate4(c);
        createTriggerDelete(c);
    }

    private void createTriggerInsert(Connection c) throws SQLException {
        /* Conditions: Insertion of non-empty geometry
           Actions   : Insert record into rtree */
        String sql = String.format("CREATE TRIGGER rtree_%s_%s_insert AFTER INSERT ON %s"
                + " WHEN (new.%s NOT NULL AND NOT ST_IsEmpty(NEW.%s))"
                + "BEGIN"
                + " INSERT OR REPLACE INTO rtree_%s_%s VALUES ("
                + " NEW.%s,"
                + " ST_MinX(NEW.%s), ST_MaxX(NEW.%s),"
                + " ST_MinY(NEW.%s), ST_MaxY(NEW.%s)"
                + ");"
                + "END;",
                tableName, columnName, tableName,
                columnName, columnName,
                tableName, columnName,
                idColumn,
                columnName, columnName,
                columnName, columnName);
        execute(c, sql);
    }

    private void createTriggerUpdate1(Connection c) throws SQLException {
        /* Conditions: Update of geometry column to non-empty geometry
                       No row ID change
           Actions   : Update record in rtree */
        String sql = String.format(""
                + "CREATE TRIGGER rtree_%s_%s_update1 AFTER UPDATE OF %s ON %s\r\n"
                + "  WHEN OLD.%s = NEW.%s AND\r\n"
                + "       (NEW.%s NOTNULL AND NOT ST_IsEmpty(NEW.%s))\r\n"
                + "BEGIN\r\n"
                + "  INSERT OR REPLACE INTO rtree_%s_%s VALUES (\r\n"
                + "    NEW.%s,\r\n"
                + "    ST_MinX(NEW.%s), ST_MaxX(NEW.%s),\r\n"
                + "    ST_MinY(NEW.%s), ST_MaxY(NEW.%s)\r\n"
                + "  );\r\n"
                + "END;",
                tableName, columnName, columnName, tableName,
                idColumn, idColumn,
                columnName, columnName,
                tableName, columnName,
                idColumn,
                columnName, columnName,
                columnName, columnName);
        execute(c, sql);
    }

    private void createTriggerUpdate2(Connection c) throws SQLException {
        /* Conditions: Update of geometry column to empty geometry
                       No row ID change
           Actions   : Remove record from rtree */
        String sql = String.format("CREATE TRIGGER rtree_%s_%s_update2 AFTER UPDATE OF %s ON %s"
                + " WHEN OLD.%s = NEW.%s AND"
                + " (NEW.%s ISNULL OR ST_IsEmpty(NEW.%s))"
                + "BEGIN"
                + " DELETE FROM rtree_%s_%s WHERE id = OLD.%s;"
                + "END;",
                tableName, columnName, columnName, tableName,
                idColumn, idColumn,
                columnName, columnName,
                tableName, columnName, idColumn);
        execute(c, sql);
    }

    private void createTriggerUpdate3(Connection c) throws SQLException {
        /* Conditions: Update of any column
                       Row ID change
                       Non-empty geometry
           Actions   : Remove record from rtree for old <i>
                       Insert record into rtree for new <i> */
        String sql = String.format("CREATE TRIGGER rtree_%s_%s_update3 AFTER UPDATE ON %s"
                + " WHEN OLD.%s != NEW.%s AND"
                + " (NEW.%s NOTNULL AND NOT ST_IsEmpty(NEW.%s))"
                + "BEGIN"
                + " DELETE FROM rtree_%s_%s WHERE id = OLD.%s;"
                + " INSERT OR REPLACE INTO rtree_%s_%s VALUES ("
                + " NEW.%s,"
                + " ST_MinX(NEW.%s), ST_MaxX(NEW.%s),"
                + " ST_MinY(NEW.%s), ST_MaxY(NEW.%s)"
                + " );"
                + "END;",
                tableName, columnName, tableName,
                idColumn, idColumn,
                columnName, columnName,
                tableName, columnName, idColumn,
                tableName, columnName,
                idColumn,
                columnName, columnName,
                columnName, columnName);
        execute(c, sql);

    }

    private void createTriggerUpdate4(Connection c) throws SQLException {
        /* Conditions: Update of any column
                       Row ID change
                       Empty geometry
           Actions   : Remove record from rtree for old and new <i> */
        String sql = String.format("CREATE TRIGGER rtree_%s_%s_update4 AFTER UPDATE ON %s"
                + " WHEN OLD.%s != NEW.%s AND"
                + " (NEW.%s ISNULL OR ST_IsEmpty(NEW.%s))"
                + "BEGIN"
                + "  DELETE FROM rtree_%s_%s WHERE id IN (OLD.%s, NEW.%s);"
                + "END;",
                tableName, columnName, tableName,
                idColumn, idColumn,
                columnName, columnName,
                tableName, columnName, idColumn, idColumn);
        execute(c, sql);
    }

    private void createTriggerDelete(Connection c) throws SQLException {
        /* Conditions: Row deleted
           Actions   : Remove record from rtree for old <i> */
        String sql = String.format("CREATE TRIGGER rtree_%s_%s_delete AFTER DELETE ON %s WHEN old.%s NOT NULL"
                + " BEGIN DELETE FROM rtree_%s_%s WHERE id = OLD.%s; END;",
                tableName, columnName, tableName, columnName,
                tableName, columnName, idColumn);
        execute(c, sql);
    }

    private void execute(Connection c, String sql) throws SQLException {
        try (Statement s = c.createStatement()) {
            s.execute(sql);
        }
    }

}
