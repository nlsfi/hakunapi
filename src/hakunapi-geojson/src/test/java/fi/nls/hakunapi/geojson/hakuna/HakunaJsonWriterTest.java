package fi.nls.hakunapi.geojson.hakuna;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Random;

import org.junit.Test;

public class HakunaJsonWriterTest {

    @Test
    public void testWriteString() throws IOException {
        String kosme = "κόσμε";
        assertArrayEquals(kosme.getBytes(StandardCharsets.UTF_8), writeString(kosme));

        String foo = "äöäÄÖÅ";
        assertArrayEquals(foo.getBytes(StandardCharsets.UTF_8), writeString(foo));

        String bar = "©®";
        assertArrayEquals(bar.getBytes(StandardCharsets.UTF_8), writeString(bar));

        String baz = "🤦🏼‍♂️";
        assertArrayEquals(baz.getBytes(StandardCharsets.UTF_8), writeString(baz));
    }

    private byte[] writeString(String s) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(64);
        try (HakunaJsonWriter json = new HakunaJsonWriter(baos, null)) {
            json.writeUTF8(s);
        }
        return baos.toByteArray();
    }

    @Test
    public void testWriteLocalDateMatchesToString() throws IOException {
        long min = LocalDate.MIN.toEpochDay();
        long max = LocalDate.MAX.toEpochDay();
        Random random = new Random(42);
        for (int i = 0; i < 1000; i++) {
            LocalDate date = LocalDate.ofEpochDay(random.nextLong(min, max));
            assertEquals(quoted(date.toString()), writeValue(json -> json.writeLocalDate(date)));
        }
    }

    @Test
    public void testWriteInstantMatchesToString() throws IOException {
        Instant[] instants = {
                Instant.EPOCH,
                Instant.parse("2026-09-02T12:00:00Z"),
                // Instant#toString elides seconds on a whole minute, so this one
                // renders as "2026-09-02T12:00Z" - the elision must be preserved
                Instant.parse("2026-09-02T12:00:00Z").truncatedTo(ChronoUnit.MINUTES),
                Instant.parse("2026-09-02T12:00:00.123Z"),
                Instant.parse("2026-09-02T12:00:00.123456789Z"),
                Instant.parse("1969-12-31T23:59:59Z"),
                Instant.MIN,
                Instant.MAX
        };
        for (Instant instant : instants) {
            assertEquals(quoted(instant.toString()), writeValue(json -> json.writeInstant(instant)));
        }
        // The widest rendering there is, so the reused StringBuilder never grows
        assertEquals(37, Instant.MAX.toString().length());
    }

    @Test
    public void testWriteDatesInsideArrayAddCommas() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(64);
        try (HakunaJsonWriter json = new HakunaJsonWriter(baos, null)) {
            json.writeStartArray();
            json.writeLocalDate(LocalDate.of(1987, 12, 31));
            json.writeLocalDate(LocalDate.of(1988, 1, 1));
            json.writeInstant(Instant.parse("2026-09-02T12:00:00Z"));
            json.writeEndArray();
        }
        assertEquals("[\"1987-12-31\",\"1988-01-01\",\"2026-09-02T12:00:00Z\"]",
                baos.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void testWriteDatesFlushWhenBufferIsNearlyFull() throws IOException {
        // The value must not straddle the buffer boundary
        ByteArrayOutputStream baos = new ByteArrayOutputStream(16384);
        LocalDate date = LocalDate.of(1987, 12, 31);
        Instant instant = Instant.parse("2026-09-02T12:00:00.123456789Z");
        StringBuilder expected = new StringBuilder("[");
        try (HakunaJsonWriter json = new HakunaJsonWriter(baos, null)) {
            json.writeStartArray();
            for (int i = 0; i < 500; i++) {
                json.writeLocalDate(date);
                json.writeInstant(instant);
                if (i > 0) {
                    expected.append(',');
                }
                expected.append('"').append(date).append("\",\"").append(instant).append('"');
            }
            json.writeEndArray();
        }
        assertEquals(expected.append(']').toString(), baos.toString(StandardCharsets.UTF_8));
    }

    private String quoted(String s) {
        return '"' + s + '"';
    }

    private String writeValue(ValueWriter w) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(64);
        try (HakunaJsonWriter json = new HakunaJsonWriter(baos, null)) {
            json.writeStartArray();
            w.write(json);
            json.writeEndArray();
        }
        String s = baos.toString(StandardCharsets.UTF_8);
        return s.substring(1, s.length() - 1);
    }

    private interface ValueWriter {
        void write(HakunaJsonWriter json) throws IOException;
    }

}
