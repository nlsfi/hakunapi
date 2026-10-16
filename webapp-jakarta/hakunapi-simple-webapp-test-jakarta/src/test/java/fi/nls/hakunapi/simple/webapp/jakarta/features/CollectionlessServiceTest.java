package fi.nls.hakunapi.simple.webapp.jakarta.features;

import static com.jayway.jsonassert.JsonAssert.with;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasItem;

import java.io.File;

import jakarta.servlet.ServletContextEvent;
import jakarta.ws.rs.core.Application;

import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import fi.nls.hakunapi.simple.webapp.jakarta.HakunaContextListener;
import fi.nls.hakunapi.simple.webapp.jakarta.HakunaTestServletContext;
import fi.nls.hakunapi.simple.webapp.jakarta.SimpleFeaturesApplication;

public class CollectionlessServiceTest extends JerseyTest {

    @Override
    protected Application configure() {
        File file = new File(getClass().getClassLoader().getResource("hakuna_collectionless/hakuna.properties").getFile());
        System.setProperty("hakuna.config.path", file.getParentFile().getAbsolutePath() + "/");

        HakunaTestServletContext sc = new HakunaTestServletContext();
        new HakunaContextListener().contextInitialized(new ServletContextEvent(sc));
        return new SimpleFeaturesApplication(sc);
    }

    @Test
    public void testConformanceHasOnlyCommonClasses() {
        String response = target("/conformance").request().get(String.class);

        with(response)
                .assertThat("$.conformsTo", hasItems(
                        equalTo("http://www.opengis.net/spec/ogcapi-common-1/1.0/conf/core"),
                        equalTo("http://www.opengis.net/spec/ogcapi-common-1/1.0/conf/json"),
                        equalTo("http://www.opengis.net/spec/ogcapi-common-1/1.0/conf/html"),
                        equalTo("http://www.opengis.net/spec/ogcapi-common-1/1.0/conf/oas30"),
                        equalTo("http://www.opengis.net/spec/ogcapi-common-2/1.0/conf/collections")))
                .assertThat("$.conformsTo", not(hasItem(equalTo("http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/core"))))
                .assertThat("$.conformsTo", not(hasItem(equalTo("http://www.opengis.net/spec/ogcapi-features-3/1.0/conf/queryables"))))
                .assertThat("$.conformsTo", not(hasItem(equalTo("http://www.opengis.net/spec/cql2/1.0/conf/cql2-text"))));
    }

    @Test
    public void testApiHasNoQueryablesPath() {
        String response = target("/api").request().get(String.class);

        org.junit.Assert.assertFalse(response.contains("queryables"));
    }
}
