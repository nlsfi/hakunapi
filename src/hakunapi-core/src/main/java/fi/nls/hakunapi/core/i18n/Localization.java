package fi.nls.hakunapi.core.i18n;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Localized values for descriptive elements, keyed by language and config key.
 *
 * Resolution is per key, so catalogs may be arbitrarily sparse -- a catalog
 * holds only what differs from the base config. A language declared with no
 * catalog is served from the base config, which is how the base
 * representation's language is stated.
 *
 * Order comes from the configured list, not from map iteration, because
 * java.util.Properties is unordered. The first language is the default.
 */
public class Localization {

    public static final Localization EMPTY = new Localization(Collections.emptyList(),
            Collections.emptyMap(), key -> null);

    private final List<String> languages;
    private final Map<String, Map<String, String>> catalogs;
    private final Function<String, String> base;

    /**
     * @param catalogs lang to (config key to localized value); a language may be absent
     * @param base lookup into the base config, used when a catalog has no value
     */
    public Localization(List<String> languages, Map<String, Map<String, String>> catalogs,
            Function<String, String> base) {
        this.languages = List.copyOf(languages);
        Map<String, Map<String, String>> copy = new LinkedHashMap<>();
        catalogs.forEach((lang, catalog) -> copy.put(lang, Map.copyOf(catalog)));
        this.catalogs = Collections.unmodifiableMap(copy);
        this.base = base;
    }

    public List<String> getLanguages() {
        return languages;
    }

    /** @return the default language, or null if none are configured */
    public String getDefaultLanguage() {
        return languages.isEmpty() ? null : languages.get(0);
    }

    public boolean isEmpty() {
        return languages.isEmpty();
    }

    /**
     * Resolves one key: catalog, then base config, then null.
     *
     * @param lang may be null to read the base config only
     */
    public String get(String lang, String key) {
        if (lang != null) {
            Map<String, String> catalog = catalogs.get(lang);
            if (catalog != null) {
                String value = catalog.get(key);
                if (value != null) {
                    return value;
                }
            }
        }
        return base.apply(key);
    }

    /**
     * Resolves one key, falling back to an explicit default rather than the base
     * config lookup, for callers already holding the base value.
     */
    public String get(String lang, String key, String fallback) {
        if (lang != null) {
            Map<String, String> catalog = catalogs.get(lang);
            if (catalog != null) {
                String value = catalog.get(key);
                if (value != null) {
                    return value;
                }
            }
        }
        return fallback;
    }

    /** @return true if a catalog for lang holds an explicit value for key */
    public boolean hasExplicit(String lang, String key) {
        Map<String, String> catalog = lang == null ? null : catalogs.get(lang);
        return catalog != null && catalog.containsKey(key);
    }

}
