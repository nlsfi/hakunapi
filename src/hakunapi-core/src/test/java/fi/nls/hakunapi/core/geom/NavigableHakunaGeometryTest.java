package fi.nls.hakunapi.core.geom;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.nio.ByteOrder;

import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ByteOrderValues;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.WKTReader;

public class NavigableHakunaGeometryTest {

    private static final WKTReader WKT = new WKTReader();

    private static Geometry g(String wkt) throws ParseException {
        return WKT.read(wkt);
    }

    private void crossCheck(String wkt) throws Exception {
        Geometry jts = g(wkt);
        NavigableHakunaGeometry nav = new HakunaGeometryJTS(jts).toNavigable();
        for (ByteOrder order : new ByteOrder[] {ByteOrder.BIG_ENDIAN, ByteOrder.LITTLE_ENDIAN}) {
            byte[] ewkb = new WKBWriter(2, order == ByteOrder.LITTLE_ENDIAN ? ByteOrderValues.LITTLE_ENDIAN : ByteOrderValues.BIG_ENDIAN).write(jts);
            NavigableHakunaGeometry e = new HakunaGeometryEWKB(ewkb).toNavigable();
            assertSame(nav, e, wkt + " order=" + order);
        }
    }

    private void assertSame(NavigableHakunaGeometry a, NavigableHakunaGeometry b, String msg) {
        assertEquals(msg + " parts", a.getNumGeometries(), b.getNumGeometries());
        for (int p = 0; p < a.getNumGeometries(); p++) {
            NavigableHakunaGeometry ap = a.getGeometryN(p);
            NavigableHakunaGeometry bp = b.getGeometryN(p);
            assertEquals(msg + " rings p" + p, ap.getNumRings(), bp.getNumRings());
            for (int r = 0; r < ap.getNumRings(); r++) {
                int n = ap.getRingSize(r);
                assertEquals(msg + " ringsize p" + p + " r" + r, n, bp.getRingSize(r));
                double[] xa = new double[2 * n];
                double[] xb = new double[2 * n];
                ap.copyRingXY(r, xa, 0, 0, n);
                bp.copyRingXY(r, xb, 0, 0, n);
                assertArrayEquals(msg + " xy p" + p + " r" + r, xa, xb, 0.0);
            }
        }
    }

    @Test public void point() throws Exception {
        crossCheck("POINT(30 10)");
    }

    @Test public void lineString() throws Exception {
        crossCheck("LINESTRING(30 10, 10 30, 40 40)");
    }

    @Test public void polygonNoHoles() throws Exception {
        crossCheck("POLYGON((35 10, 45 45, 15 40, 10 20, 35 10))");
    }

    @Test public void polygonWithHole() throws Exception {
        crossCheck("POLYGON((35 10, 45 45, 15 40, 10 20, 35 10),(20 30, 35 35, 30 20, 20 30))");
    }

    @Test public void multiPoint() throws Exception {
        crossCheck("MULTIPOINT((10 40),(40 30),(20 20),(30 10))");
    }

    @Test public void multiLineString() throws Exception {
        crossCheck("MULTILINESTRING((10 10, 20 20, 10 40),(40 40, 30 30, 40 20, 30 10))");
    }

    @Test public void multiPolygon() throws Exception {
        crossCheck("MULTIPOLYGON(((30 20, 45 40, 10 40, 30 20)),"
                + "((15 5, 40 10, 10 20, 5 10, 15 5)))");
    }

    @Test public void multiPolygonWithHoles() throws Exception {
        crossCheck("MULTIPOLYGON(((40 40, 20 45, 45 30, 40 40)),"
                + "((20 35, 10 30, 10 10, 30 5, 45 20, 20 35),(30 20, 20 15, 20 25, 30 20)))");
    }

    @Test public void mixedEndiannessDoesNotDisturbTheGeometry() throws Exception {
        byte[] ewkb = bigEndianMultiPolygonWithLittleEndianPart();

        HakunaGeometryEWKB g = new HakunaGeometryEWKB(ewkb);
        ByteOrder before = g.bb.order();

        NavigableHakunaGeometry part = g.toNavigable().getGeometryN(0);
        int n = part.getRingSize(0);
        double[] xy = new double[2 * n];
        part.copyRingXY(0, xy, 0, 0, n);

        assertEquals(0.0, xy[0], 0.0);
        assertEquals(0.0, xy[1], 0.0);
        assertEquals(10.0, xy[2], 0.0);
        assertEquals(0.0, xy[3], 0.0);

        assertEquals(before, g.bb.order());

        CountingGeometryWriter w = new CountingGeometryWriter();
        g.write(w);
        assertEquals(5, w.coordinates);
    }

    private static byte[] bigEndianMultiPolygonWithLittleEndianPart() {
        java.nio.ByteBuffer bb = java.nio.ByteBuffer.allocate(1 + 4 + 4 + 1 + 4 + 4 + 4 + 5 * 16);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.put((byte) 0);   // big endian
        bb.putInt(6);       // MULTIPOLYGON
        bb.putInt(1);       // one part
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put((byte) 1);   // ...whose part is little endian
        bb.putInt(3);       // POLYGON
        bb.putInt(1);       // one ring
        bb.putInt(5);       // five coordinates
        double[][] ring = {{0, 0}, {10, 0}, {10, 10}, {0, 10}, {0, 0}};
        for (double[] c : ring) {
            bb.putDouble(c[0]);
            bb.putDouble(c[1]);
        }
        return bb.array();
    }

    private static class CountingGeometryWriter implements fi.nls.hakunapi.core.GeometryWriter {
        int coordinates;

        @Override
        public void init(HakunaGeometryType type, int srid, int dimension) {
            // NOP
        }

        @Override
        public void end() {
            // NOP
        }

        @Override
        public void startRing() {
            // NOP
        }

        @Override
        public void endRing() {
            // NOP
        }

        @Override
        public void writeCoordinate(double x, double y) {
            coordinates++;
        }

        @Override
        public void writeCoordinate(double x, double y, double z) {
            coordinates++;
        }

        @Override
        public void writeCoordinate(double x, double y, double z, double m) {
            coordinates++;
        }
    }

    @Test public void partViewReadsCorrectPart() throws Exception {
        Geometry jts = g("MULTIPOLYGON(((0 0,1 0,1 1,0 0)),((10 10,11 10,11 11,10 10)))");
        byte[] ewkb = new WKBWriter(2, ByteOrderValues.LITTLE_ENDIAN).write(jts);
        NavigableHakunaGeometry e = new HakunaGeometryEWKB(ewkb).toNavigable();
        NavigableHakunaGeometry part1 = e.getGeometryN(1);
        double[] xy = new double[2 * part1.getRingSize(0)];
        part1.copyRingXY(0, xy, 0, 0, part1.getRingSize(0));
        assertEquals(10.0, xy[0], 0.0);
        assertEquals(10.0, xy[1], 0.0);
    }
}
