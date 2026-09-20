package fi.nls.hakunapi.source.gpkg;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;

import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.WKTReader;

import fi.nls.hakunapi.core.geom.HakunaGeometryFactory;
import fi.nls.hakunapi.gpkg.GPKGGeometry;

public class HakunaGeometryGPKGBufferTest {

    private static final int SRID = 3067;

    private static final String WKT =
            "POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0), (2 2, 4 2, 4 4, 2 4, 2 2))";

    @Test
    public void toWkbWritesExactlyGetWkbLengthBytes() throws Exception {
        HakunaGeometryGPKG g = gpkg(WKT, false);
        byte[] wkb = new byte[g.getWKBLength()];
        g.toWKB(wkb, 0);

        Geometry read = new WKBReader(HakunaGeometryFactory.GF).read(wkb);
        Geometry expected = new WKTReader(HakunaGeometryFactory.GF).read(WKT);
        assertEquals(expected, read);
    }

    @Test
    public void toWkbHonoursTheOffset() throws Exception {
        HakunaGeometryGPKG g = gpkg(WKT, false);
        int len = g.getWKBLength();
        byte[] flat = new byte[len];
        g.toWKB(flat, 0);

        byte[] offset = new byte[8 + len];
        g.toWKB(offset, 8);
        for (int i = 0; i < 8; i++) {
            assertEquals("byte " + i + " before the offset must be untouched", 0, offset[i]);
        }
        byte[] written = new byte[len];
        System.arraycopy(offset, 8, written, 0, len);
        assertArrayEquals(flat, written);
    }

    @Test
    public void aDirectBufferReadsTheSameAsAHeapOne() throws Exception {
        HakunaGeometryGPKG heap = gpkg(WKT, false);
        HakunaGeometryGPKG direct = gpkg(WKT, true);

        assertEquals(heap.srid, direct.srid);
        assertEquals(heap.type, direct.type);
        assertEquals(heap.dimension, direct.dimension);
        assertEquals(heap.wkbOffset, direct.wkbOffset);
        assertEquals(heap.dataStart, direct.dataStart);
        assertEquals(heap.getWKBLength(), direct.getWKBLength());

        byte[] a = new byte[heap.getWKBLength()];
        byte[] b = new byte[direct.getWKBLength()];
        heap.toWKB(a, 0);
        direct.toWKB(b, 0);
        assertArrayEquals(a, b);

        assertEquals(heap.toJTSGeometry(), direct.toJTSGeometry());
        assertEquals(heap.boundedBy(), direct.boundedBy());
    }

    private HakunaGeometryGPKG gpkg(String wkt, boolean direct) throws Exception {
        Geometry jts = new WKTReader(HakunaGeometryFactory.GF).read(wkt);
        byte[] wkb = new WKBWriter(2, false).write(jts);

        byte[] blob = new byte[GPKGGeometry.LENGHT_NO_ENVELOPE + wkb.length];
        GPKGGeometry.write(SRID, blob);
        System.arraycopy(wkb, 0, blob, GPKGGeometry.LENGHT_NO_ENVELOPE, wkb.length);

        if (!direct) {
            return new HakunaGeometryGPKG(ByteBuffer.wrap(blob));
        }
        ByteBuffer bb = ByteBuffer.allocateDirect(blob.length);
        bb.put(blob);
        bb.flip();
        return new HakunaGeometryGPKG(bb);
    }

}
