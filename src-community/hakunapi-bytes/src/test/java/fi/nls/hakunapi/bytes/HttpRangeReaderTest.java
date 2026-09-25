package fi.nls.hakunapi.bytes;

import static fi.nls.hakunapi.bytes.Asserts.assertReads;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

public class HttpRangeReaderTest {

    private static final int SIZE = 300_000;

    @Test
    public void testReads() throws Exception {
        byte[] data = RecordingSource.generate(SIZE);
        try (RangeHttpServer server = new RangeHttpServer(data);
                HttpRangeReader r = new HttpRangeReader(server.uri())) {
            assertReads(data, r);
        }
    }

    @Test
    public void testOneRangeRequestPerRead() throws Exception {
        byte[] data = RecordingSource.generate(SIZE);
        try (RangeHttpServer server = new RangeHttpServer(data);
                HttpRangeReader r = new HttpRangeReader(server.uri());
                ByteReader h = r.open()) {
            byte[] b = new byte[1000];
            h.read(5000, b, 0, 1000);
            h.read(SIZE - 500, b, 0, 1000);
            h.read(SIZE, b, 0, 1000);
            assertEquals(List.of("5000-5999", "299500-299999"), server.ranges);
        }
    }

    @Test
    public void testShortRangeIsAnError() throws Exception {
        try (RangeHttpServer server = new RangeHttpServer(RecordingSource.generate(SIZE));
                HttpRangeReader r = new HttpRangeReader(server.uri());
                ByteReader h = r.open()) {
            server.halfRanges = true;
            IOException e = assertThrows(IOException.class, () -> h.read(1000, new byte[1000], 0, 1000));
            assertTrue(e.getMessage().startsWith("Short read"));
        }
    }

}
