package fi.nls.hakunapi.simple.webapp.jakarta.tiles;

import static com.jayway.jsonassert.JsonAssert.with;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

import java.io.File;

import jakarta.servlet.ServletContextEvent;
import jakarta.ws.rs.core.Application;

import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import fi.nls.hakunapi.simple.webapp.jakarta.HakunaContextListener;
import fi.nls.hakunapi.simple.webapp.jakarta.HakunaTestServletContext;
import fi.nls.hakunapi.simple.webapp.jakarta.SimpleFeaturesApplication;

public class ApiExtensionDiscoveryTest extends JerseyTest {

    private static final String TILES_CORE =
            "http://www.opengis.net/spec/ogcapi-tiles-1/1.0/conf/core";

    @Override
    protected Application configure() {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("hakuna/hakuna.properties").getFile());
        System.setProperty("hakuna.config.path", file.getParentFile().getAbsolutePath() + "/");

        final HakunaTestServletContext sc = new HakunaTestServletContext();
        final ServletContextEvent sce = new ServletContextEvent(sc);
        new HakunaContextListener().contextInitialized(sce);

        return new SimpleFeaturesApplication(sc);
    }

    @Test
    public void testUnconfiguredExtensionPublishesNothing() throws Exception {
        final String conformance = target("/conformance").request().get(String.class);
        with(conformance)
                .assertThat("$.conformsTo",
                        hasItems(equalTo("http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/core")))
                .assertThat("$.conformsTo", not(hasItems(equalTo(TILES_CORE))));

        final String landingPage = target("/").request().get(String.class);
        with(landingPage).assertThat(
                "$.links[?(@.rel == 'http://www.opengis.net/def/rel/ogc/1.0/tiling-schemes')]",
                hasSize(0));
    }

}
