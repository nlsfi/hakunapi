package fi.nls.hakunapi.core.param;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import fi.nls.hakunapi.core.FeatureProducer;
import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.OutputFormat;
import fi.nls.hakunapi.core.PaginationStrategyOffset;
import fi.nls.hakunapi.core.SRIDCode;
import fi.nls.hakunapi.core.SimpleFeatureType;
import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.geom.HakunaGeometryFactory;
import fi.nls.hakunapi.core.projection.NOPProjectionTransformer;
import fi.nls.hakunapi.core.projection.ProjectionTransformer;
import fi.nls.hakunapi.core.projection.ProjectionTransformerFactory;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;
import fi.nls.hakunapi.core.request.GetFeatureCollection;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import fi.nls.hakunapi.core.schemas.Crs;

/**
 * Tests for antimeridian-spanning bbox queries (Issue #79)
 *
 * These tests verify that bbox queries correctly handle the antimeridian
 * (180°/-180° longitude line). When a bbox has minX > maxX, it indicates
 * the bbox spans the antimeridian and should be split into two regions:
 * - Western region: minX to 180°
 * - Eastern region: -180° to maxX
 */
public class BboxAntimeridianTest {

    private SimpleFeatureType collection;
    private FeatureServiceConfig service;

    @Before
    public void init() {
        collection = new SimpleFeatureType() {
            @Override
            public FeatureProducer getFeatureProducer() {
                return null;
            }
        };
        collection.setName("antimeridian_test");
        collection.setProperties(Collections.emptyList());
        collection.setStaticFilters(Collections.emptyList());
        collection.setGeom(new HakunaPropertyGeometry("geom", "table", null, false, null, new int[0], 84, 2, (__, ___, ____) -> {}));
        collection.setPaginationStrategy(PaginationStrategyOffset.INSTANCE);
        collection.setProjectionTransformerFactory(new ProjectionTransformerFactory() {
            @Override
            public ProjectionTransformer getTransformer(int fromSRID, int toSRID) {
                return NOPProjectionTransformer.INSTANCE;
            }
            @Override
            public ProjectionTransformer toCRS84(int fromSRID) {
                return NOPProjectionTransformer.INSTANCE;
            }
            @Override
            public ProjectionTransformer fromCRS84(int toSRID) {
                return NOPProjectionTransformer.INSTANCE;
            }
        });

        service = new FeatureServiceConfig() {
            @Override
            public Collection<OutputFormat> getOutputFormats() {
                return null;
            }

            @Override
            public OutputFormat getOutputFormat(String f) {
                return null;
            }

            @Override
            public Collection<FeatureType> getCollections() {
                return Arrays.asList(collection);
            }

            @Override
            public FeatureType getCollection(String name) {
                return collection.getName().equals(name) ? collection : null;
            }
        };

        service.setKnownSrids(Arrays.asList(SRIDCode.CRS84, SRIDCode.WGS84));
    }

    /**
     * Test Issue #79 bbox: 160.6,-55.95,-170,-25.89
     * This bbox spans the antimeridian and should create a MultiPolygon
     */
    @Test
    public void testIssue79AntimeridianBbox() {
        GetFeatureRequest req = new GetFeatureRequest();
        GetFeatureCollection getItems = new GetFeatureCollection(collection);
        req.addCollection(getItems);

        new BboxParam().modify(service, req, "160.6,-55.95,-170,-25.89");
        new BboxCrsParam().modify(service, req, Crs.CRS84);

        List<Filter> filters = getItems.getFilters();
        assertEquals(1, filters.size());

        Filter filter = filters.get(0);
        Geometry geom = (Geometry) filter.getValue();

        assertEquals(84, geom.getSRID());
        assertEquals("MultiPolygon", geom.getGeometryType());
        assertTrue(geom instanceof MultiPolygon);

        MultiPolygon mp = (MultiPolygon) geom;
        assertEquals(2, mp.getNumGeometries());

        Polygon western = (Polygon) mp.getGeometryN(0);
        Polygon eastern = (Polygon) mp.getGeometryN(1);

        // Western box: 160.6 to 180
        Coordinate[] westernCoords = western.getCoordinates();
        assertEquals(160.6, westernCoords[0].getX(), 0.001);
        assertEquals(180.0, westernCoords[1].getX(), 0.001);

        // Eastern box: -180 to -170
        Coordinate[] easternCoords = eastern.getCoordinates();
        assertEquals(-180.0, easternCoords[0].getX(), 0.001);
        assertEquals(-170.0, easternCoords[1].getX(), 0.001);
    }

