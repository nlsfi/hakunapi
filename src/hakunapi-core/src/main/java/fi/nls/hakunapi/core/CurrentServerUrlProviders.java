package fi.nls.hakunapi.core;

import java.util.ArrayList;
import java.util.List;

public final class CurrentServerUrlProviders {

    private static final char DYNAMIC_VARIABLE_INIT = '$';
    private static final char DYNAMIC_VARIABLE_BEGIN = '{';
    private static final char DYNAMIC_VARIABLE_END = '}';

    private CurrentServerUrlProviders() {
        // Block init
    }

    public static CurrentServerUrlProvider from(String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            throw new IllegalArgumentException("null or empty pattern form current server url!");
        }

        List<CurrentServerUrlProvider> segments = new ArrayList<>();
        for (int i = 0; i < pattern.length();) {
            int[] next = getNextDynamicSegment(pattern, i);
            if (next == null) {
                // Rest of the pattern as-is
                String s = pattern.substring(i);
                if (!s.isEmpty()) {
                    segments.add(__ -> s);
                }
                break;
            }
            if (next[0] != i) {
                String s = pattern.substring(i, next[0]);
                segments.add(__ -> s);
            }
            String key = pattern.substring(next[0] + 2, next[1]);
            segments.add(values -> values.apply(key));
            i = next[1] + 1;
        }

        if (segments.size() < 2) {
            return segments.get(0);
        }

        return values -> {
            // Stream#reduce would require separate combiner function, for-loop for the win
            StringBuilder sb = new StringBuilder();
            for (CurrentServerUrlProvider segment : segments) {
                sb.append(segment.get(values));
            }
            return sb.toString();
        };
    }

    static int[] getNextDynamicSegment(String pattern, int fromIndex) {
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

}
