package fi.nls.hakunapi.core.projection;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ByteOrderValues;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.WKTReader;

import fi.nls.hakunapi.core.geom.HakunaGeometryEWKB;
import fi.nls.hakunapi.core.geom.HakunaGeometryJTS;
import fi.nls.hakunapi.core.geom.NavigableHakunaGeometry;
import fi.nls.hakunapi.core.geom.WKBBacked;

public class EWKBTransformerTest {

    private static final WKTReader WKT = new WKTReader();

    private static final ProjectionTransformer SHIFT = new StubTransformer(3067, 3857) {
        @Override
        public void transformInPlace(double[] coords, int off, int n) {
            for (int i = 0; i < n; i++) {
                coords[2 * i] = coords[2 * i] * 2 + 1000;
                coords[2 * i + 1] = coords[2 * i + 1] * 3 - 500;
            }
        }
    };

    private static Geometry g(String wkt) throws ParseException {
        return WKT.read(wkt);
    }

    private void crossCheck(String wkt) throws Exception {
        for (ByteOrder order : new ByteOrder[] { ByteOrder.BIG_ENDIAN, ByteOrder.LITTLE_ENDIAN }) {
            String msg = wkt + " order=" + order;
            int byteOrderValue = order == ByteOrder.LITTLE_ENDIAN
                    ? ByteOrderValues.LITTLE_ENDIAN : ByteOrderValues.BIG_ENDIAN;

            Geometry jts = g(wkt);
            jts.apply(new JTSTransformer(SHIFT));
            NavigableHakunaGeometry expected = new HakunaGeometryJTS(jts).toNavigable();

            byte[] wkb = new WKBWriter(2, byteOrderValue).write(g(wkt));
            int lengthBefore = wkb.length;
            HakunaGeometryEWKB geom = new HakunaGeometryEWKB(wkb);
            int typeBefore = geom.getGeometryType();

            new EWKBTransformer(SHIFT).transform(geom);

            assertEquals(msg + " byte length", lengthBefore, wkb.length);
            assertEquals(msg + " type", typeBefore, geom.getGeometryType());
            assertStructureAndCoordinates(expected, geom.toNavigable(), msg);
        }
    }

