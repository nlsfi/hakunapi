package fi.nls.hakunapi.simple.webapp.jakarta.features;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

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
 * HTML rendering of the localized resources, plus regression cover for the
 * pages that are not localized.
 *
 * The templates read lang, langQuery, availableLanguages and alternateLanguages
 * off HTMLContext. Resources built through the original HTMLContext constructor
 * supply none of them, so every template has to render correctly with lang
 * absent; that is what the not-localized cases here check.
 */
public class HtmlI18nTest extends JerseyTest {

    /** Every page that renders the shared header bar. */
    private static final String[] PAGES = {
            "/",
            "/collections",
            "/collections/fence",
            "/conformance",
            "/collections/fence/queryables",
            "/collections/fence/items",
            "/collections/fence/items/1" };

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

    private String html(String path, String lang) {
        Response r = lang == null
                ? target(path).request("text/html").get()
                : target(path).queryParam("lang", lang).request("text/html").get();
        String body = r.readEntity(String.class);
        assertEquals(path + " lang=" + lang + " -> " + body, 200, r.getStatus());
        return body;
    }

    /* Chrome shared by every page */

    /**
     * The picker is a select in the header bar, left of the JSON link, listing
     * every available language with the resolved one selected -- so unlike the
     * alternates list it includes the current language. It navigates by script
     * rather than plain hrefs so that other query parameters survive the switch.
     *
     * Every page renders the same markup, so all of them are checked here rather
     * than once per page.
     */
    @Test
    public void testLanguagePicker() {
        for (String path : PAGES) {
            String body = html(path, "fi");

            assertTrue(path, body.contains("id=\"lang-select\""));
            assertTrue(path, body.contains("<option value=\"en\" lang=\"en\">en</option>"));
            assertTrue(path, body.contains("<option value=\"fi\" selected lang=\"fi\">fi</option>"));
            assertTrue(path, body.contains("<option value=\"sv\" lang=\"sv\">sv</option>"));

            int picker = body.indexOf("id=\"lang-select\"");
            int json = body.indexOf("id=\"json-link\"");
            assertTrue(path + ": picker at " + picker + ", json at " + json, picker > 0 && picker < json);

            assertTrue(path, body.contains("searchParams.set('lang', this.value)"));
            assertTrue(path, body.contains("window.location.assign"));
        }
    }

    /**
     * The html lang attribute names the language actually served. A localized
     * resource asked for nothing still resolves to the default, so it names that
     * rather than falling back to the template's hardcoded en.
     */
    @Test
    public void testHtmlLangAttribute() {
        for (String path : PAGES) {
            assertTrue(path, html(path, "fi").contains("<html lang=\"fi\">"));
            assertTrue(path, html(path, "sv").contains("<html lang=\"sv\">"));
            assertTrue(path, html(path, null).contains("<html lang=\"en\">"));
        }
    }

    /**
     * Breadcrumbs navigate back up, so they have to keep the language too;
     * otherwise switching language and then going up silently loses it. The
     * conformance case is the round trip that motivated this: it has no
     * localized content of its own, but dropping the language on its Home link
     * sends you back to the default.
     */
    @Test
    public void testBreadcrumbsAndLinksCarryLang() {
        assertTrue(html("/", "fi").contains("href=\"collections?lang=fi\""));
        assertTrue(html("/", "fi").contains("href=\"conformance?lang=fi\""));

        assertTrue(html("/collections", "fi").contains("?lang=fi\">Home</a>"));
        assertTrue(html("/collections", "fi").contains("href=\"collections/fence?lang=fi\""));

        String info = html("/collections/fence", "fi");
        assertTrue(info, info.contains("?lang=fi\">Home</a>"));
        assertTrue(info, info.contains("href=\"collections?lang=fi\">Collections</a>"));
        assertTrue(info, info.contains("href=\"collections/fence/items?lang=fi\""));
        assertTrue(info, info.contains("href=\"collections/fence/queryables?lang=fi\""));
        // the schema link was missing from this page entirely (#185)
        assertTrue(info, info.contains("href=\"collections/fence/schema?lang=fi\""));

        assertTrue(html("/conformance", "fi").contains("?lang=fi\">Home</a>"));

        String queryables = html("/collections/fence/queryables", "fi");
        assertTrue(queryables, queryables.contains("?lang=fi\">Home</a>"));
        assertTrue(queryables, queryables.contains("href=\"collections?lang=fi\">Collections</a>"));
        assertTrue(queryables, queryables.contains("href=\"collections/fence?lang=fi\">"));

        String items = html("/collections/fence/items", "fi");
        assertTrue(items, items.contains("?lang=fi\">Home</a>"));
        assertTrue(items, items.contains("collections?lang=fi\">Collections</a>"));

        String feature = html("/collections/fence/items/1", "fi");
        assertTrue(feature, feature.contains("?lang=fi\">Home</a>"));
        assertTrue(feature, feature.contains("/items?lang=fi\">Items</a>"));
    }

    /* Localized content */

