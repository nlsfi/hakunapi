package fi.nls.hakunapi.gpkg.function;

import java.nio.ByteOrder;

public final class Bytes {

    private Bytes() {}

    public static final double readDouble(byte[] b, int off, ByteOrder bo) {
        return bo == ByteOrder.BIG_ENDIAN ? readDoubleBE(b, off) : readDoubleLE(b, off);
    }

    public static final double readDoubleBE(byte[] b, int off) {
        return Double.longBitsToDouble(readLongBE(b, off));
    }

    public static final double readDoubleLE(byte[] b, int off) {
        return Double.longBitsToDouble(readLongLE(b, off));
    }

    public static final long readLongBE(byte[] b, int off) {
        int h = ((b[off++] & 0xff) << 24) |
                ((b[off++] & 0xff) << 16) |
                ((b[off++] & 0xff) <<  8) |
                ((b[off++] & 0xff) <<  0);
        int l = ((b[off++] & 0xff) << 24) |
                ((b[off++] & 0xff) << 16) |
                ((b[off++] & 0xff) <<  8) |
                ((b[off  ] & 0xff) <<  0);
        return (((long) h) << 32L) | ((long) l) & 0xffffffffL;
    }

    public static final long readLongLE(byte[] b, int off) {
        int l = ((b[off++] & 0xff) <<  0) |
                ((b[off++] & 0xff) <<  8) |
                ((b[off++] & 0xff) << 16) |
                ((b[off++] & 0xff) << 24);
        int h = ((b[off++] & 0xff) <<  0) |
                ((b[off++] & 0xff) <<  8) |
                ((b[off++] & 0xff) << 16) |
                ((b[off  ] & 0xff) << 24);
        return (((long) h) << 32L) | ((long) l) & 0xffffffffL;
    }

}