    private void assertStructureAndCoordinates(NavigableHakunaGeometry a,
            NavigableHakunaGeometry b, String msg) {
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

    @Test public void ringLongerThanScratch() throws Exception {
        StringBuilder sb = new StringBuilder("LINESTRING(");
        int n = 200;
        for (int i = 0; i < n; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(i).append(' ').append(2 * i);
        }
        crossCheck(sb.append(')').toString());
    }

    @Test public void mixedEndiannessIsReprojectedCorrectly() throws Exception {
        byte[] wkb = bigEndianMultiPolygonWithLittleEndianPart();
        HakunaGeometryEWKB geom = new HakunaGeometryEWKB(wkb);
        ByteOrder orderBefore = geom.bb.order();
        int positionBefore = geom.bb.position();

        new EWKBTransformer(SHIFT).transform(geom);

        assertEquals("shared buffer byte order", orderBefore, geom.bb.order());
        assertEquals("shared buffer position", positionBefore, geom.bb.position());

        NavigableHakunaGeometry part = geom.toNavigable().getGeometryN(0);
        int n = part.getRingSize(0);
        double[] xy = new double[2 * n];
        part.copyRingXY(0, xy, 0, 0, n);

        assertEquals(1000.0, xy[0], 0.0);
        assertEquals(-500.0, xy[1], 0.0);
        assertEquals(1020.0, xy[2], 0.0);
        assertEquals(-500.0, xy[3], 0.0);
    }

    private static byte[] bigEndianMultiPolygonWithLittleEndianPart() {
        ByteBuffer bb = ByteBuffer.allocate(1 + 4 + 4 + 1 + 4 + 4 + 4 + 5 * 16);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.put((byte) 0);   // big endian
        bb.putInt(6);       // MULTIPOLYGON
        bb.putInt(1);       // one part
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put((byte) 1);   // ...whose part is little endian
        bb.putInt(3);       // POLYGON
        bb.putInt(1);       // one ring
        bb.putInt(5);       // five coordinates
        double[][] ring = { { 0, 0 }, { 10, 0 }, { 10, 10 }, { 0, 10 }, { 0, 0 } };
        for (double[] c : ring) {
            bb.putDouble(c[0]);
            bb.putDouble(c[1]);
        }
        return bb.array();
    }

    @Test public void geoPackageShapedBlobReprojectsLikeEwkb() throws Exception {
        String wkt = "POLYGON((35 10, 45 45, 15 40, 10 20, 35 10),(20 30, 35 35, 30 20, 20 30))";
        byte[] body = new WKBWriter(2, ByteOrderValues.LITTLE_ENDIAN).write(g(wkt));

        HakunaGeometryEWKB bare = new HakunaGeometryEWKB(body.clone());
        new EWKBTransformer(SHIFT).transform(bare);

        for (int envelopeBytes : new int[] { 8, 40 }) {
            String msg = "gpkg header=" + envelopeBytes;
            byte[] blob = gpkgBlob(body, envelopeBytes);
            byte[] headerBefore = new byte[envelopeBytes];
            System.arraycopy(blob, 0, headerBefore, 0, envelopeBytes);

            StubWKBBacked geom = new StubWKBBacked(blob, envelopeBytes);
            new EWKBTransformer(SHIFT).transform(geom);

            byte[] headerAfter = new byte[envelopeBytes];
            System.arraycopy(blob, 0, headerAfter, 0, envelopeBytes);
            ByteBuffer.wrap(headerBefore, 4, 4).order(ByteOrder.LITTLE_ENDIAN).putInt(3857);
            assertArrayEquals(msg + " header differs only by srid", headerBefore, headerAfter);

            assertStructureAndCoordinates(bare.toNavigable(), geom.toNavigable(), msg);
        }
    }

    private static byte[] gpkgBlob(byte[] body, int headerBytes) {
        byte[] blob = new byte[headerBytes + body.length];
        blob[0] = 'G';
        blob[1] = 'P';
        blob[2] = 0; // version 1
        blob[3] = (byte) (1 | (headerBytes == 40 ? 1 << 1 : 0)); // LE, XY envelope or none
        ByteBuffer.wrap(blob, 4, 4).order(ByteOrder.LITTLE_ENDIAN).putInt(3067);
        System.arraycopy(body, 0, blob, headerBytes, body.length);
        return blob;
    }

    @Test public void transformReportsTargetSridInFieldBytesAndWriter() throws Exception {
        for (int order : new int[] { ByteOrderValues.BIG_ENDIAN, ByteOrderValues.LITTLE_ENDIAN }) {
            Geometry jts = g("LINESTRING(30 10, 10 30)");
            jts.setSRID(3067);
            byte[] ewkb = new WKBWriter(2, order, true).write(jts);
            HakunaGeometryEWKB geom = new HakunaGeometryEWKB(ewkb);

            new EWKBTransformer(SHIFT).transform(geom);

            assertEquals(3857, geom.getSrid());
            assertEquals(3857, new HakunaGeometryEWKB(ewkb).getSrid());
            int[] written = new int[1];
            geom.write(new fi.nls.hakunapi.core.GeometryWriter() {
                @Override public void init(fi.nls.hakunapi.core.geom.HakunaGeometryType type, int srid, int dimension) { written[0] = srid; }
                @Override public void end() { }
                @Override public void writeCoordinate(double x, double y) { }
                @Override public void writeCoordinate(double x, double y, double z) { }
                @Override public void writeCoordinate(double x, double y, double z, double m) { }
                @Override public void startRing() { }
                @Override public void endRing() { }
            });
            assertEquals(3857, written[0]);
        }

        HakunaGeometryEWKB noSrid = new HakunaGeometryEWKB(new WKBWriter(2, ByteOrderValues.LITTLE_ENDIAN).write(g("POINT(1 2)")));
        new EWKBTransformer(SHIFT).transform(noSrid);
        assertEquals(3857, noSrid.getSrid());
    }

    @Test public void rejectsNon2dGeometry() throws Exception {
        Geometry jts = g("POINT Z(30 10 5)");
        byte[] wkb = new WKBWriter(3, ByteOrderValues.LITTLE_ENDIAN).write(jts);
        HakunaGeometryEWKB geom = new HakunaGeometryEWKB(wkb);
        assertEquals(3, geom.getDimension());
        try {
            new EWKBTransformer(SHIFT).transform(geom);
            fail("expected IllegalArgumentException for 3D geometry");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    private static class StubWKBBacked implements WKBBacked {

        private final ByteBuffer bb;
        private final int dataStart;
        private final int type;

        StubWKBBacked(byte[] blob, int headerBytes) {
            this.bb = ByteBuffer.wrap(blob);
            this.bb.order(ByteOrder.LITTLE_ENDIAN);
            int typeInt = bb.getInt(headerBytes + 1);
            this.type = typeInt & 0x7;
            this.dataStart = headerBytes + 5;
        }

        @Override public ByteBuffer getBuffer() { return bb; }
        @Override public int getDataStart() { return dataStart; }
        @Override public int getGeometryType() { return type; }
        @Override public int getDimension() { return 2; }
        @Override public void setSrid(int srid) { bb.order(ByteOrder.LITTLE_ENDIAN).putInt(4, srid); }

        @Override public byte[] toEWKB() { throw new UnsupportedOperationException(); }
        @Override public Geometry toJTSGeometry() { throw new UnsupportedOperationException(); }
        @Override public void write(fi.nls.hakunapi.core.GeometryWriter writer) {
            throw new UnsupportedOperationException();
        }
    }

    private abstract static class StubTransformer implements ProjectionTransformer {

        private final int fromSRID;
        private final int toSRID;

        StubTransformer(int fromSRID, int toSRID) {
            this.fromSRID = fromSRID;
            this.toSRID = toSRID;
        }

        @Override public int getDimension() { return 2; }
        @Override public int getFromSRID() { return fromSRID; }
        @Override public int getToSRID() { return toSRID; }
    }

}
