package fi.nls.hakunapi.core.geom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

public class EWKTReaderTest {

    @Test
    public void test() throws ParseException {
        WKTReader wkt = new WKTReader();
        EWKTReader ewkt = new EWKTReader(wkt);

        String a = "SRID=4269;LINESTRING(-71.160281 42.258729,-71.160837 42.259113,-71.161144 42.25932)";
        String b = "SRID=4269;POINT(-71.064544 42.28787)";
        String c = "SRID=4269;POLYGON((-71.1776585052917 42.3902909739571,-71.1776820268866 42.3903701743239,"
                +  "-71.1776063012595 42.3903825660754,-71.1775826583081 42.3903033653531,-71.1776585052917 42.3902909739571))";

        assertEquals(4269, ewkt.read(a).getSRID());
        assertEquals(4269, ewkt.read(b).getSRID());
        assertEquals(4269, ewkt.read(c).getSRID());

        a = "SRID=0;LINESTRING(-71.160281 42.258729,-71.160837 42.259113,-71.161144 42.25932)";
        b = "SRID=0;POINT(-71.064544 42.28787)";
        c = "SRID=0;POLYGON((-71.1776585052917 42.3902909739571,-71.1776820268866 42.3903701743239,"
                +  "-71.1776063012595 42.3903825660754,-71.1775826583081 42.3903033653531,-71.1776585052917 42.3902909739571))";

        assertEquals(0, ewkt.read(a).getSRID());
        assertEquals(0, ewkt.read(b).getSRID());
        assertEquals(0, ewkt.read(c).getSRID());

        a = "LINESTRING(-71.160281 42.258729,-71.160837 42.259113,-71.161144 42.25932)";
        b = "POINT(-71.064544 42.28787)";
        c = "POLYGON((-71.1776585052917 42.3902909739571,-71.1776820268866 42.3903701743239,"
                +  "-71.1776063012595 42.3903825660754,-71.1775826583081 42.3903033653531,-71.1776585052917 42.3902909739571))";

        assertEquals(0, ewkt.read(a).getSRID());
        assertEquals(0, ewkt.read(b).getSRID());
        assertEquals(0, ewkt.read(c).getSRID());

        a = "SRID=foo;LINESTRING(-71.160281 42.258729,-71.160837 42.259113,-71.161144 42.25932)";
        b = "SRID=fooPOINT(-71.064544 42.28787)";
        c = "SRID=;POLYGON((-71.1776585052917 42.3902909739571,-71.1776820268866 42.3903701743239,"
                +  "-71.1776063012595 42.3903825660754,-71.1775826583081 42.3903033653531,-71.1776585052917 42.3902909739571))";

        try {
            assertEquals(0, ewkt.read(a).getSRID());
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid EWKT", e.getMessage());
        }
        try {
            assertEquals(0, ewkt.read(b).getSRID());
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid EWKT", e.getMessage());
        }
        try {
            assertEquals(0, ewkt.read(c).getSRID());
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid EWKT", e.getMessage());
        }
    }

}
