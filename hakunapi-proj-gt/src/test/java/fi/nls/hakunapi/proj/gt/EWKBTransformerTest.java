package fi.nls.hakunapi.proj.gt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;

import fi.nls.hakunapi.core.projection.EWKBTransformer;
import fi.nls.hakunapi.core.projection.ProjectionTransformer;
import fi.nls.hakunapi.core.projection.ProjectionTransformerFactory;

public class EWKBTransformerTest {

    @Test
    public void testGeoToolsEWKB() throws Exception {
        GeometryFactory gf = new GeometryFactory();
        ProjectionTransformerFactory fact = new GeoToolsProjectionTransformerFactory();
        ProjectionTransformer t = fact.getTransformer(3067, 3879);
        EWKBTransformer ewkbt = new EWKBTransformer(t);

        Geometry g3067 = gf.createPoint(new Coordinate(387352,6673122)).buffer(10, 8);
        
        WKBWriter w = new WKBWriter();
        byte[] bytest3067 = w.write(g3067);
        
        WKBReader r = new WKBReader();
        
        Geometry gWkbRead = r.read(bytest3067);
        
        assertTrue(g3067.equals(gWkbRead));

        ewkbt.transformEWKB(bytest3067);

    

    }
}
