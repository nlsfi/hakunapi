package fi.nls.hakunapi.core.param;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import fi.nls.hakunapi.core.FeatureProducer;
import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.OutputFormat;
import fi.nls.hakunapi.core.SRIDCode;
import fi.nls.hakunapi.core.SimpleFeatureType;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;
import fi.nls.hakunapi.core.request.GetFeatureCollection;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import fi.nls.hakunapi.core.util.CrsUtil;

public class CrsParamTest {

    private static final String EPSG_3067 = "http://www.opengis.net/def/crs/EPSG/0/3067";

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
        myCollection.setGeom(new HakunaPropertyGeometry("geometry", "table", null, false, null, new int[] { 3067 }, 84, 2, (__, ___, ____) -> {}));

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
    }

    private GetFeatureRequest request() {
        GetFeatureRequest req = new GetFeatureRequest();
        req.addCollection(new GetFeatureCollection(myCollection));
        return req;
    }

    @Test
    public void testSridSupportedByCollectionButUnknownToService() {
        service.setKnownSrids(Arrays.asList(SRIDCode.CRS84, SRIDCode.WGS84));
        try {
            new CrsParam().modify(service, request(), EPSG_3067);
        } catch (IllegalArgumentException e) {
            assertEquals(CrsUtil.ERR_UNSUPPORTED_CRS, e.getMessage());
            return;
        }
        throw new AssertionError("Expected IllegalArgumentException");
    }

    @Test
    public void testSridKnownToService() {
        service.setKnownSrids(Arrays.asList(SRIDCode.CRS84, SRIDCode.WGS84,
                new SRIDCode(3067, false, false, fi.nls.hakunapi.core.geom.HakunaGeometryDimension.XY)));
        GetFeatureRequest req = request();
        new CrsParam().modify(service, req, EPSG_3067);
        assertEquals(3067, req.getSRID());
    }

}
