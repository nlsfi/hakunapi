package fi.nls.hakunapi.core.i18n;

import java.util.List;
import java.util.Locale;

/**
 * Language negotiation per OGC API Features Part 1 Core 7.10, plus an explicit
 * lang query parameter that takes precedence over Accept-Language.
 *
 * Available tags are expected in lower case, as HakunaConfigParser normalizes
 * them.
 */
public final class LangNegotiation {

    private LangNegotiation() {
    }

    /**
     * @param available in declared order, first is the default
     * @return the language to serve, or null if none are available
     */
    public static String resolve(List<String> available, String langParam, List<String> acceptLanguageTags) {
        if (available == null || available.isEmpty()) {
            return null;
        }

        if (langParam != null && !langParam.isBlank()) {
            String match = match(available, langParam);
            return match != null ? match : available.get(0);
        }

        if (acceptLanguageTags != null) {
            for (String tag : acceptLanguageTags) {
                String match = match(available, tag);
                if (match != null) {
                    return match;
                }
            }
        }

        return available.get(0);
    }

    /** @return the available language matching tag exactly or by primary subtag, or null */
    public static String match(List<String> available, String tag) {
        if (tag == null || tag.isBlank()) {
            return null;
        }

        String normalized = tag.trim().toLowerCase(Locale.ROOT);
        // Locale.ROOT round-trips to the wildcard "und", which asserts nothing
        if ("und".equals(normalized) || "*".equals(normalized)) {
            return null;
        }

        for (String lang : available) {
            if (lang.equals(normalized)) {
                return lang;
            }
        }

        int dash = normalized.indexOf('-');
        if (dash > 0) {
            String primary = normalized.substring(0, dash);
            for (String lang : available) {
                if (lang.equals(primary)) {
                    return lang;
                }
            }
        }

        return null;
    }

}
