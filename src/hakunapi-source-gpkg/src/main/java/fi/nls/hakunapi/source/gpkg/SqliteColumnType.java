package fi.nls.hakunapi.source.gpkg;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.sqlite.core.CoreStatement;
import org.sqlite.core.DB;

public final class SqliteColumnType {

    private static final int SQLITE_NULL = 5;

    private final CoreStatement stmt;

    private SqliteColumnType(CoreStatement stmt) {
        this.stmt = stmt;
    }

    public static SqliteColumnType of(ResultSet rs) {
        try {
            // unwrap because the pool hands out a proxy
            Statement s = rs.getStatement();
            if (!s.isWrapperFor(CoreStatement.class)) {
                return null;
            }
            return new SqliteColumnType(s.unwrap(CoreStatement.class));
        } catch (SQLException e) {
            return null;
        }
    }

    /** @param i zero-based, as SQLite counts columns (JDBC is one-based) */
    public boolean isNull(int i) throws SQLException {
        return type(i) == SQLITE_NULL;
    }

    public int type(int i) throws SQLException {
        return stmt.pointer.safeRunInt((DB db, long ptr) -> db.column_type(ptr, i));
    }

}
