package fi.nls.hakunapi.source.gpkg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;

import org.junit.Test;

public class ResultSetValueProviderDateTest {

    /**
     * The hand-rolled parse has to accept exactly the strings LocalDate.parse
     * accepts and produce exactly its dates, since that is what it replaced.
     */
    @Test
    public void agreesWithLocalDateParse() throws Exception {
        for (int year = 0; year <= 9999; year += 373) {
            for (int month = 1; month <= 12; month += 5) {
                for (int day = 1; day <= 28; day += 9) {
                    String text = String.format("%04d-%02d-%02d", year, month, day);
                    assertEquals(LocalDate.parse(text), read("'" + text + "'"));
                }
            }
        }
    }

    @Test
    public void nullStaysNull() throws Exception {
        assertNull(read("null"));
    }

    /**
     * Everything the parse rejects falls through to the driver, which is what
     * decided these before: a datetime and an epoch INTEGER still convert, and
     * shapes that are not dates still fail.
     */
    @Test
    public void fallsBackToTheDriver() throws Exception {
        assertEquals(LocalDate.of(2025, 3, 25), read("'2025-03-25 12:34:56'"));
        assertEquals(LocalDate.of(2025, 3, 25), read("1742860800000")); // epoch millis
        assertEquals(LocalDate.of(1970, 1, 21), read("1742860800"));    // epoch seconds

        // Right shape but not a real date, unpadded, and multi-byte UTF-8 that is
        // ten bytes but not ten characters.
        for (String literal : new String[] {"'2025-02-30'", "'2025-3-25'", "'äääää'"}) {
            try {
                read(literal);
                throw new AssertionError("expected " + literal + " to be rejected");
            } catch (RuntimeException expected) {
                // the driver's rejection, surfaced
            }
        }
    }

    private static LocalDate read(String literal) throws Exception {
        try (Connection c = DriverManager.getConnection("jdbc:sqlite::memory:");
                Statement s = c.createStatement()) {
            try (ResultSet rs = s.executeQuery("select " + literal + " as d")) {
                assertTrue(rs.next());
                return new ResultSetValueProvider(rs, 1).getLocalDate(0);
            }
        }
    }
}
