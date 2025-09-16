package fi.nls.hakunapi.core;

import fi.nls.hakunapi.core.request.GetFeatureCollection;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import fi.nls.hakunapi.core.util.StringPair;

public interface PaginationStrategy {

    public default void apply(GetFeatureRequest request, NextCursor cursor) {
        throw new UnsupportedOperationException();
    }

    public default void apply(GetFeatureRequest request, int offset) {
        throw new UnsupportedOperationException();
    }

    public NextCursor getNextCursor(GetFeatureRequest request, GetFeatureCollection col, ValueProvider last);
    public StringPair getNextQueryParam(NextCursor next);

    public boolean isOffsetParamSupported();

}
