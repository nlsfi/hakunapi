package fi.nls.hakunapi.mvt.algorithm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;

import fi.nls.hakunapi.core.geom.HakunaGeometryFactory;
import fi.nls.hakunapi.mvt.algorithm.DouglasPeucker;

public class DouglasPeuckerTest {

    @Test
    public void testThatStraightLineGetsSimplifiedWithZeroTolerance() {
        GeometryFactory gf = HakunaGeometryFactory.GF;
        Coordinate[] arr = new Coordinate[] { new Coordinate(0, 0), new Coordinate(10, 10), new Coordinate(20, 20),
                new Coordinate(30, 30), new Coordinate(40, 40), new Coordinate(50, 50) };
        Coordinate[] arr_expected = new Coordinate[] { new Coordinate(0, 0), new Coordinate(50, 50) };
        LineString ls = gf.createLineString(arr);
        LineString actual = (LineString) new DouglasPeucker(0).transform(ls);
        LineString expected = (LineString) gf.createLineString(arr_expected);
        assertEquals(expected, actual);
    }

    @Test
    public void testThatMatchesTheRegularOne() {
        GeometryFactory gf = HakunaGeometryFactory.GF;
        LineString ls = createRandomLineString(500, gf);
        double tolerance = 1.5;
        LineString actual = (LineString) new DouglasPeucker(tolerance).transform(ls);
        DouglasPeuckerSimplifier simplifier = new DouglasPeuckerSimplifier(ls);
        simplifier.setDistanceTolerance(tolerance);
        LineString expected = (LineString) simplifier.getResultGeometry();
        assertEquals(expected, actual);
    }

    @Test
    public void testPolygons() {
        GeometryFactory gf = HakunaGeometryFactory.GF;
        Polygon poly = createRandomPolygon(20, gf);
        double tolerance = 1.5;
        Geometry actual = new DouglasPeucker(tolerance).transform(poly);

        DouglasPeuckerSimplifier simplifier = new DouglasPeuckerSimplifier(poly);
        simplifier.setDistanceTolerance(tolerance);
        Geometry expected = simplifier.getResultGeometry();

        assertTrue(expected.getEnvelope().equals(actual.getEnvelope()));
        assertTrue(expected.equals(actual));
    }

    public static void main(String[] args) {
        for (int i = 0; i < 1000; i++) {

            new DouglasPeuckerTest().testPolygons();
        }
    }

    private static LineString createRandomLineString(int n, GeometryFactory gf) {
        double x = (Math.random() * 360.0) - 180.0;
        double y = (Math.random() * 180.0) - 90.0;
        List<Coordinate> points = new ArrayList<>();
        points.add(new Coordinate(x, y));
        for (int i = 1; i < n; i++) {
            double dx = (Math.random() * 10.0) - 5.0;
            double dy = (Math.random() * 10.0) - 5.0;
            x += dx;
            y += dy;
            points.add(new Coordinate(x, y));
        }
        return gf.createLineString(points.toArray(new Coordinate[0]));
    }

    private static Polygon createRandomPolygon(int n, GeometryFactory gf) {
        double _x = (Math.random() * 360.0) - 180.0;
        double _y = (Math.random() * 180.0) - 90.0;
        Polygon poly = null;
        boolean valid = false;
        while (!valid) {
            List<Coordinate> points = new ArrayList<>();
            points.add(new Coordinate(_x, _y));
            double x = _x;
            double y = _y;
            for (int i = 1; i < n; i++) {
                double dx = (Math.random() * 10.0) - 5.0;
                double dy = (Math.random() * 10.0) - 5.0;
                x += dx;
                y += dy;
                points.add(new Coordinate(x, y));
            }
            points.add(new Coordinate(_x, _y));
            poly = gf.createPolygon((Coordinate[]) points.toArray(new Coordinate[] {}));
            valid = poly.isValid();
        }
        return poly;
    }

}
