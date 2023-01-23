package fi.nls.hakunapi.core;

import fi.nls.hakunapi.core.filter.Filter;

public interface FilterParser {

    public String getCode();
    public Filter parse(FeatureType ft, String filter, int filterSrid) throws IllegalArgumentException;

}
