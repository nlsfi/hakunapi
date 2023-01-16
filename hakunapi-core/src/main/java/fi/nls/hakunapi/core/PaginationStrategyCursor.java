package fi.nls.hakunapi.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.request.GetFeatureCollection;
import fi.nls.hakunapi.core.request.GetFeatureRequest;

public class PaginationStrategyCursor implements PaginationStrategy {

    private static final ObjectMapper om = new ObjectMapper();

    private final List<HakunaProperty> properties;
    private final List<Boolean> ascending;

    public PaginationStrategyCursor(List<HakunaProperty> properties, List<Boolean> ascending) {
        this.properties = properties;
        this.ascending = ascending;
    }

    @Override
    public List<HakunaProperty> getProperties() {
        return properties;
    }

    @Override
    public List<Boolean> getAscending() {
        return ascending;
    }

    @Override
    public int getMaxGroupSize() {
        return 1;
    }

    @Override
    public void apply(GetFeatureRequest request, NextCursor cursor) {
        GetFeatureCollection c = request.getCollections().get(0);
        int n = properties.size();
        if (n == 1) {
            HakunaProperty prop = getProperties().get(0);
            boolean ascending = getAscending().get(0);
            String next = cursor.getNext();
            Filter f = ascending ? Filter.greaterThanOrEqualTo(prop, next) : Filter.lessThanThanOrEqualTo(prop, next);
            c.addFilter(f);
        } else {
            String json = cursor.getNext();
            List<String> values = fromJsonArray(json);
            List<Filter> subFilters = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                HakunaProperty prop = getProperties().get(i);
                boolean ascending = getAscending().get(i);
                String next = values.get(i);
                subFilters.add(ascending
                        ? Filter.greaterThanOrEqualTo(prop, next)
                        : Filter.lessThanThanOrEqualTo(prop, next));
            }
            c.addFilter(Filter.and(subFilters));
        }
        request.setFirstPage(false);
        request.setOffset(cursor.getOffset());
    }

    @Override
    public NextCursor getNextCursor(int offset, int limit, ValueProvider next) {
        int n = properties.size();
        if (n == 1) {
            return new NextCursor(next.getObject(0).toString(), 0);
        } else {
            List<String> values = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                values.add(next.getObject(i).toString());
            }
            String json = toJsonArray(values);
            return new NextCursor(json, 0);
        }
    }

    private static String toJsonArray(List<String> list) {
        try {
            return om.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<String> fromJsonArray(String json) {
        try {
            return om.readValue(json, om.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean shouldOffset() {
        return false;
    }

    @Override
    public boolean shouldSortBy() {
        return true;
    }

}
