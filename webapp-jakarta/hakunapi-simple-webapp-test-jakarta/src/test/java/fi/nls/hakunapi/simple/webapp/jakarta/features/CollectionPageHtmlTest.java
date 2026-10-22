package fi.nls.hakunapi.simple.webapp.jakarta.features;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import java.io.File;

import jakarta.servlet.ServletContextEvent;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;

import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import fi.nls.hakunapi.simple.webapp.jakarta.HakunaContextListener;
import fi.nls.hakunapi.simple.webapp.jakarta.HakunaTestServletContext;
import fi.nls.hakunapi.simple.webapp.jakarta.SimpleFeaturesApplication;

public class CollectionPageHtmlTest extends JerseyTest {

    @Override
    protected Application configure() {
        File file = new File(getClass().getClassLoader().getResource("hakuna_html_map/hakuna.properties").getFile());
        System.setProperty("hakuna.config.path", file.getParentFile().getAbsolutePath() + "/");

        HakunaTestServletContext sc = new HakunaTestServletContext();
        new HakunaContextListener().contextInitialized(new ServletContextEvent(sc));
        return new SimpleFeaturesApplication(sc);
    }

    private String html(String collectionId) {
        return target("/collections/" + collectionId).request(MediaType.TEXT_HTML).get(String.class);
    }

    @Test
    public void testSchemaLinkAndSpatialExtent() {
        String page = html("fence");

        assertThat(page, containsString("href=\"collections/fence/schema\""));
        assertThat(page, containsString("Spatial extent"));
        assertThat(page, containsString("-110, -60, 140, 30"));
        assertThat(page, not(containsString("Temporal extent")));
    }

    @Test
    public void testTemporalExtent() {
        String page = html("survey");

        assertThat(page, containsString("Temporal extent"));
        assertThat(page, containsString("2020-01-01T00:00:00Z / 2021-01-01T00:00:00Z"));
    }

}
