package fi.nls.hakunapi.core;

import java.util.List;
import java.util.stream.Collectors;

import fi.nls.hakunapi.core.HakunapiPlaceholder.PlaceholderSegment;

public final class CurrentServerUrlProviders {

    private CurrentServerUrlProviders() {
        // Block init
    }

    public static CurrentServerUrlProvider from(String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            throw new IllegalArgumentException("null or empty pattern form current server url!");
        }

        List<CurrentServerUrlProvider> segments = HakunapiPlaceholder.parseSegments(pattern).stream()
                .map(CurrentServerUrlProviders::mapSegment)
                .collect(Collectors.toList());

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

    private static CurrentServerUrlProvider mapSegment(PlaceholderSegment s) {
        return s.isPlaceholder() ? values -> values.apply(s.value()) : __ -> s.value();
    }

}
