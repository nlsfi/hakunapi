package fi.nls.hakunapi.filter;

import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.ecql.ECQL;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.FilterParser;
import fi.nls.hakunapi.core.filter.Filter;

public class CQLText implements FilterParser {

    public static final CQLText INSTANCE = new CQLText();

    private CQLText() {
        // Use INSTANCE;
    }

    @Override
    public String getCode() {
        return "cql-text";
    }

    @Override
    public Filter parse(FeatureType ft, String filter, int filterSrid) throws IllegalArgumentException {
        try {
            org.opengis.filter.Filter cql = ECQL.toFilter(filter);
            return new GeoToolsFilter2HakunaFilter(ft, filterSrid).map(cql);
        } catch (CQLException e) {
            throw new IllegalArgumentException("Unexpected CQL expression");
        }
    }

}
