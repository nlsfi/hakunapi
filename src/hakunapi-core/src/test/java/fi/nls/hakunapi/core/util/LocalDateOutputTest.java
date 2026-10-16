package fi.nls.hakunapi.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Random;

import org.junit.Test;

public class LocalDateOutputTest {

    @Test
    public void testEdgeYears() {
        // LocalDate#toString pads to four digits and prefixes '+' beyond them
        assertEquals("0000-01-02", format(LocalDate.of(0, 1, 2)));
        assertEquals("0001-01-02", format(LocalDate.of(1, 1, 2)));
        assertEquals("0999-12-31", format(LocalDate.of(999, 12, 31)));
        assertEquals("1000-01-01", format(LocalDate.of(1000, 1, 1)));
        assertEquals("9999-12-31", format(LocalDate.of(9999, 12, 31)));
        assertEquals("+10000-01-01", format(LocalDate.of(10000, 1, 1)));
        assertEquals("-0001-01-02", format(LocalDate.of(-1, 1, 2)));
        assertEquals("-0999-12-31", format(LocalDate.of(-999, 12, 31)));
        assertEquals("-1000-01-01", format(LocalDate.of(-1000, 1, 1)));
        assertEquals("-10000-01-01", format(LocalDate.of(-10000, 1, 1)));
    }

    @Test
    public void testMatchesToStringAtBounds() {
        LocalDate min = LocalDate.MIN;
        LocalDate max = LocalDate.MAX;
        assertEquals(min.toString(), format(min));
        assertEquals(max.toString(), format(max));
        assertTrue(min.toString().length() <= LocalDateOutput.MAX_BYTE_LEN);
        assertTrue(max.toString().length() <= LocalDateOutput.MAX_BYTE_LEN);
    }

    @Test
    public void testMatchesToStringForEveryDayOfALeapYear() {
        LocalDate date = LocalDate.of(2024, 1, 1);
        for (int i = 0; i < 366; i++) {
            assertEquals(date.toString(), format(date));
            date = date.plusDays(1);
        }
    }

    @Test
    public void testMatchesToStringForRandomDates() {
        long min = LocalDate.MIN.toEpochDay();
        long max = LocalDate.MAX.toEpochDay();
        Random random = new Random(42);
        for (int i = 0; i < 100_000; i++) {
            LocalDate date = LocalDate.ofEpochDay(random.nextLong(min, max));
            assertEquals(date.toString(), format(date));
        }
    }

    @Test
    public void testWritesAtOffsetAndReturnsEndOffset() {
        byte[] buf = new byte[32];
        buf[0] = '"';
        int end = LocalDateOutput.outputLocalDate(LocalDate.of(1987, 12, 31), buf, 1);
        buf[end] = '"';
        assertEquals("\"1987-12-31\"", new String(buf, 0, end + 1, StandardCharsets.US_ASCII));
    }

    private String format(LocalDate date) {
        byte[] buf = new byte[LocalDateOutput.MAX_BYTE_LEN];
        int len = LocalDateOutput.outputLocalDate(date, buf, 0);
        return new String(buf, 0, len, StandardCharsets.US_ASCII);
    }

}
