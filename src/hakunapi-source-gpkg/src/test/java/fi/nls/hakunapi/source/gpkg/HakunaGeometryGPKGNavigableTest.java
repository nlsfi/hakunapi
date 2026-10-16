package fi.nls.hakunapi.source.gpkg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.WKTReader;

import fi.nls.hakunapi.core.geom.HakunaGeometryFactory;
import fi.nls.hakunapi.core.geom.HakunaGeometryJTS;
import fi.nls.hakunapi.core.geom.NavigableHakunaGeometry;
import fi.nls.hakunapi.gpkg.GPKGGeometry;

public class HakunaGeometryGPKGNavigableTest {

    private static final int SRID = 3067;

    @Test
    public void lineStringMatchesJts() throws Exception {
        assertSameCoordinates("LINESTRING (1 2, 3 4, 5 6, 7 8)");
    }

    @Test
    public void polygonWithHoleMatchesJts() throws Exception {
        assertSameCoordinates("POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0),"
                + " (2 2, 4 2, 4 4, 2 4, 2 2))");
    }

    @Test
    public void multiLineStringMatchesJts() throws Exception {
        assertSameCoordinates("MULTILINESTRING ((1 1, 2 2), (10 10, 20 20, 30 30))");
    }

    @Test
    public void pointMatchesJts() throws Exception {
        assertSameCoordinates("POINT (385000 6672000)");
    }

    @Test
    public void navigableWalksTheBlobInPlace() throws Exception {
        HakunaGeometryGPKG g = gpkg("LINESTRING (1 2, 3 4)");
        ByteOrder before = g.bb.order();
        int position = g.bb.position();

        NavigableHakunaGeometry nav = g.toNavigable();
        double[] xy = new double[4];
        nav.copyRingXY(0, xy, 0, 0, 2);

        assertEquals(1.0, xy[0], 0.0);
        assertEquals(2.0, xy[1], 0.0);
        assertEquals(3.0, xy[2], 0.0);
        assertEquals(4.0, xy[3], 0.0);
        assertEquals(before, g.bb.order());
        assertEquals(position, g.bb.position());
    }

    private void assertSameCoordinates(String wkt) throws Exception {
        Geometry jts = new WKTReader(HakunaGeometryFactory.GF).read(wkt);
        NavigableHakunaGeometry viaBlob = gpkg(wkt).toNavigable();
        NavigableHakunaGeometry viaJts = new HakunaGeometryJTS(jts).toNavigable();

        assertEquals(viaJts.getNumGeometries(), viaBlob.getNumGeometries());
        for (int p = 0; p < viaJts.getNumGeometries(); p++) {
            NavigableHakunaGeometry a = viaJts.getGeometryN(p);
            NavigableHakunaGeometry b = viaBlob.getGeometryN(p);
            assertEquals("rings in part " + p, a.getNumRings(), b.getNumRings());
            for (int r = 0; r < a.getNumRings(); r++) {
                int n = a.getRingSize(r);
                assertEquals("size of ring " + p + "/" + r, n, b.getRingSize(r));
                assertTrue("ring " + p + "/" + r + " is empty", n > 0);

                double[] expected = new double[2 * n];
                double[] actual = new double[2 * n];
                a.copyRingXY(r, expected, 0, 0, n);
                b.copyRingXY(r, actual, 0, 0, n);
                for (int i = 0; i < expected.length; i++) {
                    assertEquals("ring " + p + "/" + r + " ordinate " + i,
                            expected[i], actual[i], 0.0);
                }
            }
        }
    }

    private HakunaGeometryGPKG gpkg(String wkt) throws Exception {
        Geometry jts = new WKTReader(HakunaGeometryFactory.GF).read(wkt);
        byte[] wkb = new WKBWriter(2, false).write(jts);

        byte[] blob = new byte[GPKGGeometry.LENGHT_NO_ENVELOPE + wkb.length];
        GPKGGeometry.write(SRID, blob);
        System.arraycopy(wkb, 0, blob, GPKGGeometry.LENGHT_NO_ENVELOPE, wkb.length);

        return new HakunaGeometryGPKG(ByteBuffer.wrap(blob));
    }

}
