package fi.nls.hakunapi.core.i18n;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class LangNegotiationTest {

    private static final List<String> AVAILABLE = Arrays.asList("en", "fi", "sv");

    @Test
    public void testNoLanguagesAvailable() {
        assertNull(LangNegotiation.resolve(Collections.emptyList(), "fi", null));
        assertNull(LangNegotiation.resolve(null, "fi", null));
    }


    @Test
    public void testLangParamIsCaseInsensitive() {
        assertEquals("fi", LangNegotiation.resolve(AVAILABLE, "FI", null));
        assertEquals("fi", LangNegotiation.resolve(AVAILABLE, "Fi-FI", null));
    }


    @Test
    public void testLangParamBeatsAcceptLanguage() {
        assertEquals("sv", LangNegotiation.resolve(AVAILABLE, "sv", Arrays.asList("fi", "en")));
    }

    /**
     * Behaviour change vs. the previous GetCollectionSchemaImpl.resolveLang:
     * an unmatched lang param used to yield null (falling through to the
     * non-localized defaults), whereas an unmatched Accept-Language fell back to
     * the first configured language. Both now fall back to first-declared.
     */
    @Test
    public void testUnmatchedLangParamFallsBackToFirstDeclared() {
        assertEquals("en", LangNegotiation.resolve(AVAILABLE, "de", null));
        assertEquals("en", LangNegotiation.resolve(AVAILABLE, "zz-ZZ", null));
    }

    @Test
    public void testBlankLangParamIsIgnored() {
        assertEquals("fi", LangNegotiation.resolve(AVAILABLE, "  ", Arrays.asList("fi")));
        assertEquals("fi", LangNegotiation.resolve(AVAILABLE, null, Arrays.asList("fi")));
    }

    @Test
    public void testAcceptLanguageInPreferenceOrder() {
        assertEquals("sv", LangNegotiation.resolve(AVAILABLE, null, Arrays.asList("de", "sv", "fi")));
    }

    @Test
    public void testAcceptLanguagePrimarySubtagFallback() {
        assertEquals("sv", LangNegotiation.resolve(AVAILABLE, null, Arrays.asList("sv-FI")));
    }

    @Test
    public void testUnmatchedAcceptLanguageFallsBackToFirstDeclared() {
        assertEquals("en", LangNegotiation.resolve(AVAILABLE, null, Arrays.asList("de", "fr")));
        assertEquals("en", LangNegotiation.resolve(AVAILABLE, null, Collections.emptyList()));
    }

    /**
     * JAX-RS maps a wildcard Accept-Language to Locale.ROOT, whose language tag
     * is "und". It expresses no preference and must not match anything.
     */
    @Test
    public void testWildcardAcceptLanguageMatchesNothing() {
        assertNull(LangNegotiation.match(AVAILABLE, "und"));
        assertNull(LangNegotiation.match(AVAILABLE, "*"));
        assertEquals("en", LangNegotiation.resolve(AVAILABLE, null, Arrays.asList("und")));
    }


}
