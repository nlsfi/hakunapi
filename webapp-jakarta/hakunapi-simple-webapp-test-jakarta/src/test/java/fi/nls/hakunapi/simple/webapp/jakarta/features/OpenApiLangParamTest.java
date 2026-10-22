package fi.nls.hakunapi.simple.webapp.jakarta.features;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletContextEvent;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;

import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;

import com.jayway.jsonpath.JsonPath;

import fi.nls.hakunapi.simple.webapp.jakarta.HakunaContextListener;
import fi.nls.hakunapi.simple.webapp.jakarta.HakunaTestServletContext;
import fi.nls.hakunapi.simple.webapp.jakarta.SimpleFeaturesApplication;

/**
 * The lang query parameter in the generated OpenAPI document.
 *
 * OpenAPI30Generator picks up @QueryParam reflectively, so lang would appear
 * even with no help, but as a bare string with no description. @ParamClass
 * routes it through LangParam instead, which describes it and enumerates the
 * languages the service actually serves. ParamClass had no other user in the
 * codebase, so this also covers that generator path for the first time.
 */
public class OpenApiLangParamTest extends JerseyTest {

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

    private String api() {
        Response r = target("/api").request("application/vnd.oai.openapi+json;version=3.0").get();
        String body = r.readEntity(String.class);
        assertEquals(body, 200, r.getStatus());
        return body;
    }

    @Test
    public void testApiDocumentGenerates() {
        // The whole document must build; a missing @ResponseClass on a Response
        // returning method would NPE the generator instead
        assertNotNull(api());
    }

    @Test
    public void testLangIsDocumentedAsACommonParameter() {
        String body = api();

        Map<String, Object> param = JsonPath.read(body, "$.components.parameters.lang");
        assertEquals("lang", param.get("name"));
        assertEquals("query", param.get("in"));
        assertTrue(param.toString(), ((String) param.get("description")).contains("Accept-Language"));
    }

    /**
     * The enumeration comes from the service's declared languages, so the
     * document tells a client what it can actually ask for.
     */
    @Test
    public void testLangEnumeratesDeclaredLanguages() {
        String body = api();

        List<String> values = JsonPath.read(body, "$.components.parameters.lang.schema.enum");
        assertEquals(List.of("en", "fi", "sv"), values);
        assertEquals("en", JsonPath.read(body, "$.components.parameters.lang.schema.default"));
    }

    /**
     * Each localized path carries a parameter entry for lang.
     *
     * The entry itself serializes as an empty object, because a common
     * parameter is emitted as a $ref and swagger-models annotates $ref with a
     * Jackson 2 annotation that this build's Jackson 3 mapper does not pick up.
     * That is pre-existing and not specific to lang: the untouched
     * /items/{featureId} path shows the same empty objects for its own common
     * parameters, as does every response schema in the document. So this only
     * asserts the arity, which is what is observable, and the resolved
     * definition is asserted in components above.
     */
    @Test
    public void testLocalizedPathsCarryALangParameter() {
        String body = api();

        // path -> parameters expected: its own, plus the lang ref
        Map<String, Integer> expected = Map.of(
                "$['paths']['/']", 1,
                "$['paths']['/collections']", 1,
                "$['paths']['/collections/{collectionId}']", 2,
                "$['paths']['/collections/{collectionId}/schema']", 2);

        for (Map.Entry<String, Integer> e : expected.entrySet()) {
            List<Map<String, Object>> params = JsonPath.read(body, e.getKey() + ".get.parameters");
            assertEquals(e.getKey() + " -> " + params, e.getValue().intValue(), params.size());
        }
    }

    /**
     * A path with no lang parameter is unaffected, so the four above are not
     * simply picking up something every path has.
     */
    @Test
    public void testConformancePathHasNoLangParameter() {
        String body = api();

        // no parameters at all means the key is absent rather than an empty list
        Map<String, Object> get = JsonPath.read(body, "$['paths']['/conformance'].get");
        assertFalse(get.toString(), get.containsKey("parameters"));
    }

}
