package fi.nls.hakunapi.core.param;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import fi.nls.hakunapi.core.FeatureProducer;
import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.OutputFormat;
import fi.nls.hakunapi.core.PaginationStrategyOffset;
import fi.nls.hakunapi.core.SRIDCode;
import fi.nls.hakunapi.core.SimpleFeatureType;
import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;
import fi.nls.hakunapi.core.request.GetFeatureCollection;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import fi.nls.hakunapi.core.schemas.Crs;

public class BboxCrsParamTest {

    private SimpleFeatureType myCollection;
    private FeatureServiceConfig service;

    @Before
    public void init() {
        myCollection = new SimpleFeatureType() {

            @Override
            public FeatureProducer getFeatureProducer() {
                return null;
            }
        };
        myCollection.setName("myCollection");
        myCollection.setProperties(Collections.emptyList());
        myCollection.setStaticFilters(Collections.emptyList());
        myCollection.setGeom(new HakunaPropertyGeometry("geometry", "table", null, false, null, new int[0], 84, 2, (__, ___, ____) -> {}));
        myCollection.setPaginationStrategy(PaginationStrategyOffset.INSTANCE);

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
                return Arrays.asList(myCollection);
            }

            @Override
            public FeatureType getCollection(String name) {
                return myCollection.getName().equals(name) ? myCollection : null;
            }
        };

        service.setKnownSrids(Arrays.asList(SRIDCode.CRS84, SRIDCode.WGS84));
    }

    @Test
    public void testLonLatRS84() {
        GetFeatureRequest req = new GetFeatureRequest();
        GetFeatureCollection getItems = new GetFeatureCollection(myCollection);
        req.addCollection(getItems);

        new BboxParam().modify(service, req, "-180,-90,180,90");
        new BboxCrsParam().modify(service, req, Crs.CRS84);

        List<Filter> filters = getItems.getFilters();
        assertEquals(1, filters.size());
        Filter filter = filters.get(0);
        Geometry geom = (Geometry) filter.getValue();
        assertEquals(84, geom.getSRID());
        Coordinate bottomLeft = geom.getCoordinates()[0];
        assertEquals(-180, bottomLeft.getX(), 0.0);
        assertEquals(-90, bottomLeft.getY(), 0.0);
    }

    @Test
    public void testLatLonCRS4326() {
        GetFeatureRequest req = new GetFeatureRequest();
        GetFeatureCollection getItems = new GetFeatureCollection(myCollection);
        req.addCollection(getItems);

        new BboxParam().modify(service, req, "-90,-180,90,180");
        String bboxCrs = "http://www.opengis.net/def/crs/EPSG/0/4326";
        new BboxCrsParam().modify(service, req, bboxCrs);

        List<Filter> filters = getItems.getFilters();
        assertEquals(1, filters.size());
        Filter filter = filters.get(0);
        Geometry geom = (Geometry) filter.getValue();
        assertEquals(4326, geom.getSRID());
        Coordinate bottomLeft = geom.getCoordinates()[0];
        assertEquals(-180, bottomLeft.getX(), 0.0);
        assertEquals(-90, bottomLeft.getY(), 0.0);
    }

}
