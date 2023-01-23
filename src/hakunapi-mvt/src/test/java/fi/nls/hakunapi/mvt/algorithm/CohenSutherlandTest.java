package fi.nls.hakunapi.mvt.algorithm;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import fi.nls.hakunapi.core.geom.HakunaGeometryFactory;

public class CohenSutherlandTest {

    @Test
    public void testClipLine() throws ParseException {
        Envelope bbox = new Envelope(0, 30, 0, 30);
        GeometryFactory gf = HakunaGeometryFactory.GF;
        WKTReader wkt = new WKTReader(gf);
        wkt.setIsOldJtsCoordinateSyntaxAllowed(false);
        CohenSutherland clipper = new CohenSutherland(bbox);

        LineString ls = (LineString) wkt.read("LINESTRING (-10 10, 10 10, 10 -10, 20 -10, 20 10, 40 10, 40 20, 20 20, 20 40, 10 40, 10 20, 5 20, -10 20)");
        List<LineString> clipped = clipper.clipLine(ls, gf);

        LineString e1 = (LineString) wkt.read("LINESTRING ( 0 10, 10 10, 10  0)");
        LineString e2 = (LineString) wkt.read("LINESTRING (20  0, 20 10, 30 10)");
        LineString e3 = (LineString) wkt.read("LINESTRING (30 20, 20 20, 20 30)");
        LineString e4 = (LineString) wkt.read("LINESTRING (10 30, 10 20,  5 20, 0 20)");
        assertEquals(4, clipped.size());
        assertEquals(e1, clipped.get(0));
        assertEquals(e2, clipped.get(1));
        assertEquals(e3, clipped.get(2));
        assertEquals(e4, clipped.get(3));
    }

    @Test
    public void testClipLineSimple() throws ParseException {
        Envelope bbox = new Envelope(0, 20, 0, 20);
        GeometryFactory gf = HakunaGeometryFactory.GF;
        WKTReader wkt = new WKTReader(gf);
        wkt.setIsOldJtsCoordinateSyntaxAllowed(false);
        CohenSutherland clipper = new CohenSutherland(bbox);

        LineString ls = (LineString) wkt.read("LINESTRING (-10 10, 10 10, 10 -10)");
        List<LineString> clipped = clipper.clipLine(ls, gf);

        LineString e1 = (LineString) wkt.read("LINESTRING ( 0 10, 10 10, 10  0)");
        assertEquals(e1, clipped.get(0));
    }

    @Test
    public void testClipLineManyCrossings() throws ParseException {
        Envelope bbox = new Envelope(0, 20, 0, 20);
        GeometryFactory gf = HakunaGeometryFactory.GF;
        WKTReader wkt = new WKTReader(gf);
        wkt.setIsOldJtsCoordinateSyntaxAllowed(false);
        CohenSutherland clipper = new CohenSutherland(bbox);

        LineString ls = (LineString) wkt.read("LINESTRING (10 -10, 10 30, 20 30, 20 -10)");
        List<LineString> clipped = clipper.clipLine(ls, gf);
        LineString e1 = (LineString) wkt.read("LINESTRING (10  0, 10 20)");
        LineString e2 = (LineString) wkt.read("LINESTRING (20 20, 20  0)");
        assertEquals(2, clipped.size());
        assertEquals(e1, clipped.get(0));
        assertEquals(e2, clipped.get(1));
    }

    @Test
    public void testClipDisjointLine() throws ParseException {
        Envelope bbox = new Envelope(0, 30, 0, 30);
        GeometryFactory gf = HakunaGeometryFactory.GF;
        WKTReader wkt = new WKTReader(gf);
        wkt.setIsOldJtsCoordinateSyntaxAllowed(false);
        CohenSutherland clipper = new CohenSutherland(bbox);

        LineString input = (LineString) wkt.read("LINESTRING (40 40, 50 50)");
        List<LineString> actual = clipper.clipLine(input, gf);
        assertEquals(0, actual.size());
    }

    @Test
    public void testLineOnlyTouchingOnePoint() throws ParseException {
        Envelope bbox = new Envelope(0, 30, 0, 30);
        GeometryFactory gf = HakunaGeometryFactory.GF;
        WKTReader wkt = new WKTReader(gf);
        wkt.setIsOldJtsCoordinateSyntaxAllowed(false);
        CohenSutherland clipper = new CohenSutherland(bbox);

        LineString input = (LineString) wkt.read("LINESTRING (30 30, 50 30)");
        List<LineString> actual = clipper.clipLine(input, gf);
        assertEquals(0, actual.size());
    }

}