    /**
     * Test that points west of antimeridian (160.6° to 180°) intersect western box
     */
    @Test
    public void testPointsInWesternPartOfAntimeridianBbox() {
        GetFeatureRequest req = new GetFeatureRequest();
        GetFeatureCollection getItems = new GetFeatureCollection(collection);
        req.addCollection(getItems);

        new BboxParam().modify(service, req, "160.6,-55.95,-170,-25.89");

        Filter filter = getItems.getFilters().get(0);
        Geometry bboxGeom = (Geometry) filter.getValue();

        // Points that SHOULD be in western part
        Point westPoint1 = HakunaGeometryFactory.GF.createPoint(new Coordinate(165.0, -30.0));
        Point westPoint2 = HakunaGeometryFactory.GF.createPoint(new Coordinate(170.0, -35.0));
        Point westPoint3 = HakunaGeometryFactory.GF.createPoint(new Coordinate(175.0, -40.0));

        assertTrue("Point at 165°, -30° should be in western part", bboxGeom.intersects(westPoint1));
        assertTrue("Point at 170°, -35° should be in western part", bboxGeom.intersects(westPoint2));
        assertTrue("Point at 175°, -40° should be in western part", bboxGeom.intersects(westPoint3));
    }

    /**
     * Test that points east of antimeridian (-180° to -170°) intersect eastern box
     */
    @Test
    public void testPointsInEasternPartOfAntimeridianBbox() {
        GetFeatureRequest req = new GetFeatureRequest();
        GetFeatureCollection getItems = new GetFeatureCollection(collection);
        req.addCollection(getItems);

        new BboxParam().modify(service, req, "160.6,-55.95,-170,-25.89");

        Filter filter = getItems.getFilters().get(0);
        Geometry bboxGeom = (Geometry) filter.getValue();

        // Points that SHOULD be in eastern part
        Point eastPoint1 = HakunaGeometryFactory.GF.createPoint(new Coordinate(-175.0, -30.0));
        Point eastPoint2 = HakunaGeometryFactory.GF.createPoint(new Coordinate(-172.0, -35.0));
        Point eastPoint3 = HakunaGeometryFactory.GF.createPoint(new Coordinate(-170.5, -40.0));

        assertTrue("Point at -175°, -30° should be in eastern part", bboxGeom.intersects(eastPoint1));
        assertTrue("Point at -172°, -35° should be in eastern part", bboxGeom.intersects(eastPoint2));
        assertTrue("Point at -170.5°, -40° should be in eastern part", bboxGeom.intersects(eastPoint3));
    }

    /**
     * Test that points outside the bbox do NOT intersect
     */
    @Test
    public void testPointsOutsideAntimeridianBbox() {
        GetFeatureRequest req = new GetFeatureRequest();
        GetFeatureCollection getItems = new GetFeatureCollection(collection);
        req.addCollection(getItems);

        new BboxParam().modify(service, req, "160.6,-55.95,-170,-25.89");

        Filter filter = getItems.getFilters().get(0);
        Geometry bboxGeom = (Geometry) filter.getValue();

        // Points that should NOT be in bbox
        Point outsideWest = HakunaGeometryFactory.GF.createPoint(new Coordinate(150.0, -30.0));  // Too far west
        Point outsideEast = HakunaGeometryFactory.GF.createPoint(new Coordinate(-160.0, -30.0)); // Too far east
        Point outsideNorth = HakunaGeometryFactory.GF.createPoint(new Coordinate(170.0, -20.0)); // Too far north
        Point outsideSouth = HakunaGeometryFactory.GF.createPoint(new Coordinate(170.0, -60.0)); // Too far south

        assertTrue("Point at 150° should be OUTSIDE (too far west)", !bboxGeom.intersects(outsideWest));
        assertTrue("Point at -160° should be OUTSIDE (too far east)", !bboxGeom.intersects(outsideEast));
        assertTrue("Point at -20° latitude should be OUTSIDE (too far north)", !bboxGeom.intersects(outsideNorth));
        assertTrue("Point at -60° latitude should be OUTSIDE (too far south)", !bboxGeom.intersects(outsideSouth));
    }

