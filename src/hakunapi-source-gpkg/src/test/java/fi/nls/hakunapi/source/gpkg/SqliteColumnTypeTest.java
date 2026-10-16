package fi.nls.hakunapi.source.gpkg;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.Test;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class SqliteColumnTypeTest {

    private static final String QUERY = "select NULL, 1, 'x', NULL";

    @Test
    public void readsColumnTypeOnPlainAndPooledConnections() throws Exception {
        try (Connection c = DriverManager.getConnection("jdbc:sqlite::memory:");
                Statement s = c.createStatement();
                ResultSet rs = s.executeQuery(QUERY)) {
            assertTrue(rs.next());
            SqliteColumnType type = SqliteColumnType.of(rs);
            assertNotNull(type);
            assertNullFlags(type);
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite::memory:");
        config.setMaximumPoolSize(1);
        HikariDataSource ds = new HikariDataSource(config);
        try {
            try (Connection c = ds.getConnection();
                    Statement s = c.createStatement();
                    ResultSet rs = s.executeQuery(QUERY)) {
                assertTrue(rs.next());
                SqliteColumnType type = SqliteColumnType.of(rs);
                assertNotNull(type);
                assertNullFlags(type);
            }

            try (Connection c = ds.getConnection();
                    Statement s = c.createStatement();
                    ResultSet rs = s.executeQuery(QUERY)) {
                assertTrue(rs.next());
                assertNullFlags(new ResultSetValueProvider(rs, 4));
            }
        } finally {
            ds.close();
        }
    }

    private static void assertNullFlags(SqliteColumnType type) throws Exception {
        assertTrue(type.isNull(0));
        assertFalse(type.isNull(1));
        assertFalse(type.isNull(2));
        assertTrue(type.isNull(3));
    }

    private static void assertNullFlags(ResultSetValueProvider values) {
        assertTrue(values.isNull(0));
        assertFalse(values.isNull(1));
        assertFalse(values.isNull(2));
        assertTrue(values.isNull(3));
    }
}
