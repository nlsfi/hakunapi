package fi.nls.hakunapi.core;

import fi.nls.hakunapi.core.param.OffsetParam;
import fi.nls.hakunapi.core.request.GetFeatureCollection;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import fi.nls.hakunapi.core.util.StringPair;

public class PaginationStrategyOffset implements PaginationStrategy {

    public static final PaginationStrategyOffset INSTANCE = new PaginationStrategyOffset();
    public static final String NAME = "offset";

    private PaginationStrategyOffset() {
        // Use INSTANCE
    }

    @Override
    public void apply(GetFeatureRequest request, int offset) {
        request.setFirstPage(offset == 0);
        request.setOffset(offset);
        request.getCollections().forEach(c -> c.setPaginationStrategy(this));
    }

    @Override
    public NextCursor getNextCursor(GetFeatureRequest request, GetFeatureCollection col, ValueProvider last) {
        return new NextCursor("", request.getOffset() + request.getLimit());
    }

    @Override
    public StringPair getNextQueryParam(NextCursor next) {
        return new StringPair(OffsetParam.PARAM_NAME, Integer.toString(next.getOffset()));
    }

    @Override
    public boolean isOffsetParamSupported() {
        return true;
    }

}
