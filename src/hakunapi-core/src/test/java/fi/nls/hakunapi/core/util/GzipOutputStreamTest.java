package fi.nls.hakunapi.core.util;

import static org.junit.Assert.assertArrayEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

import org.junit.Test;

public class GzipOutputStreamTest {

    @Test
    public void testAgainstJDK() throws IOException {
        byte[] input = generateRandomData(64 * 1024);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (OutputStream out = new GZIPOutputStream(baos)) {
            out.write(input);
        }
        byte[] expected = baos.toByteArray();

        baos.reset();
        Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
        try (OutputStream out = new GzipOutputStream(baos, deflater, 8192)) {
            out.write(input);
        }
        byte[] actual = baos.toByteArray();

        // GZIP "OS" header flag
        // Changed from 0 to 255 in Java 16, see https://bugs.openjdk.org/browse/JDK-8244706
        // GzipOutputStream outputs 255, JDK 11 outputs 0 -- ignore checking the OS header flag
        actual[9] = expected[9];

        assertArrayEquals(expected, actual);
    }

    private byte[] generateRandomData(int n) {
        byte[] b = new byte[n];
        ThreadLocalRandom r = ThreadLocalRandom.current();
        ByteBuffer bb = ByteBuffer.wrap(b);
        while (bb.hasRemaining()) {
            bb.putLong(r.nextLong());
        }
        return b;
    }

}
