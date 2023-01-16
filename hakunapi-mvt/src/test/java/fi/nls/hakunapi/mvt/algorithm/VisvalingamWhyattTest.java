package fi.nls.hakunapi.mvt.algorithm;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.simplify.VWSimplifier;

import fi.nls.hakunapi.core.geom.HakunaGeometryFactory;
import fi.nls.hakunapi.mvt.algorithm.VisvalingamWhyatt;
import fi.nls.hakunapi.mvt.algorithm.VisvalingamWhyatt.Vertex;

public class VisvalingamWhyattTest {

    @Test
    public void testThatStraightLineGetsSimplifiedWithZeroTolerance() {
        GeometryFactory gf = HakunaGeometryFactory.GF;
        Coordinate[] arr = new Coordinate[] {
                new Coordinate(0, 0),
                new Coordinate(10, 10),
                new Coordinate(20, 20),
                new Coordinate(30, 30),
                new Coordinate(40, 40),
                new Coordinate(50, 50)
        };
        Coordinate[] arr_expected = new Coordinate[] {
                new Coordinate(0, 0),
                new Coordinate(50, 50)
        };
        LineString ls = gf.createLineString(arr);
        LineString actual = (LineString) new VisvalingamWhyatt(0).transform(ls);
        LineString expected = (LineString) gf.createLineString(arr_expected);
        assertEquals(expected, actual);
    }

    @Test
    public void testLineStringAgainstJTSImplementation() {
        GeometryFactory gf = HakunaGeometryFactory.GF;
        LineString ls = createRandomLineString(500, gf);
        double tolerance = 1.5;
        VWSimplifier simplifier = new VWSimplifier(ls);
        simplifier.setDistanceTolerance(tolerance);
        LineString expected = (LineString) simplifier.getResultGeometry();
        LineString actual = (LineString) new VisvalingamWhyatt(tolerance).transform(ls.copy());
        assertEquals(expected, actual);
    }

    @Test
    public void testPolygonAgainstJTSImplementation() {
        GeometryFactory gf = HakunaGeometryFactory.GF;
        Polygon poly = createRandomPolygon(20, gf);
        double tolerance = 1.5;
        VWSimplifier simplifier = new VWSimplifier(poly);
        simplifier.setDistanceTolerance(tolerance);
        Geometry expected = simplifier.getResultGeometry();
        Geometry actual = new VisvalingamWhyatt(tolerance).transform(poly.copy());
        expected.normalize();
        actual.normalize();
        assertEquals(expected, actual);
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

    @Test
    public void heapHappyCase() {
        VisvalingamWhyatt.VertexUpdateHeap heap = new VisvalingamWhyatt.VertexUpdateHeap();

        Vertex[] vertices = new Vertex[6];
        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = new Vertex(i);
        }
        vertices[0].area = 100;
        vertices[1].area = 300;
        vertices[2].area = 50; // => 350
        vertices[3].area = 200;
        vertices[4].area = 10; // Remove first
        vertices[5].area = 500; // => 0

        for (int i = 0; i < 5; i++) {
            heap.add(vertices[i]);
        }

        Vertex v = heap.poll();
        assertEquals(4, v.i);
        assertEquals(0, 10, v.area);

        heap.add(vertices[5]);

        vertices[2].area = 350;
        heap.update(vertices[2]);
        vertices[5].area = 0;
        heap.update(vertices[5]);

        v = heap.poll();
        assertEquals(5, v.i);
        assertEquals(0, 0, v.area);

        v = heap.poll();
        assertEquals(0, v.i);
        assertEquals(0, 100, v.area);

        v = heap.poll();
        assertEquals(3, v.i);
        assertEquals(0, 200, v.area);

        v = heap.poll();
        assertEquals(1, v.i);
        assertEquals(0, 300, v.area);

        v = heap.poll();
        assertEquals(2, v.i);
        assertEquals(0, 350, v.area);

        assertEquals(true, heap.isEmpty());
    }

}
