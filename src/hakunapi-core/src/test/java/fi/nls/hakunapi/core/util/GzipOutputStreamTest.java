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
        byte[] input = generateRandomData(1024 * 1024);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream out = new GZIPOutputStream(baos, 8192);
        out.write(input);
        out.close();
        byte[] expected = baos.toByteArray();

        baos = new ByteArrayOutputStream();
        Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
        out = new GzipOutputStream(baos, deflater, 8192);
        out.write(input);
        out.close();
        byte[] actual = baos.toByteArray();

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