    /**
     * Test a normal (non-antimeridian) bbox in the same region
     * This should create a single Polygon, not a MultiPolygon
     */
    @Test
    public void testNormalBboxInSameRegion() {
        GetFeatureRequest req = new GetFeatureRequest();
        GetFeatureCollection getItems = new GetFeatureCollection(collection);
        req.addCollection(getItems);

        // Normal bbox: minX < maxX (165 to 175, does not cross antimeridian)
        new BboxParam().modify(service, req, "165,-50,175,-30");

        Filter filter = getItems.getFilters().get(0);
        Geometry geom = (Geometry) filter.getValue();

        assertEquals(84, geom.getSRID());
        assertEquals("Polygon", geom.getGeometryType());

        // Should intersect western points
        Point westPoint = HakunaGeometryFactory.GF.createPoint(new Coordinate(170.0, -35.0));
        assertTrue("Normal bbox should intersect point at 170°", geom.intersects(westPoint));
    }

    /**
     * Test bbox that spans exactly from -180 to 180 (full longitude range)
     */
    @Test
    public void testFullLongitudeRangeBbox() {
        GetFeatureRequest req = new GetFeatureRequest();
        GetFeatureCollection getItems = new GetFeatureCollection(collection);
        req.addCollection(getItems);

        new BboxParam().modify(service, req, "-180,-90,180,90");

        Filter filter = getItems.getFilters().get(0);
        Geometry geom = (Geometry) filter.getValue();

        assertEquals(84, geom.getSRID());

        // Should be a single polygon covering the whole world
        Point anyPoint = HakunaGeometryFactory.GF.createPoint(new Coordinate(0, 0));
        assertTrue("Full range bbox should intersect any point", geom.intersects(anyPoint));
    }

    /**
     * Test that antimeridian bbox works with EPSG:4326 (lat/lon order)
     * After axis swapping, BboxParam should still detect antimeridian crossing
     */
    @Test
    public void testAntimeridianBboxWithEPSG4326() {
        GetFeatureRequest req = new GetFeatureRequest();
        GetFeatureCollection getItems = new GetFeatureCollection(collection);
        req.addCollection(getItems);

        String bboxCrs = "http://www.opengis.net/def/crs/EPSG/0/4326";
        new BboxCrsParam().modify(service, req, bboxCrs);
        // EPSG:4326 uses lat/lon order in the input
        new BboxParam().modify(service, req, "-55.95,160.6,-25.89,-170");

        Filter filter = getItems.getFilters().get(0);
        Geometry geom = (Geometry) filter.getValue();

        // Should have SRID 4326 and create a geometry (possibly MultiPolygon after reprojection)
        assertEquals(4326, geom.getSRID());
        assertEquals("MultiPolygon", geom.getGeometryType());
        assertEquals(2, geom.getNumGeometries());
    }

    /**
     * Test bbox close to but not crossing antimeridian (179° to 180°)
     */
    @Test
    public void testBboxNearAntimeridianNotCrossing() {
        GetFeatureRequest req = new GetFeatureRequest();
        GetFeatureCollection getItems = new GetFeatureCollection(collection);
        req.addCollection(getItems);

        new BboxParam().modify(service, req, "179,-10,180,10");

        Filter filter = getItems.getFilters().get(0);
        Geometry geom = (Geometry) filter.getValue();

        // Should be a single polygon since it doesn't cross
        assertEquals("Polygon", geom.getGeometryType());
    }

    /**
     * Test very narrow bbox crossing antimeridian (179.5° to -179.5°)
     */
    @Test
    public void testNarrowAntimeridianCrossingBbox() {
        GetFeatureRequest req = new GetFeatureRequest();
        GetFeatureCollection getItems = new GetFeatureCollection(collection);
        req.addCollection(getItems);

        new BboxParam().modify(service, req, "179.5,-10,-179.5,10");

        Filter filter = getItems.getFilters().get(0);
        Geometry geom = (Geometry) filter.getValue();

        assertEquals("MultiPolygon", geom.getGeometryType());

        // Should intersect points very close to antimeridian on both sides
        Point westOfLine = HakunaGeometryFactory.GF.createPoint(new Coordinate(179.8, 0));
        Point eastOfLine = HakunaGeometryFactory.GF.createPoint(new Coordinate(-179.8, 0));

        assertTrue("Should intersect point just west of antimeridian", geom.intersects(westOfLine));
        assertTrue("Should intersect point just east of antimeridian", geom.intersects(eastOfLine));
    }
}
