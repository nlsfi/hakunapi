package fi.nls.hakunapi.core.util;

import java.util.List;

import fi.nls.hakunapi.core.filter.Filter;

public class FilterUtil {

    public static Filter findFilterByTag(List<Filter> filters, String tag) {
        if (filters != null) {
            for (Filter f : filters) {
                if (tag.equals(f.getTag())) {
                    return f;
                }
            }
        }
        return null;
    }

    public static int indexOfFilterByTag(List<Filter> filters, String tag) {
        if (filters != null) {
            for (int i = 0; i < filters.size(); i++) {
                Filter f = filters.get(i);
                if (tag.equals(f.getTag())) {
                    return i;
                }
            }
        }
        return -1;
    }

}
