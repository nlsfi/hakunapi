package fi.nls.hakunapi.tiles.mvt;

/** Just enough protobuf to walk an encoded vector tile without decoding it. */
public final class Protobuf {

    public static final int WIRE_VARINT = 0;
    public static final int WIRE_FIXED64 = 1;
    public static final int WIRE_LEN = 2;
    public static final int WIRE_FIXED32 = 5;

    private Protobuf() {}

    /** Value in the low 40 bits, offset just past it in the high 24, so one scan yields both; both are checked. */
    public static long varint(byte[] b, int pos) {
        long value = 0;
        int shift = 0;
        for (int i = pos; i < b.length; i++) {
            byte cur = b[i];
            value |= ((long) (cur & 0x7f)) << shift;
            if ((cur & 0x80) == 0) {
                int end = i + 1;
                if (value > 0xFF_FFFF_FFFFL) {
                    throw new IllegalArgumentException("Varint too large in MVT tile: " + value);
                }
                if (end > 0xFF_FFFF) {
                    throw new IllegalArgumentException("MVT tile too large to walk: " + end + " bytes");
                }
                return ((long) end << 40) | value;
            }
            shift += 7;
        }
        throw new IllegalArgumentException("Truncated varint in MVT tile");
    }

    public static int value(long varint) {
        return (int) (varint & 0xFF_FFFF_FFFFL);
    }

    public static int end(long varint) {
        return (int) (varint >>> 40);
    }

    public static int field(int tag) {
        return tag >>> 3;
    }

    public static int wire(int tag) {
        return tag & 0x7;
    }

    public static int skip(byte[] b, int pos, int wire) {
        switch (wire) {
        case WIRE_VARINT:
            return end(varint(b, pos));
        case WIRE_FIXED64:
            return pos + 8;
        case WIRE_LEN:
            long len = varint(b, pos);
            return end(len) + value(len);
        case WIRE_FIXED32:
            return pos + 4;
        default:
            throw new IllegalArgumentException("Unsupported protobuf wire type " + wire + " in MVT tile");
        }
    }

}
