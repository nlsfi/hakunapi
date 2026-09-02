package fi.nls.hakunapi.core.i18n;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LocalizableKeysTest {

    /**
     * The key builders and the whitelist are separate lists of the same keys,
     * and nothing but this test ties them together. A builder for a key the
     * whitelist rejects would fail only at runtime, by silently serving the
     * untranslated fallback.
     */
    @Test
    public void testEveryBuiltKeyIsLocalizable() {
        assertTrue(LocalizableKeys.isLocalizable(LocalizableKeys.apiTitle()));
        assertTrue(LocalizableKeys.isLocalizable(LocalizableKeys.apiDescription()));
        assertTrue(LocalizableKeys.isLocalizable(LocalizableKeys.collectionTitle("mun")));
        assertTrue(LocalizableKeys.isLocalizable(LocalizableKeys.collectionDescription("mun")));
    }

    @Test
    public void testLinkTitlesAreNotLocalizable() {
        assertFalse(LocalizableKeys.isLocalizable("api.links.metadata.title"));
        assertFalse(LocalizableKeys.isLocalizable("collections.mun.links.legend.title"));
        assertFalse(LocalizableKeys.isLocalizable("default.collections.links.legend.title"));
    }

    @Test
    public void testUnrelatedConfigIsRejected() {
        assertFalse(LocalizableKeys.isLocalizable("db.url"));
        assertFalse(LocalizableKeys.isLocalizable("getfeatures.limit.max"));
        assertFalse(LocalizableKeys.isLocalizable("api.version"));
        assertFalse(LocalizableKeys.isLocalizable(null));
        assertFalse(LocalizableKeys.isLocalizable(""));
    }

    /**
     * A placeholder stands for exactly one non-empty segment, and the key must
     * match wholly: neither a dotted nor an empty collection id, nor a prefix or
     * suffix of a whitelisted key, is localizable.
     */
    @Test
    public void testKeyMustMatchWhollyWithOneNonEmptySegmentPerPlaceholder() {
        assertFalse(LocalizableKeys.isLocalizable("collections.a.b.title"));
        assertFalse(LocalizableKeys.isLocalizable("collections..title"));
        assertFalse(LocalizableKeys.isLocalizable("collections.mun.title.extra"));
        assertFalse(LocalizableKeys.isLocalizable("xapi.title"));
    }
}
