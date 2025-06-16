package fi.nls.hakunapi.core;

import fi.nls.hakunapi.core.request.GetFeatureCollection;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import fi.nls.hakunapi.core.util.StringPair;

public class PaginationStrategyHybrid implements PaginationStrategy {

    public static final PaginationStrategyHybrid INSTANCE = new PaginationStrategyHybrid();
    public static final String NAME = "hybrid";

    private PaginationStrategyHybrid() {
        // Use INSTANCE
    }

    @Override
    public void apply(GetFeatureRequest request, NextCursor cursor) {
        PaginationStrategyCursor.INSTANCE.apply(request, cursor);
    }

    @Override
    public void apply(GetFeatureRequest request, int offset) {
        PaginationStrategyOffset.INSTANCE.apply(request, offset);
    }

    @Override
    public NextCursor getNextCursor(GetFeatureRequest request, GetFeatureCollection col, ValueProvider last) {
        return PaginationStrategyCursor.INSTANCE.getNextCursor(request, col, last);
    }

    @Override
    public StringPair getNextQueryParam(NextCursor next) {
        return PaginationStrategyCursor.INSTANCE.getNextQueryParam(next);
    }

    @Override
    public boolean isOffsetParamSupported() {
        return true;
    }
}
