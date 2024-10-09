package fi.nls.hakunapi.core.param;

import static fi.nls.hakunapi.core.param.DatetimeParam.parse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class DatetimeParamTest {

    @Test
    public void testRFC3339() {
        Instant actual, expected;

        actual = parse("2018-02-12T23:20:50Z", null, "");
        expected = OffsetDateTime.of(2018, 2, 12, 23, 20, 50, 0, ZoneOffset.UTC).toInstant();
        assertEquals(expected, actual);

        actual = parse("2018-02-12T23:20:50.123456789Z", null, "");
        expected = OffsetDateTime.of(2018, 2, 12, 23, 20, 50, 123456789, ZoneOffset.UTC).toInstant();
        assertEquals(expected, actual);

        actual = parse("2018-02-12T23:20:50.150+00:00", null, "");
        expected = OffsetDateTime.of(2018, 2, 12, 23, 20, 50, (int) TimeUnit.MILLISECONDS.toNanos(150), ZoneOffset.UTC).toInstant();
        assertEquals(expected, actual);

        actual = parse("2020-01-01T02:00:00+02:00", null, "");
        expected = OffsetDateTime.of(2020, 01, 01, 2, 0, 0, 0, ZoneOffset.ofHours(2)).toInstant();

        actual = parse("2020-01-01T00:00:00-03:00", null, "");
        expected = OffsetDateTime.of(2020, 01, 01, 0, 0, 0, 0, ZoneOffset.ofHours(-3)).toInstant();

        actual = parse("..", Instant.MAX, "");
        expected = Instant.MAX;
        assertEquals(expected, actual);

        // Allow lowercase z
        actual = parse("2018-02-12T23:20:50z", null, "");
        expected = OffsetDateTime.of(2018, 2, 12, 23, 20, 50, 0, ZoneOffset.UTC).toInstant();
        assertEquals(expected, actual);

        // ^ and t
        actual = parse("2018-02-12t23:20:50Z", null, "");
        expected = OffsetDateTime.of(2018, 2, 12, 23, 20, 50, 0, ZoneOffset.UTC).toInstant();
        assertEquals(expected, actual);

        // minutes and seconds are required (only fractions of a second optional)
        assertThrows(IllegalArgumentException.class, () -> parse("2018-02-12T00Z", null, ""));

        // Offset is required
        assertThrows(IllegalArgumentException.class, () -> parse("2018-02-12T23:20:50", null, ""));

        // date and time not allowed to be separated by space character (questionable)
        assertThrows(IllegalArgumentException.class, () -> parse("2018-02-12 23:20:50Z", null, ""));

        // Disallow hours only offset compared to DateTimeFormatter.ISO_OFFSET_DATE_TIME
        assertThrows(IllegalArgumentException.class, () -> parse("2020-01-01T00:00:00+02", null, ""));

        // Disallow offset seconds compared to DateTimeFormatter.ISO_OFFSET_DATE_TIME
        assertThrows(IllegalArgumentException.class, () -> parse("2020-01-01T00:00:00+02:00:01", null, ""));

        // No extra whitespace allowed
        assertThrows(IllegalArgumentException.class, () -> parse("2018-02-12T23:20:50 Z", null, ""));
        assertThrows(IllegalArgumentException.class, () -> parse("2018-02-12T23:20:50 +02:00", null, ""));

        // We don't support leap seconds as java.time mostly ignores them
    }

}
