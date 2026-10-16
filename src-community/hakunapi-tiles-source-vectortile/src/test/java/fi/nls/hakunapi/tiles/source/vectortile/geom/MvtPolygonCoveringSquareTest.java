package fi.nls.hakunapi.tiles.source.vectortile.geom;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Random;

import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

import fi.nls.hakunapi.core.geom.HakunaGeometryJTS;

public class MvtPolygonCoveringSquareTest {
    private static final int EXTENT = 4096;
    private static final int BUFFER = 256;
    private static final double TILE_SPAN = 4096.0;
    private static final GeometryFactory GF = new GeometryFactory();
    private static final double RES = TILE_SPAN / EXTENT;

    @Test
    public void testCoveringRingEqualsClippedRing() throws Exception {
        assertSame(ring(4, 40000, false));
        assertSame(ring(4, 40000, true));
        assertSame(ring(3, 40000, false));
        assertSame(ring(3, 40000, true));
        assertSame(ring(8, 20000, false));
        assertSame(ring(8, 20000, true));
    }

    @Test
    public void testRandomRingsAgree() throws Exception {
        Random rnd = new Random(20260916);
        int covering = 0;
        for (int i = 0; i < 5000; i++) {
            int n = 3 + rnd.nextInt(8);
            double[] xy = randomRing(rnd, n, 400 + rnd.nextDouble() * 50000);
            assertSame(xy);
            if (emit(xy, true).length != 0 && contains(xy, -BUFFER, -BUFFER)) {
                covering++;
            }
        }
        assertTrue("no covering rings generated", covering > 100);
    }

    private void assertSame(double[]... rings) throws Exception {
        double[][][] poly = new double[1][][];
        poly[0] = rings;
        assertArrayEquals("fast path differs from the clip",
                emitMulti(poly, false), emitMulti(poly, true));
    }

    private int[] emit(double[] ring, boolean fastPath) throws Exception {
        return emitMulti(new double[][][] { { ring } }, fastPath);
    }

    private int[] emitMulti(double[][][] poly, boolean fastPath) throws Exception {
        MvtPolygonGeometryWriter w = new MvtPolygonGeometryWriter();
        w.coveringFastPath = fastPath;
        w.initTile(null, 0.0, TILE_SPAN, RES, EXTENT, EXTENT, EXTENT, BUFFER);
        w.resetFeature();
        w.writeNavigable(new HakunaGeometryJTS(multiPolygon(poly)).toNavigable());
        return Arrays.copyOf(w.encoder().commands(), w.encoder().length());
    }

    private static org.locationtech.jts.geom.Geometry multiPolygon(double[][][] poly) {
        Polygon[] parts = new Polygon[poly.length];
        for (int p = 0; p < poly.length; p++) {
            double[][] part = poly[p];
            LinearRing shell = linearRing(part[0]);
            LinearRing[] holes = new LinearRing[part.length - 1];
            for (int h = 0; h < holes.length; h++) {
                holes[h] = linearRing(part[h + 1]);
            }
            parts[p] = GF.createPolygon(shell, holes);
        }
        return GF.createMultiPolygon(parts);
    }

    private static LinearRing linearRing(double[] xy) {
        int n = xy.length / 2;
        Coordinate[] cs = new Coordinate[n + 1];
        for (int i = 0; i < n; i++) {
            cs[i] = new Coordinate(xy[i * 2], xy[i * 2 + 1]);
        }
        cs[n] = cs[0];
        return GF.createLinearRing(cs);
    }

    private static double[] ring(int n, double radius, boolean reverse) {
        double[] xy = new double[n * 2];
        for (int i = 0; i < n; i++) {
            int k = reverse ? n - 1 - i : i;
            double ang = k * 2 * Math.PI / n + 0.3;
            xy[i * 2] = 2048 + radius * Math.cos(ang);
            xy[i * 2 + 1] = 2048 + radius * Math.sin(ang);
        }
        return xy;
    }

    private static double[] randomRing(Random rnd, int n, double radius) {
        double[] ang = new double[n];
        for (int i = 0; i < n; i++) {
            ang[i] = rnd.nextDouble() * Math.PI * 2;
        }
        Arrays.sort(ang);
        if (rnd.nextBoolean()) {
            for (int i = 0; i < n / 2; i++) {
                double t = ang[i];
                ang[i] = ang[n - 1 - i];
                ang[n - 1 - i] = t;
            }
        }
        double[] xy = new double[n * 2];
        for (int i = 0; i < n; i++) {
            double rad = radius * (0.5 + rnd.nextDouble());
            xy[i * 2] = 2048 + rad * Math.cos(ang[i]);
            xy[i * 2 + 1] = 2048 + rad * Math.sin(ang[i]);
        }
        return xy;
    }

    private static double[] offsetRing(double[] xy, double dx, double dy) {
        double[] out = new double[xy.length];
        for (int i = 0; i < xy.length / 2; i++) {
            out[i * 2] = xy[i * 2] + dx;
            out[i * 2 + 1] = xy[i * 2 + 1] + dy;
        }
        return out;
    }

    private static boolean contains(double[] xy, double px, double py) {
        int n = xy.length / 2;
        boolean in = false;
        for (int i = 0, j = n - 1; i < n; j = i++) {
            double xi = xy[i * 2], yi = xy[i * 2 + 1];
            double xj = xy[j * 2], yj = xy[j * 2 + 1];
            if (((yi > py) != (yj > py)) && (px < (xj - xi) * (py - yi) / (yj - yi) + xi)) {
                in = !in;
            }
        }
        return in;
    }
}