    /**
     * Titles and descriptions come from the catalog. The listing renders
     * "collection_id (Title)" and the collection page shows id, title and
     * description; the id was previously absent from that page entirely.
     */
    @Test
    public void testLocalizedTitlesAndDescriptions() {
        String landing = html("/", "fi");
        assertTrue(landing, landing.contains("Kotoistustestin rajapinta"));
        assertTrue(landing, landing.contains("Peruskäännös suomeksi"));

        String listing = html("/collections", "fi");
        assertTrue(listing, listing.contains("building_part_area (Rakennuksen osa (alue))"));
        assertTrue(listing, listing.contains("fence (Aita)"));

        String info = html("/collections/building_part_area", "fi");
        assertTrue(info, info.contains("Rakennuksen osa (alue)"));
        assertTrue(info, info.contains("building_part_area"));
        assertTrue(info, info.contains("Rakennusten osat alueina, ääkkösiä"));
    }

    /**
     * The items and feature pages head themselves with the same collection
     * title, id and description as /collections/{collectionId}. Only the
     * FeatureType handed to the writer for display is localized, so nothing
     * localized reaches the data path.
     */
    @Test
    public void testFeaturePageHeadersAreLocalized() {
        String items = html("/collections/fence/items", "fi");
        assertTrue(items, items.contains("<h1 class=\"mb-0\">Aita</h1>"));
        assertTrue(items, items.contains("<small class=\"text-secondary\">fence</small>"));
        assertTrue(items, items.contains("Aita tai muurirakenne"));

        String feature = html("/collections/fence/items/1", "fi");
        assertTrue(feature, feature.contains("Aita / 1"));
        assertTrue(feature, feature.contains("<small class=\"text-secondary\">fence</small>"));
        assertTrue(feature, feature.contains("Aita tai muurirakenne"));
    }

    /**
     * The collection segment of a breadcrumb mirrors the URL path, so it shows
     * the technical id rather than the localized title: it stays stable across
     * languages and matches the address bar.
     */
    @Test
    public void testBreadcrumbShowsCollectionIdNotTitle() {
        String info = html("/collections/fence", "fi");
        assertTrue(info, info.contains("aria-current=\"page\">fence</li>"));
        // the localized title still heads the page itself
        assertTrue(info, info.contains("<h1 class=\"mb-0\">Aita</h1>"));

        String queryables = html("/collections/fence/queryables", "fi");
        assertTrue(queryables, queryables.contains(">fence</a>"));
        assertFalse(queryables, queryables.contains(">Aita</a>"));
    }

    /**
     * Language alternates are links on the model like any other, so they must
     * not fall through into the Additional Resources block.
     */
    @Test
    public void testAlternatesAreNotAdditionalResources() {
        String landing = html("/", "fi");
        int additional = landing.indexOf("Additional Resources");
        if (additional >= 0) {
            String block = landing.substring(additional, landing.indexOf("</main>", additional));
            assertFalse(block, block.contains("lang=en"));
            assertFalse(block, block.contains("lang=sv"));
        }
        // the configured additional link is still shown, with its localized title
        assertTrue(landing, landing.contains("Aineiston metatiedot"));

        assertFalse(html("/collections/fence", "fi").contains("Additional Resources"));
    }

    /* The feature-data path */

    /**
     * Feature data itself is untouched by the language. The links do carry lang,
     * which is the propagation working, and the timestamp naturally differs, so
     * this compares the features array rather than the whole document.
     */
    @Test
    public void testFeatureDataIsNotAffectedByLanguage() {
        Response fi = target("/collections/fence/items").queryParam("lang", "fi")
                .request("application/geo+json").get();
        Response en = target("/collections/fence/items").queryParam("lang", "en")
                .request("application/geo+json").get();

        assertEquals(200, fi.getStatus());
        assertEquals(200, en.getStatus());

        Object fiFeatures = JsonPath.read(fi.readEntity(String.class), "$.features");
        Object enFeatures = JsonPath.read(en.readEntity(String.class), "$.features");
        assertEquals(enFeatures.toString(), fiFeatures.toString());
    }

    /**
     * The metadata pages propagate lang into the items links they build, so
     * those links have to actually work. They previously 400'd with "Unknown
     * parameter 'lang'": the feature-data operations validate query parameters
     * against their known set, and lang is not one of them. Asserting the link
     * text was not enough; this follows it.
     *
     * A genuinely unknown parameter must still be rejected -- accepting lang has
     * not turned the check off.
     */
    @Test
    public void testItemsAcceptLangButStillRejectUnknownParameters() {
        for (String path : new String[] { "/collections/fence/items", "/collections/fence/items/1" }) {
            Response ok = target(path).queryParam("lang", "fi").request().get();
            assertEquals(path + " -> " + ok.readEntity(String.class), 200, ok.getStatus());
        }

        Response bad = target("/collections/fence/items").queryParam("nonsense", "x").request().get();
        assertEquals(400, bad.getStatus());
        assertTrue(bad.readEntity(String.class).contains("nonsense"));
    }

    /** Swagger UI is not localized, but it still has to render. */
    @Test
    public void testApiHtmlStillRenders() {
        assertTrue(html("/api.html", null).contains("<html lang=\"en\">"));
    }

}
