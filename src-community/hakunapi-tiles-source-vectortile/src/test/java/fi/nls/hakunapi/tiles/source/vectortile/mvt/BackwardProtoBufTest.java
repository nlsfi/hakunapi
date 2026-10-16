package fi.nls.hakunapi.tiles.source.vectortile.mvt;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class BackwardProtoBufTest {

    @Test
    public void varintLargeRoundTrips() {
        long v = 0xDEADBEEFCAFEL;
        BackwardProtoBuf b = new BackwardProtoBuf();
        b.prependVarint(v);
        byte[] enc = b.toByteArray();
        assertEquals(v, readVarint(enc));
    }

    @Test
    public void tagThenBodyIsForwardOrder() {
        BackwardProtoBuf b = new BackwardProtoBuf();
        b.prependBytes(new byte[] {(byte) 0xAA, (byte) 0xBB}, 0, 2);
        b.prependLengthDelimitedHeader(4, 2);
        assertArrayEquals(new byte[] {0x22, 0x02, (byte) 0xAA, (byte) 0xBB}, b.toByteArray());
    }

    private static long readVarint(byte[] a) {
        long result = 0;
        int shift = 0;
        for (byte value : a) {
            int bb = value & 0xff;
            result |= (long) (bb & 0x7f) << shift;
            if ((bb & 0x80) == 0) {
                break;
            }
            shift += 7;
        }
        return result;
    }
}
