package fi.nls.hakunapi.core;

import java.util.List;

import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.request.GetFeatureRequest;

public interface PaginationStrategy {

    public void apply(GetFeatureRequest request, NextCursor cursor);
    public boolean shouldOffset();
    public boolean shouldSortBy();
    public List<HakunaProperty> getProperties();
    public List<Boolean> getAscending();
    public int getMaxGroupSize();
    public NextCursor getNextCursor(int offset, int limit, ValueProvider last);

}
