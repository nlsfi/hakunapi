package fi.nls.hakunapi.gpkg;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.locationtech.jts.geom.Envelope;

public class GPKGGeometry {

    public static final int LENGHT_NO_ENVELOPE = 8;
    public static final int LENGHT_XY_ENVELOPE = 40;
    public static final int LENGHT_XYZ_ENVELOPE = 56;
    public static final int LENGHT_XYM_ENVELOPE = 56;
    public static final int LENGHT_XYZM_ENVELOPE = 72;

    public static final byte BYTE_ORDER_BE = 0;
    public static final byte BYTE_ORDER_LE = 1;

    public static final byte NO_ENVELOPE = 0;
    public static final byte XY_ENVELOPE = 1;
    public static final byte XYZ_ENVELOPE = 2;
    public static final byte XYM_ENVELOPE = 3;
    public static final byte XYZM_ENVELOPE = 4;

    public static byte[] write(int srid, byte[] buf) {
        ByteOrder nativeOrder = ByteOrder.nativeOrder();
        byte flags = nativeOrder == ByteOrder.BIG_ENDIAN ? BYTE_ORDER_BE : BYTE_ORDER_LE;

        ByteBuffer bb = ByteBuffer.wrap(buf);
        bb.order(nativeOrder);
        bb.put((byte) 'G'); // Magic
        bb.put((byte) 'P'); // Magic
        bb.put((byte) 0); // version, 0 = version 1
        bb.put(flags);
        bb.putInt(srid);

        return buf;
    }

    public static byte[] write(int srid, Envelope envelope, byte[] buf) {
        ByteOrder nativeOrder = ByteOrder.nativeOrder();
        byte flags = nativeOrder == ByteOrder.BIG_ENDIAN ? BYTE_ORDER_BE : BYTE_ORDER_LE;
        flags |= XY_ENVELOPE << 1;

        ByteBuffer bb = ByteBuffer.wrap(buf);
        bb.order(nativeOrder);
        bb.put(0, (byte) 'G'); // Magic
        bb.put(1, (byte) 'P'); // Magic
        bb.put(2, (byte) 0); // version, 0 = version 1
        bb.put(3, flags);
        bb.putInt(4, srid);
        bb.putDouble(8, envelope.getMinX());
        bb.putDouble(16, envelope.getMaxX());
        bb.putDouble(24, envelope.getMinY());
        bb.putDouble(32, envelope.getMaxY());

        return buf;
    }

    public static boolean isEmpty(byte flags) {
        return (flags & (1 << 4)) != 0;
    }

    public static int getEnvelopeIndicatorCode(byte flags) {
        return (flags >> 1) & 0x7;
    }

    public static ByteOrder getByteOrder(byte flags) {
        return (flags & 1) == BYTE_ORDER_LE ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
    }

    public static int getWKBOffset(int envelopeIndicator) {
        switch (envelopeIndicator) {
        case NO_ENVELOPE:
            return 8 + LENGHT_NO_ENVELOPE;
        case XY_ENVELOPE:
            return 8 + LENGHT_XY_ENVELOPE;
        case XYZ_ENVELOPE:
            return 8 + LENGHT_XYZ_ENVELOPE;
        case XYM_ENVELOPE:
            return 8 + LENGHT_XYM_ENVELOPE;
        case XYZM_ENVELOPE:
            return 8 + LENGHT_XYZM_ENVELOPE;
        default:
            throw new IllegalArgumentException("Invalid value");
        }
    }

}