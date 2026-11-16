package fi.nls.hakunapi.simple.webapp.jakarta.features;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ServletContextEvent;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;

import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;

import fi.nls.hakunapi.simple.webapp.jakarta.HakunaContextListener;
import fi.nls.hakunapi.simple.webapp.jakarta.HakunaTestServletContext;
import fi.nls.hakunapi.simple.webapp.jakarta.SimpleFeaturesApplication;

/**
 * Byte-exact output of the generated documents for the hakuna_i18n config: the
 * OpenAPI document, and the schema and queryables of building_part_area in its
 * base language and in fi.
 *
 * These go through the OpenAPI mixins and the SchemaDefinition path of
 * OpenAPIMessageBodyWriter, so a change to the serialization stack shows up here
 * as a diff rather than as a silently different document.
 */
public class GoldenDocumentTest extends JerseyTest {

    private static final String OPENAPI = "application/vnd.oai.openapi+json;version=3.0";
    private static final String SCHEMA = "application/schema+json";

    @Override
    protected Application configure() {
        enable(TestProperties.LOG_TRAFFIC);

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("hakuna_i18n/hakuna.properties").getFile());

        System.setProperty("hakuna.config.path", file.getParentFile().getAbsolutePath() + "/");

        final HakunaTestServletContext sc = new HakunaTestServletContext();
        final ServletContextEvent sce = new ServletContextEvent(sc);
        new HakunaContextListener().contextInitialized(sce);

        return new SimpleFeaturesApplication(sc);
    }

    @Test
    public void testOpenApiDocument() throws Exception {
        assertEquals(golden("api.json"), fetch("/api", OPENAPI, null));
    }

    @Test
    public void testCollectionSchema() throws Exception {
        assertEquals(golden("schema.json"), fetch("/collections/building_part_area/schema", SCHEMA, null));
        assertEquals(golden("schema-fi.json"), fetch("/collections/building_part_area/schema", SCHEMA, "fi"));
    }

    @Test
    public void testCollectionQueryables() throws Exception {
        assertEquals(golden("queryables.json"), fetch("/collections/building_part_area/queryables", SCHEMA, null));
        assertEquals(golden("queryables-fi.json"), fetch("/collections/building_part_area/queryables", SCHEMA, "fi"));
    }

    private String fetch(String path, String accept, String lang) {
        WebTarget target = target(path);
        if (lang != null) {
            target = target.queryParam("lang", lang);
        }
        Response r = target.request(accept).get();
        String body = r.readEntity(String.class);
        assertEquals(200, r.getStatus());
        return body;
    }

    private static String golden(String name) throws IOException {
        try (InputStream in = GoldenDocumentTest.class.getResourceAsStream("/golden/" + name)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

}
