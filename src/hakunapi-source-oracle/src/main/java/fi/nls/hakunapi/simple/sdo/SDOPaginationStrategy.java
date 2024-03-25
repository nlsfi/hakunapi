package fi.nls.hakunapi.simple.sdo;

import java.util.Collections;
import java.util.List;

import fi.nls.hakunapi.core.NextCursor;
import fi.nls.hakunapi.core.PaginationStrategy;
import fi.nls.hakunapi.core.ValueProvider;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.request.GetFeatureRequest;

public class SDOPaginationStrategy implements PaginationStrategy {
    public static final SDOPaginationStrategy INSTANCE = new SDOPaginationStrategy();

    private SDOPaginationStrategy() {
        // Use INSTANCE
    }

    @Override
    public void apply(GetFeatureRequest request, NextCursor cursor) {
        request.setFirstPage(cursor.getOffset() == 0);
        request.setOffset(cursor.getOffset());
    }

    @Override
    public List<HakunaProperty> getProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<Boolean> getAscending() {
        return Collections.emptyList();
    }

    @Override
    public int getMaxGroupSize() {
        return 1;
    }

    @Override
    public NextCursor getNextCursor(int offset, int limit, ValueProvider last) {
        return new NextCursor("", offset + limit);
    }

    @Override
    public boolean shouldOffset() {
        return true;
    }

    @Override
    public boolean shouldSortBy() {
        return true;
    }
}
