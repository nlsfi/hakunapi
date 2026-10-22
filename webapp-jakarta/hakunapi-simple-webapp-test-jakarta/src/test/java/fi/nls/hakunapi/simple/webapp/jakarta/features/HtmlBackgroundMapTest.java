package fi.nls.hakunapi.simple.webapp.jakarta.features;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import jakarta.servlet.ServletContextEvent;
import jakarta.ws.rs.core.Application;

import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import fi.nls.hakunapi.simple.webapp.jakarta.HakunaContextListener;
import fi.nls.hakunapi.simple.webapp.jakarta.HakunaTestServletContext;
import fi.nls.hakunapi.simple.webapp.jakarta.SimpleFeaturesApplication;

public class HtmlBackgroundMapTest extends JerseyTest {

    private static final String OSM = "L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png'";
    private static final String CRS84 = "http://www.opengis.net/def/crs/OGC/1.3/CRS84";
    private static final String CRS_PREFIX = "http://www.opengis.net/def/crs/EPSG/0/";

    @Override
    protected Application configure() {
        File file = new File(getClass().getClassLoader().getResource("hakuna_html_map/hakuna.properties").getFile());
        System.setProperty("hakuna.config.path", file.getParentFile().getAbsolutePath() + "/");

        HakunaTestServletContext sc = new HakunaTestServletContext();
        new HakunaContextListener().contextInitialized(new ServletContextEvent(sc));
        return new SimpleFeaturesApplication(sc);
    }

    @Test
    public void testItemsPageBackgroundMaps() {
        // Web mercator: default OpenStreetMap
        String mercator = items("3857");
        assertTrue(mercator.contains(OSM));
        assertTrue(mercator.contains("var crs = L.CRS.EPSG3857;"));

        // Configured service for the CRS replaces the default
        String geographic = items("84");
        assertTrue(geographic.contains("L.tileLayer('https://example.org/WGS84_Pseudo-Mercator/{z}/{y}/{x}.png'"));
        assertFalse(geographic.contains(OSM));

        // TM35FIN without a configured service: OpenStreetMap does not serve it, geometries only
        String tm35 = items("3067");
        assertFalse(tm35.contains("L.tileLayer"));
        assertTrue(tm35.contains("L.Proj.CRS(\"EPSG:3067\""));
        assertTrue(tm35.contains("No background map for this coordinate system"));
    }

    @Test
    public void testItemPageBackgroundMaps() {
        assertTrue(item("3857").contains(OSM));
        assertFalse(item("3067").contains("L.tileLayer"));
    }

    private String items(String srid) {
        return html("/collections/fence/items", srid);
    }

    private String item(String srid) {
        return html("/collections/fence/items/1", srid);
    }

    private String html(String path, String srid) {
        jakarta.ws.rs.core.Response r = target(path)
                .queryParam("f", "html")
                .queryParam("crs", "84".equals(srid) ? CRS84 : CRS_PREFIX + srid)
                .request().get();
        assertEquals(200, r.getStatus());
        return r.readEntity(String.class);
    }

}
