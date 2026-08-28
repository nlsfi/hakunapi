package fi.nls.hakunapi.core.i18n;

import java.util.List;
import java.util.regex.Pattern;

/**
 * The whitelist of config keys a localization catalog may override.
 *
 * A catalog shares the main config keyspace, so without a whitelist it could
 * override unrelated configuration such as db.url. Keys outside the whitelist
 * are rejected at startup rather than ignored.
 *
 * Adding a key is backwards compatible, removing one is not.
 */
public final class LocalizableKeys {

    /**
     * The whitelist, in the readable form used in error messages. A
     * placeholder in angle brackets stands for one config name segment.
     */
    private static final List<String> KEYS = List.of(
            // service metadata
            "api.title",
            "api.description",
            // collection metadata
            "collections.<id>.title",
            "collections.<id>.description");

    private static final List<Pattern> PATTERNS = KEYS.stream()
            .map(LocalizableKeys::toPattern)
            .toList();

    private LocalizableKeys() {
    }

    /** Quotes the literal segments and turns each placeholder into [^.]+ */
    private static Pattern toPattern(String key) {
        StringBuilder regex = new StringBuilder(key.length() + 16);
        for (String segment : key.split("\\.", -1)) {
            if (regex.length() > 0) {
                regex.append("\\.");
            }
            if (segment.startsWith("<") && segment.endsWith(">")) {
                regex.append("[^.]+");
            } else {
                regex.append(Pattern.quote(segment));
            }
        }
        return Pattern.compile(regex.toString());
    }

    public static boolean isLocalizable(String key) {
        if (key == null) {
            return false;
        }
        for (Pattern p : PATTERNS) {
            if (p.matcher(key).matches()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the whitelist as a human readable list, for error messages
     */
    public static String describe() {
        return String.join(", ", KEYS);
    }

    /* Key builders */

    public static String apiTitle() {
        return "api.title";
    }

    public static String apiDescription() {
        return "api.description";
    }

    public static String collectionTitle(String collectionId) {
        return "collections." + collectionId + ".title";
    }

    public static String collectionDescription(String collectionId) {
        return "collections." + collectionId + ".description";
    }

}
