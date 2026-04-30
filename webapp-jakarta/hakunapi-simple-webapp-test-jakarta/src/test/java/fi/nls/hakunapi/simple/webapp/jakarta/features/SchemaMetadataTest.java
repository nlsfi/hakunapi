package fi.nls.hakunapi.simple.webapp.jakarta.features;

import static com.jayway.jsonassert.JsonAssert.with;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;

import java.io.File;

import jakarta.servlet.ServletContextEvent;
import jakarta.ws.rs.core.Application;

import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.nls.hakunapi.simple.webapp.jakarta.HakunaContextListener;
import fi.nls.hakunapi.simple.webapp.jakarta.HakunaTestServletContext;
import fi.nls.hakunapi.simple.webapp.jakarta.SimpleFeaturesApplication;

public class SchemaMetadataTest extends JerseyTest {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaMetadataTest.class);

    @Override
    protected Application configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("hakuna_schema/hakuna.properties").getFile());

        System.setProperty("hakuna.config.path", file.getParentFile().getAbsolutePath() + "/");

        final HakunaTestServletContext sc = new HakunaTestServletContext();
        final ServletContextEvent sce = new ServletContextEvent(sc);
        new HakunaContextListener().contextInitialized(sce);

        final Application app = new SimpleFeaturesApplication(sc);
        return app;
    }

    @Test
    public void testCollectionTitleFromSchema() {
        final String response = target("/collections/building_part_area").request().get(String.class);
        LOG.info(response);

        with(response)
                .assertThat("$.id", equalTo("building_part_area"))
                .assertThat("$.title", equalTo("Building part (area)"))
                .assertThat("$.description", equalTo("Building parts represented as areas"));
    }

    @Test
    public void testCollectionTitleFromSchemaFence() {
        final String response = target("/collections/fence").request().get(String.class);
        LOG.info(response);

        with(response)
                .assertThat("$.id", equalTo("fence"))
                .assertThat("$.title", equalTo("Fence"))
                .assertThat("$.description", equalTo("A fence or wall structure"));
    }

    @Test
    public void testPropertyMetadataInSchema() {
        final jakarta.ws.rs.core.Response r = target("/collections/building_part_area/schema").request().get();
        final String response = r.readEntity(String.class);
        LOG.info("Status: {} Body: {}", r.getStatus(), response);

        org.junit.Assert.assertEquals(200, r.getStatus());

        with(response)
                .assertThat("$.properties.properties.properties.name.title", equalTo("Name"))
                .assertThat("$.properties.properties.properties.name.description", equalTo("Name of the building part"))
                .assertThat("$.properties.properties.properties.building_function_id.title", equalTo("Function"))
                .assertThat("$.properties.properties.properties.building_function_id.enum", hasItems(1, 2, 3, 4))
                .assertThat("$.properties.properties.properties.building_function_id.x-codelist-uri",
                        equalTo("https://uri.suomi.fi/codelist/maastotest/building_function_v1"));
    }

}
