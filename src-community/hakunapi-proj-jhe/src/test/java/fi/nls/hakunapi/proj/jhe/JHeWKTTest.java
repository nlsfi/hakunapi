package fi.nls.hakunapi.proj.jhe;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class JHeWKTTest {

    @Test
    public void testAxisOrderGK25() {
        String xy = JHeWKT.get(3879, true);
        String authority = JHeWKT.get(3879, false);
        assertTrue(xy, xy.startsWith("PROJCS[\"EUREF-FIN / GK25FIN\""));
        assertTrue(xy, xy.endsWith("AUTHORITY[\"EPSG\",\"3879\"]]"));
        assertTrue(xy, xy.indexOf("AXIS[\"Easting\"") < xy.indexOf("AXIS[\"Northing\""));
        assertTrue(authority, authority.indexOf("AXIS[\"Northing\"") < authority.indexOf("AXIS[\"Easting\""));
    }

    @Test
    public void testUnsupported() {
        assertNull(JHeWKT.get(3068, true));
    }

}
