package fi.nls.hakunapi.tiles.generator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

public final class TileGzip {

    private TileGzip() {}

    public static byte[] compress(byte[] tile) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(Math.max(64, tile.length));
        try (GZIPOutputStream gz = new FastCompression(out)) {
            gz.write(tile);
        }
        return out.toByteArray();
    }

    /** GZIPOutputStream has no level constructor; the level is set on its inherited Deflater. */
    private static class FastCompression extends GZIPOutputStream {

        private static final int LEVEL = 4;

        FastCompression(ByteArrayOutputStream out) throws IOException {
            super(out, 8192);
            def.setLevel(LEVEL);
        }

    }

}
