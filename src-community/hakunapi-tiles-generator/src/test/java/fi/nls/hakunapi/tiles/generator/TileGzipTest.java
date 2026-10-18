package fi.nls.hakunapi.tiles.generator;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

import org.junit.Test;

public class TileGzipTest {

    private static byte[] gunzip(byte[] compressed) throws Exception {
        try (GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(compressed))) {
            return in.readAllBytes();
        }
    }

    @Test
    public void roundTripsTilesIncludingAnEmptyOne() throws Exception {
        byte[] tile = new byte[4096];
        for (int i = 0; i < tile.length; i++) {
            tile[i] = (byte) (i * 31);
        }

        assertArrayEquals(tile, gunzip(TileGzip.compress(tile)));
        assertArrayEquals(new byte[0], gunzip(TileGzip.compress(new byte[0])));
    }

    @Test
    public void outputIsAReproducibleGzipMember() throws Exception {
        byte[] tile = "the same tile bytes, twice".getBytes(StandardCharsets.UTF_8);
        byte[] out = TileGzip.compress(tile);

        assertArrayEquals(out, TileGzip.compress(tile));
        assertEquals((byte) 0x1f, out[0]);
        assertEquals((byte) 0x8b, out[1]);
        for (int i = 4; i < 8; i++) {
            assertEquals(0, out[i]);
        }
        assertEquals((byte) 255, out[9]);
    }

}
