package fi.nls.hakunapi.tiles.source.vectortile.geom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

import fi.nls.hakunapi.core.geom.HakunaGeometryJTS;
import fi.nls.hakunapi.tiles.source.vectortile.geom.MvtCommands.Ring;

public class MvtPolygonGeometryWriterTest {
    private static final int EXTENT = 4096;
    private static final double TILE_SPAN = 1000.0;
    private static final double RES = TILE_SPAN / EXTENT;

    private static final GeometryFactory GF = new GeometryFactory();

    private MvtPolygonGeometryWriter newWriter() {
        MvtPolygonGeometryWriter w = new MvtPolygonGeometryWriter();
        w.initTile(null, 0.0, TILE_SPAN, RES, EXTENT, EXTENT, EXTENT, 0);
        return w;
    }

    private void polygon(MvtPolygonGeometryWriter w, double[]... rings) throws Exception {
        w.writeNavigable(new HakunaGeometryJTS(poly(rings)).toNavigable());
    }

    private void multiPolygon(MvtPolygonGeometryWriter w, double[][]... parts) throws Exception {
        Polygon[] ps = new Polygon[parts.length];
        for (int i = 0; i < parts.length; i++) {
            ps[i] = poly(parts[i]);
        }
        w.writeNavigable(new HakunaGeometryJTS(GF.createMultiPolygon(ps)).toNavigable());
    }

    private static Polygon poly(double[][] rings) {
        LinearRing shell = linearRing(rings[0]);
        LinearRing[] holes = new LinearRing[rings.length - 1];
        for (int i = 0; i < holes.length; i++) {
            holes[i] = linearRing(rings[i + 1]);
        }
        return GF.createPolygon(shell, holes);
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

    private List<Ring> rings(MvtPolygonGeometryWriter w) {
        return MvtCommands.decode(w.encoder().commands(), w.encoder().length());
    }

    @Test
    public void squareCrossingEdgeClippedToExtent() throws Exception {
        MvtPolygonGeometryWriter w = newWriter();
        w.resetFeature();
        polygon(w, new double[] {500, 500, 1500, 500, 1500, 1500, 500, 1500});

        List<Ring> rs = rings(w);
        assertEquals(1, rs.size());
        for (int[] p : rs.get(0).pts) {
            assertTrue(p[0] >= 0 && p[0] <= EXTENT);
            assertTrue(p[1] >= 0 && p[1] <= EXTENT);
        }
    }

    @Test
    public void polygonWithHoleEmitsOppositeWinding() throws Exception {
        MvtPolygonGeometryWriter w = newWriter();
        w.resetFeature();
        polygon(w, new double[] {100, 100, 900, 100, 900, 900, 100, 900},
                   new double[] {300, 300, 700, 300, 700, 700, 300, 700});

        List<Ring> rs = rings(w);
        assertEquals(2, rs.size());
        double aExt = rs.get(0).signedArea();
        double aHole = rs.get(1).signedArea();
        assertTrue("exterior nonzero", aExt != 0);
        assertTrue("hole nonzero", aHole != 0);
        assertTrue("hole opposite winding to exterior", (aExt > 0) != (aHole > 0));
    }

}
