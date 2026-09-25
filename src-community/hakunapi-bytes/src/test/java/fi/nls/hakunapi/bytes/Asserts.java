package fi.nls.hakunapi.bytes;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Random;

public class Asserts {

    private Asserts() {
        // Use static methods
    }

    /**
     * Random reads through both overloads at a nonzero dstOff, a sequential scan, and EOF.
     */
    public static void assertReads(byte[] data, ByteSource s) throws IOException {
        assertEquals(data.length, s.size());
        Random r = new Random(42);
        try (ByteReader h = s.open(); Arena arena = Arena.ofConfined()) {
            MemorySegment seg = arena.allocate(20_003);
            byte[] arr = new byte[20_007];
            for (int i = 0; i < 100; i++) {
                int off = r.nextInt(data.length);
                int len = 1 + r.nextInt(20_000);
                int want = Math.min(len, data.length - off);
                assertEquals(want, h.read(off, seg, 3, len));
                assertArrayEquals(slice(data, off, want), seg.asSlice(3, want).toArray(ValueLayout.JAVA_BYTE));
                assertEquals(want, h.read(off, arr, 7, len));
                assertArrayEquals(slice(data, off, want), Arrays.copyOfRange(arr, 7, 7 + want));
            }
            for (int off = 0; off < data.length; off += 777) {
                int want = Math.min(777, data.length - off);
                assertEquals(want, h.read(off, arr, 0, 777));
                assertArrayEquals(slice(data, off, want), Arrays.copyOf(arr, want));
            }
            assertEquals(0, h.read(data.length, arr, 0, 10));
            assertEquals(0, h.read(0, arr, 0, 0));
        }
    }

    public static byte[] slice(byte[] b, long off, int len) {
        return Arrays.copyOfRange(b, (int) off, (int) off + len);
    }

    public static Path resource(String name) throws URISyntaxException {
        return Path.of(Asserts.class.getResource("/" + name).toURI());
    }

}
