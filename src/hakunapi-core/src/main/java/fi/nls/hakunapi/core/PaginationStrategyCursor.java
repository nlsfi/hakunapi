package fi.nls.hakunapi.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.param.NextParam;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.request.GetFeatureCollection;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import fi.nls.hakunapi.core.util.StringPair;

public class PaginationStrategyCursor implements PaginationStrategy {

    private static final ObjectMapper om = new ObjectMapper();

    public static final PaginationStrategyCursor INSTANCE = new PaginationStrategyCursor();
    public static final String NAME = "cursor";

    private PaginationStrategyCursor() {
        // Use INSTANCE
    }

    @Override
    public void apply(GetFeatureRequest request, NextCursor cursor) {
        GetFeatureCollection c = request.getCollections().get(0);
        List<OrderBy> orderBy = c.getOrderBy();
        int n = orderBy.size();
        if (n == 1) {
            HakunaProperty prop = orderBy.get(0).getProperty();
            boolean ascending = orderBy.get(0).isAscending();
            String next = cursor.getNext();
            Filter f = ascending ? Filter.greaterThanOrEqualTo(prop, next) : Filter.lessThanThanOrEqualTo(prop, next);
            c.addFilter(f);
        } else {
            String json = cursor.getNext();
            List<String> values = fromJsonArray(json);
            List<Filter> subFilters = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                HakunaProperty prop = orderBy.get(i).getProperty();
                boolean ascending = orderBy.get(i).isAscending();
                String next = values.get(i);
                subFilters.add(ascending
                        ? Filter.greaterThanOrEqualTo(prop, next)
                        : Filter.lessThanThanOrEqualTo(prop, next));
            }
            c.addFilter(Filter.and(subFilters));
        }

        request.setFirstPage(false);
        request.setOffset(cursor.getOffset());
        request.getCollections().forEach(col -> col.setPaginationStrategy(this));
    }

    @Override
    public NextCursor getNextCursor(GetFeatureRequest request, GetFeatureCollection c, ValueProvider next) {
        List<OrderBy> orderBy = c.getOrderBy();
        int n = orderBy.size();
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
    public boolean isOffsetParamSupported() {
        return false;
    }

    @Override
    public StringPair getNextQueryParam(NextCursor next) {
        return new StringPair(NextParam.PARAM_NAME, next.toWireFormat());
    }

}
