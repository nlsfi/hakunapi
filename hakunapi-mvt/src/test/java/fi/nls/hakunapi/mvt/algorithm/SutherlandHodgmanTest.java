package fi.nls.hakunapi.mvt.algorithm;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import fi.nls.hakunapi.core.geom.HakunaGeometryFactory;

public class SutherlandHodgmanTest {

    @Test
    public void testClipPolygon() throws ParseException {
        Envelope bbox = new Envelope(0, 30, 0, 30);
        GeometryFactory gf = HakunaGeometryFactory.GF;
        WKTReader wkt = new WKTReader(gf);
        wkt.setIsOldJtsCoordinateSyntaxAllowed(false);
        SutherlandHodgman clipper = new SutherlandHodgman(bbox);

        LinearRing input = (LinearRing) wkt.read("LINEARRING (-10 10, 0 10, 10 10, 10 5, 10 -5, 10 -10, 20 -10, 20 10, 40 10, 40 20, 20 20, 20 40, 10 40, 10 20, 5 20, -10 20, -10 10)");
        LinearRing actual = clipper.clipPolygon(input, gf);
        LinearRing expected = (LinearRing) wkt.read("LINEARRING (0 10, 10 10, 10 5, 10 0, 20 0, 20 10, 30 10, 30 20, 20 20, 20 30, 10 30, 10 20, 5 20, 0 20, 0 10)");

        actual.normalize();
        expected.normalize();

        assertEquals(expected, actual);
    }
    
    @Test
    public void testClipPolygon3() throws ParseException {
        Envelope bbox = new Envelope(0, 20, 0, 20);
        GeometryFactory gf = HakunaGeometryFactory.GF;
        WKTReader wkt = new WKTReader(gf);
        wkt.setIsOldJtsCoordinateSyntaxAllowed(false);
        SutherlandHodgman clipper = new SutherlandHodgman(bbox);

        LinearRing input = (LinearRing) wkt.read("LINEARRING (10 10, 30 -10, 30 15, 15 15, 10 10)");
        LinearRing actual = clipper.clipPolygon(input, gf);
        LinearRing expected = (LinearRing) wkt.read("LINEARRING (10 10, 20 0, 20 15, 15 15, 10 10)");

        actual.normalize();
        expected.normalize();

        assertEquals(expected, actual);
    }

    @Test
    public void testClipPolygon2() throws ParseException {
        Envelope bbox = new Envelope(0, 30, 0, 30);
        GeometryFactory gf = HakunaGeometryFactory.GF;
        WKTReader wkt = new WKTReader(gf);
        wkt.setIsOldJtsCoordinateSyntaxAllowed(false);
        SutherlandHodgman clipper = new SutherlandHodgman(bbox);

        LinearRing input = (LinearRing) wkt.read("LINEARRING (0 0, 40 10, 40 40, 10 40, 0 0)");
        LinearRing actual = clipper.clipPolygon(input, gf);
        LinearRing expected = (LinearRing) wkt.read("LINEARRING (0 0, 30 7.5, 30 30, 7.5 30, 0 0)");

        actual.normalize();
        expected.normalize();

        assertEquals(expected, actual);
    }

    @Test
    public void testClipDisjointPolygon() throws ParseException {
        Envelope bbox = new Envelope(0, 20, 0, 20);
        GeometryFactory gf = HakunaGeometryFactory.GF;
        WKTReader wkt = new WKTReader(gf);
        wkt.setIsOldJtsCoordinateSyntaxAllowed(false);
        SutherlandHodgman clipper = new SutherlandHodgman(bbox);

        LinearRing input = (LinearRing) wkt.read("LINEARRING (30 30, 50 30, 50 50, 30 50, 30 30)");
        LinearRing actual = clipper.clipPolygon(input, gf);
        assertEquals(null, actual);
    }

    @Test
    public void testPolygonOnlyTouchingOnePoint() throws ParseException {
        Envelope bbox = new Envelope(0, 30, 0, 30);
        GeometryFactory gf = HakunaGeometryFactory.GF;
        WKTReader wkt = new WKTReader(gf);
        wkt.setIsOldJtsCoordinateSyntaxAllowed(false);
        SutherlandHodgman clipper = new SutherlandHodgman(bbox);

        LinearRing input = (LinearRing) wkt.read("LINEARRING (30 30, 50 30, 50 50, 30 50, 30 30)");
        LinearRing actual = clipper.clipPolygon(input, gf);
        assertEquals(null, actual);
    }

    @Test
    public void testPolygonOnlyTouchingSegment() throws ParseException {
        Envelope bbox = new Envelope(0, 30, 0, 30);
        GeometryFactory gf = HakunaGeometryFactory.GF;
        WKTReader wkt = new WKTReader(gf);
        wkt.setIsOldJtsCoordinateSyntaxAllowed(false);
        SutherlandHodgman clipper = new SutherlandHodgman(bbox);

        LinearRing input = (LinearRing) wkt.read("LINEARRING (20 30, 40 40, 50 30, 20 30)");
        LinearRing actual = clipper.clipPolygon(input, gf);
        assertEquals(null, actual);
    }

}
