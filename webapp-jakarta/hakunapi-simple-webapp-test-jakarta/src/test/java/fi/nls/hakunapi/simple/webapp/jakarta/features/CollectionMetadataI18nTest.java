package fi.nls.hakunapi.simple.webapp.jakarta.features;

import static com.jayway.jsonassert.JsonAssert.with;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
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
 * Language negotiation on /collections and /collections/{id}.
 *
 * The hakuna_i18n config declares locale=en,fi,sv where en has no catalog (so it
 * is served from the base config), fi is fully translated and sv is deliberately
 * sparse, covering only one of the two collections and no api.description.
 */
public class CollectionMetadataI18nTest extends JerseyTest {

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
     * that it reaches the collection resource. An unmatched language falls back
     * to the first declared one, whose base title comes off the schema file
     * rather than a catalog. The sv catalog covers fence but not
     * building_part_area, so per-key fallback keeps the untranslated collection
     * on its base title.
     */
    @Test
    public void testNegotiationAndPerCollectionFallback() {
        assertTitle(target("/collections/building_part_area").queryParam("lang", "fi").request().get(),
                "fi", "Rakennuksen osa (alue)");

        assertTitle(target("/collections/building_part_area").request().acceptLanguage("fi").get(),
                "fi", "Rakennuksen osa (alue)");

        // explicit lang beats Accept-Language
        assertTitle(target("/collections/fence").queryParam("lang", "sv").request().acceptLanguage("fi").get(),
                "sv", "Staket");

        // unmatched falls back to the first declared language
        assertTitle(target("/collections/fence").queryParam("lang", "de").request().get(),
                "en", "Fence");

        // sv catalog does not cover this collection, so its base title stands
        assertTitle(target("/collections/building_part_area").queryParam("lang", "sv").request().get(),
                "sv", "Building part (area)");
    }

    private static void assertTitle(Response r, String lang, String title) {
        String body = r.readEntity(String.class);
        assertEquals(body, 200, r.getStatus());
        assertEquals(body, lang, r.getHeaderString("Content-Language"));
        with(body).assertThat("$.title", equalTo(title));
    }

    @Test
    public void testLangPropagatesIntoLinkHrefs() {
        Response r = target("/collections/building_part_area").queryParam("lang", "fi").request().get();
        String body = r.readEntity(String.class);

        // items, describedby and queryables all come off the same queryParams map
        assertTrue(body, body.contains("/items?lang=fi") || body.contains("lang=fi"));
        assertTrue(body, body.contains("/schema?lang=fi"));
        assertTrue(body, body.contains("/queryables?lang=fi"));
    }

    /**
     * describedby points at the localized schema, so it declares the language it
     * will be served in. This is the first use of Link.hreflang in hakunapi.
     */
    @Test
    public void testDescribedByCarriesHreflang() {
        Response r = target("/collections/building_part_area").queryParam("lang", "fi").request().get();
        String body = r.readEntity(String.class);

        assertEquals("fi", hreflangOfRel(body, "describedby"));
    }

    @Test
    public void testAlternateLangLinksArePresent() {
        Response r = target("/collections/fence").queryParam("lang", "fi").request().get();
        String body = r.readEntity(String.class);

        // en and sv are the other declared languages
        assertTrue(body, body.contains("\"hreflang\":\"en\"") || body.contains("lang=en"));
        assertTrue(body, body.contains("\"hreflang\":\"sv\"") || body.contains("lang=sv"));
    }

    @Test
    public void testCollectionsListingIsLocalized() {
        Response r = target("/collections").queryParam("lang", "fi").request().get();
        String body = r.readEntity(String.class);

        assertEquals(200, r.getStatus());
        assertEquals("fi", r.getHeaderString("Content-Language"));
        with(body)
                .assertThat("$.collections[0].title", equalTo("Rakennuksen osa (alue)"))
                .assertThat("$.collections[1].title", equalTo("Aita"));
    }

    /**
     * Catalogs are sparse: sv translates only fence, so building_part_area
     * renders its base title inside the Swedish listing rather than being
     * dropped or blanked.
     */
    @Test
    public void testSparseCatalogRendersBaseTitleForUntranslatedCollections() {
        Response r = target("/collections").queryParam("lang", "sv").request().get();
        String body = r.readEntity(String.class);

        assertEquals("sv", r.getHeaderString("Content-Language"));
        with(body)
                .assertThat("$.collections[0].title", equalTo("Building part (area)"))
                .assertThat("$.collections[1].title", equalTo("Staket"));
    }

    @Test
    public void testCollectionsSelfLinkCarriesHreflangAndLang() {
        Response r = target("/collections").queryParam("lang", "fi").request().get();
        String body = r.readEntity(String.class);

        assertEquals("fi", hreflangOfRel(body, "self"));
        assertTrue(body, body.contains("/collections?lang=fi"));
    }

    /**
     * api.links.* is service level, so its localized title is asserted on the
     * landing page in LandingPageI18nTest. Here it is enough that a collection
     * with no additional links of its own renders its localized title.
     */
    @Test
    public void testLocalizedTitleWithNoCollectionLinks() {
        Response r = target("/collections/fence").queryParam("lang", "fi").request().get();
        String body = r.readEntity(String.class);

        with(body).assertThat("$.title", equalTo("Aita"));
    }

    /**
     * No lang at all still negotiates, because a declared locale= list means the
     * resource has a language to report even when the client expresses no
     * preference.
     */
    @Test
    public void testNoLangRequestedStillReportsContentLanguage() {
        Response r = target("/collections/fence").request().get();
        String body = r.readEntity(String.class);

        assertEquals(200, r.getStatus());
        assertEquals("en", r.getHeaderString("Content-Language"));
        with(body).assertThat("$.title", equalTo("Fence"));
    }

    /**
     * Reads the hreflang of the first link with the given rel. Done by walking the
     * parsed JSON rather than with a JsonPath filter expression, which is
     * awkward to index reliably here.
     */
    private static String hreflangOfRel(String body, String rel) {
        List<Map<String, Object>> links = com.jayway.jsonpath.JsonPath.read(body, "$.links");
        for (Map<String, Object> link : links) {
            if (rel.equals(link.get("rel"))) {
                return (String) link.get("hreflang");
            }
        }
        return null;
    }

    @Test
    public void testHtmlVariantSetsContentLanguage() {
        Response r = target("/collections/fence").queryParam("lang", "fi")
                .request("text/html").get();

        assertEquals(200, r.getStatus());
        assertEquals("fi", r.getHeaderString("Content-Language"));
        String body = r.readEntity(String.class);
        assertTrue(body, body.contains("Aita"));
    }

}
