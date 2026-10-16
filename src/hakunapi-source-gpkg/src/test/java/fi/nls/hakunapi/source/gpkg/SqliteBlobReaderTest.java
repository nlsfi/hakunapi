package fi.nls.hakunapi.source.gpkg;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.Test;

public class SqliteBlobReaderTest {

    private static final byte[] BLOB = {0x47, 0x50, 0x00, 0x03, 0x11, 0x22, 0x33, 0x44};

    @Test
    public void readsTheSameBytesAsGetBytes() throws Exception {
        try (Connection c = memoryDb()) {
            try (PreparedStatement ps = c.prepareStatement("SELECT g FROM t ORDER BY id")) {
                SqliteBlobReader reader = SqliteBlobReader.of(ps);
                assertNotNull("driver should expose the zero-copy route", reader);

                ResultSet rs = ps.executeQuery();

                assertTrue(rs.next());
                assertNull("NULL must read as null", reader.read(0));
                assertNull(rs.getBytes(1));

                assertTrue(rs.next());
                ByteBuffer empty = reader.read(0);
                assertNotNull("an empty blob is not NULL", empty);
                assertEquals(0, empty.remaining());
                assertEquals(0, rs.getBytes(1).length);

                assertTrue(rs.next());
                ByteBuffer bb = reader.read(0);
                byte[] read = new byte[bb.remaining()];
                bb.get(read);
                assertArrayEquals(BLOB, read);
            }
        }
    }

    @Test
    public void unknownStatementYieldsNoReader() throws Exception {
        assertNull(SqliteBlobReader.of(null));
    }

    private Connection memoryDb() throws Exception {
        Connection c = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement s = c.createStatement()) {
            s.execute("CREATE TABLE t (id INTEGER, g BLOB)");
            s.execute("INSERT INTO t VALUES (1, NULL)");
            s.execute("INSERT INTO t VALUES (2, x'')");
            s.execute("INSERT INTO t VALUES (3, x'4750000311223344')");
        }
        return c;
    }

}
