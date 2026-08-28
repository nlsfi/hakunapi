package fi.nls.hakunapi.simple.webapp.jakarta.features;

import static com.jayway.jsonassert.JsonAssert.with;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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

import fi.nls.hakunapi.simple.webapp.jakarta.HakunaContextListener;
import fi.nls.hakunapi.simple.webapp.jakarta.HakunaTestServletContext;
import fi.nls.hakunapi.simple.webapp.jakarta.SimpleFeaturesApplication;

/**
 * Language negotiation on the landing page.
 *
 * Uses the same hakuna_i18n config as CollectionMetadataI18nTest: locale=en,fi,sv
 * where en has no catalog, fi is fully translated and sv is sparse (it sets
 * api.title but not api.description).
 */
public class LandingPageI18nTest extends JerseyTest {

    @Override
    protected Application configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("hakuna_i18n/hakuna.properties").getFile());

        System.setProperty("hakuna.config.path", file.getParentFile().getAbsolutePath() + "/");

        final HakunaTestServletContext sc = new HakunaTestServletContext();
        final ServletContextEvent sce = new ServletContextEvent(sc);
        new HakunaContextListener().contextInitialized(sce);

        return new SimpleFeaturesApplication(sc);
    }

    /**
     * Negotiation is unit-tested in LangNegotiationTest; what matters here is
     * that it is wired into the resource and reported as Content-Language.
     * lang wins over Accept-Language, an unmatched value falls back to the
     * first declared language, and the sv catalog sets api.title but not
     * api.description, so the description falls back to the base config per key.
     */
    @Test
    public void testNegotiationAndPerKeyFallback() {
        assertTitle(target("/").request().get(),
                "en", "Localization Test API", "Base language description");

        assertTitle(target("/").queryParam("lang", "fi").request().get(),
                "fi", "Kotoistustestin rajapinta", "Peruskäännös suomeksi");

        assertTitle(target("/").request().acceptLanguage("fi").get(),
                "fi", "Kotoistustestin rajapinta", "Peruskäännös suomeksi");

        // explicit lang beats Accept-Language
        assertTitle(target("/").queryParam("lang", "sv").request().acceptLanguage("fi").get(),
                "sv", "Localiseringstest API", "Base language description");

        // unmatched falls back to the first declared language
        assertTitle(target("/").queryParam("lang", "de").request().get(),
                "en", "Localization Test API", "Base language description");
    }

    private static void assertTitle(Response r, String lang, String title, String description) {
        String body = r.readEntity(String.class);
        assertEquals(body, 200, r.getStatus());
        assertEquals(body, lang, r.getHeaderString("Content-Language"));
        with(body)
                .assertThat("$.title", equalTo(title))
                .assertThat("$.description", equalTo(description));
    }

    /**
     * Root.Builder appends the query string to every link it builds, so putting
     * lang there propagates it to api, conformance and collections links.
     */
    @Test
    public void testLangPropagatesIntoAllLinks() {
        Response r = target("/").queryParam("lang", "fi").request().get();
        String body = r.readEntity(String.class);

        assertTrue(body, body.contains("/collections?lang=fi"));
        assertTrue(body, body.contains("/conformance?lang=fi"));
        assertTrue(body, body.contains("/api?lang=fi"));
    }

    @Test
    public void testSelfLinkCarriesHreflang() {
        Response r = target("/").queryParam("lang", "fi").request().get();
        String body = r.readEntity(String.class);

        assertEquals("fi", hreflangOfRel(body, "self"));
    }

    @Test
    public void testAlternateLangLinksArePresent() {
        Response r = target("/").queryParam("lang", "fi").request().get();
        String body = r.readEntity(String.class);

        List<Map<String, Object>> alternates = alternateLangLinks(body);
        assertEquals(2, alternates.size());
        assertTrue(alternates.toString(), alternates.stream()
                .anyMatch(l -> "en".equals(l.get("hreflang")) && ((String) l.get("href")).contains("lang=en")));
        assertTrue(alternates.toString(), alternates.stream()
                .anyMatch(l -> "sv".equals(l.get("hreflang")) && ((String) l.get("href")).contains("lang=sv")));
    }

    /**
     * Additional links are served as configured whatever the negotiated
     * language: a config declares one link per language, each with its own
     * hreflang, rather than relying on a translated title.
     */
    @Test
    public void testAdditionalLinksAreNotLocalized() {
        Response r = target("/").queryParam("lang", "fi").request().get();
        String body = r.readEntity(String.class);

        assertEquals("Dataset metadata", titleOfHref(body, "https://example.org/metadata"));
        assertEquals("Aineiston metatiedot", titleOfHref(body, "https://example.org/metatiedot"));
    }

    @Test
    public void testHtmlVariantSetsContentLanguage() {
        Response r = target("/").queryParam("lang", "fi").request("text/html").get();

        assertEquals(200, r.getStatus());
        assertEquals("fi", r.getHeaderString("Content-Language"));
        String body = r.readEntity(String.class);
        assertTrue(body, body.contains("Kotoistustestin rajapinta"));
    }


    /**
     * Language alternates only. The format alternate (this document as HTML)
     * also carries an hreflang, since it is the same document in the same
     * language, so rel plus hreflang is not enough to tell them apart; a
     * language alternate is the one pointing at a different lang.
     */
    private static List<Map<String, Object>> alternateLangLinks(String body) {
        List<Map<String, Object>> links = com.jayway.jsonpath.JsonPath.read(body, "$.links");
        return links.stream()
                .filter(l -> "alternate".equals(l.get("rel")))
                .filter(l -> l.get("hreflang") != null)
                .filter(l -> !"fi".equals(l.get("hreflang")))
                .collect(java.util.stream.Collectors.toList());
    }

    private static String hreflangOfRel(String body, String rel) {
        List<Map<String, Object>> links = com.jayway.jsonpath.JsonPath.read(body, "$.links");
        for (Map<String, Object> link : links) {
            if (rel.equals(link.get("rel"))) {
                return (String) link.get("hreflang");
            }
        }
        return null;
    }

    private static String titleOfHref(String body, String href) {
        List<Map<String, Object>> links = com.jayway.jsonpath.JsonPath.read(body, "$.links");
        for (Map<String, Object> link : links) {
            if (href.equals(link.get("href"))) {
                return (String) link.get("title");
            }
        }
        return null;
    }

}
