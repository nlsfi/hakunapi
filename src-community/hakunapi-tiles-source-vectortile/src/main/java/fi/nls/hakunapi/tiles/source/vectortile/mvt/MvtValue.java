package fi.nls.hakunapi.tiles.source.vectortile.mvt;

final class MvtValue {
    static final int STRING = 1;
    static final int FLOAT = 2;
    static final int DOUBLE = 3;
    static final int INT = 4;
    static final int UINT = 5;
    static final int SINT = 6;
    static final int BOOL = 7;

    private MvtValue() {}

    static void prependString(BackwardProtoBuf b, byte[] utf8) {
        b.prependBytes(utf8, 0, utf8.length);
        b.prependLengthDelimitedHeader(STRING, utf8.length);
    }

    static void prependDouble(BackwardProtoBuf b, double v) {
        b.prependFixed64Field(DOUBLE, Double.doubleToLongBits(v));
    }

    static void prependFloat(BackwardProtoBuf b, float v) {
        b.prependFixed32Field(FLOAT, Float.floatToIntBits(v));
    }

    static void prependInt(BackwardProtoBuf b, long v) {
        b.prependVarintField(INT, v);
    }

    static void prependUint(BackwardProtoBuf b, long v) {
        b.prependVarintField(UINT, v);
    }

    static void prependSint(BackwardProtoBuf b, long v) {
        b.prependVarintField(SINT, (v << 1) ^ (v >> 63));
    }

    static void prependBool(BackwardProtoBuf b, boolean v) {
        b.prependVarintField(BOOL, v ? 1 : 0);
    }
}
