package fi.nls.hakunapi.tiles.generator;

import java.util.List;
import java.util.Map;

import fi.nls.hakunapi.core.filter.Filter;

/** Zoom bounds are inclusive; filters has no entry for a collection without a filter. */
public record ZoomRange(
        String name,
        int minZoom,
        int maxZoom,
        List<String> collectionIds,
        Map<String, Filter> filters) {

    public ZoomRange {
        collectionIds = List.copyOf(collectionIds);
        filters = Map.copyOf(filters);
    }

    public boolean covers(int zoom) {
        return zoom >= minZoom && zoom <= maxZoom;
    }

}
