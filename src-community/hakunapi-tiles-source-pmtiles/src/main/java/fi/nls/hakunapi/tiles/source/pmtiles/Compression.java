package fi.nls.hakunapi.tiles.source.pmtiles;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

public final class Compression {
    public static final byte UNKNOWN = 0;
    public static final byte NONE = 1;
    public static final byte GZIP = 2;
    public static final byte BROTLI = 3;
    public static final byte ZSTD = 4;

    private static final int MAX_DECOMPRESSED = 64 << 20;

    private Compression() {
    }

    public static byte[] decode(byte type, byte[] bytes) throws IOException {
        switch (type) {
            case NONE:
            case UNKNOWN:
                return bytes;
            case GZIP:
                return gunzip(bytes);
            default:
                throw new IOException("Unsupported PMTiles internal compression: " + type
                        + " (only none/gzip supported for directories)");
        }
    }

    public static String contentEncoding(byte type) {
        switch (type) {
            case GZIP:
                return "gzip";
            case BROTLI:
                return "br";
            case ZSTD:
                return "zstd";
            default:
                return null;
        }
    }

    private static byte[] gunzip(byte[] bytes) throws IOException {
        try (GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
            ByteArrayOutputStream out = new ByteArrayOutputStream((int) Math.min(bytes.length * 2L, 1 << 20));
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) {
                if (out.size() + n > MAX_DECOMPRESSED) {
                    throw new IOException("PMTiles gzip data inflates past " + MAX_DECOMPRESSED + " bytes");
                }
                out.write(buf, 0, n);
            }
            return out.toByteArray();
        }
    }
}
