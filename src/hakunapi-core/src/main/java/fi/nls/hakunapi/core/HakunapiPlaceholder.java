package fi.nls.hakunapi.core;

import java.util.ArrayList;
import java.util.List;

public class HakunapiPlaceholder {

    private static final char DYNAMIC_VARIABLE_INIT = '$';
    private static final char DYNAMIC_VARIABLE_BEGIN = '{';
    private static final char DYNAMIC_VARIABLE_END = '}';

    private HakunapiPlaceholder() {
        // Block init
    }

    public static List<PlaceholderSegment> parseSegments(String pattern) {
        List<PlaceholderSegment> segments = new ArrayList<>();

        for (int i = 0; i < pattern.length();) {
            int[] next = getNextDynamicSegment(pattern, i);
            if (next == null) {
                // Rest of the pattern as-is
                String s = pattern.substring(i);
                if (!s.isEmpty()) {
                    segments.add(new PlaceholderSegment(s, false));
                }
                break;
            }
            if (next[0] != i) {
                String s = pattern.substring(i, next[0]);
                segments.add(new PlaceholderSegment(s, false));
            }
            String key = pattern.substring(next[0] + 2, next[1]);
            segments.add(new PlaceholderSegment(key, true));
            i = next[1] + 1;
        }

        return segments;
    }

    private static int[] getNextDynamicSegment(String pattern, int fromIndex) {
        int i = pattern.indexOf(DYNAMIC_VARIABLE_INIT, fromIndex);
        if (i < 0) {
            return null;
        }
        if (pattern.charAt(i + 1) != DYNAMIC_VARIABLE_BEGIN) {
            return null;
        }
        int j = pattern.indexOf(DYNAMIC_VARIABLE_END, i + 2);
        if (j < 0 || j == i + 2) {
            return null;
        }
        return new int[] { i, j };
    }

    public static final class PlaceholderSegment {

        private final String value;
        private final boolean placeholder;

        private PlaceholderSegment(String value, boolean placeholder) {
            this.value = value;
            this.placeholder = placeholder;
        }

        public String value() {
            return value;
        }

        public boolean isPlaceholder() {
            return placeholder;
        }

    }

}
