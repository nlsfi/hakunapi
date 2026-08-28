package fi.nls.hakunapi.core.i18n;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class LocalizationTest {

    private static Localization l10n() {
        Map<String, Map<String, String>> catalogs = new LinkedHashMap<>();
        catalogs.put("fi", Map.of("api.title", "Otsikko"));
        // sv is declared with no catalog at all
        return new Localization(Arrays.asList("en", "fi", "sv"), catalogs, LocalizationTest::base);
    }

    private static String base(String key) {
        switch (key) {
        case "api.title":
            return "Base title";
        case "api.description":
            return "Base description";
        default:
            return null;
        }
    }

    @Test
    public void testDeclaredOrderIsPreservedAndFirstIsDefault() {
        Localization l = l10n();

        assertEquals(Arrays.asList("en", "fi", "sv"), l.getLanguages());
        assertEquals("en", l.getDefaultLanguage());
        assertFalse(l.isEmpty());
    }

    @Test
    public void testCatalogWins() {
        assertEquals("Otsikko", l10n().get("fi", "api.title"));
    }

    @Test
    public void testFallsBackToBasePerKey() {
        // fi has a catalog but no entry for this key
        assertEquals("Base description", l10n().get("fi", "api.description"));
    }

    /**
     * A declared language with no catalog is served entirely from the base
     * config. That is how the base representation's language is made explicit so
     * Content-Language and hreflang can be stated rather than guessed.
     */
    @Test
    public void testLanguageWithNoCatalogReadsTheBaseConfig() {
        Localization l = l10n();

        assertEquals("Base title", l.get("en", "api.title"));
        assertEquals("Base title", l.get("sv", "api.title"));
    }

    @Test
    public void testUnknownKeyIsAbsent() {
        assertNull(l10n().get("fi", "api.nonexistent"));
    }

    @Test
    public void testNullLangReadsTheBaseConfigOnly() {
        assertEquals("Base title", l10n().get(null, "api.title"));
    }

    @Test
    public void testUnknownLangReadsTheBaseConfigOnly() {
        assertEquals("Base title", l10n().get("de", "api.title"));
    }

    /**
     * The explicit-fallback overload is for callers that already hold the base
     * value, such as a title read off a FeatureType, and must not consult the
     * base config lookup.
     */
    @Test
    public void testExplicitFallbackOverloadIgnoresTheBaseConfig() {
        Localization l = l10n();

        assertEquals("Otsikko", l.get("fi", "api.title", "caller default"));
        assertEquals("caller default", l.get("fi", "api.description", "caller default"));
        assertNull(l.get("fi", "api.description", null));
        assertEquals("caller default", l.get(null, "api.title", "caller default"));
    }

    @Test
    public void testHasExplicitDistinguishesCatalogFromFallback() {
        Localization l = l10n();

        assertTrue(l.hasExplicit("fi", "api.title"));
        assertFalse(l.hasExplicit("fi", "api.description"));
        assertFalse(l.hasExplicit("en", "api.title"));
        assertFalse(l.hasExplicit(null, "api.title"));
        assertFalse(l.hasExplicit("de", "api.title"));
    }

    @Test
    public void testEmpty() {
        assertTrue(Localization.EMPTY.isEmpty());
        assertEquals(Collections.emptyList(), Localization.EMPTY.getLanguages());
        assertNull(Localization.EMPTY.getDefaultLanguage());
        assertNull(Localization.EMPTY.get("fi", "api.title"));
    }

    /**
     * Defensive copies: a Localization handed to a running service must not be
     * mutable through the maps the caller kept.
     */
    @Test
    public void testDefensivelyCopiesItsInputs() {
        List<String> langs = new java.util.ArrayList<>(List.of("fi"));
        Map<String, String> catalog = new LinkedHashMap<>(Map.of("api.title", "Otsikko"));
        Map<String, Map<String, String>> catalogs = new LinkedHashMap<>();
        catalogs.put("fi", catalog);

        Localization l = new Localization(langs, catalogs, LocalizationTest::base);

        langs.add("sv");
        catalog.put("api.title", "Muutettu");
        catalogs.remove("fi");

        assertEquals(Arrays.asList("fi"), l.getLanguages());
        assertEquals("Otsikko", l.get("fi", "api.title"));

        try {
            l.getLanguages().add("de");
            fail("Expected the returned language list to be immutable");
        } catch (UnsupportedOperationException expected) {
            // as intended
        }
    }

}
