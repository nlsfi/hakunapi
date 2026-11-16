package fi.nls.hakunapi.proj.jhe;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class JHeWKTTest {

    @Test
    public void testGK25() {
        String wkt = JHeWKT.get(3879);
        assertTrue(wkt, wkt.startsWith("PROJCS[\"ETRS89-FIN [EUREF-FIN] / GK25FIN\""));
        assertTrue(wkt, wkt.endsWith("AUTHORITY[\"EPSG\",\"3879\"]]"));
    }

    @Test
    public void testUnsupported() {
        assertNull(JHeWKT.get(3068));
    }

}
