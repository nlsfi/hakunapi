package fi.nls.hakunapi.core;

import java.util.HashMap;
import java.util.Map;

public enum ConformanceClass {
	
	// OGC API - Features - Part 1: Core
    Core("http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/core"),
    OpenAPI30("http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/oas30"),
    GeoJSON("http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/geojson"),
    
    HTML("http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/html"),
    GMLSF0("http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/gmlsf0"),
    GMLSF2("http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/gmlsf2"),
    GPKG("http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/gpkg"),
    DIRECT_TILE("http://www.opengis.net/spec/wfs/3.0/vt/req/core/direct-tile-access-path-api"),

    //OGC API - Features - Part 2: Coordinate Reference Systems by Reference
    CRS("http://www.opengis.net/spec/ogcapi-features-2/1.0/conf/crs"),

    //OGC API - Features - Part 3: Filtering 
    QUERYABLES("http://www.opengis.net/spec/ogcapi-features-3/1.0/conf/queryables"),
    QUERYABLES_QUERY_PARAMETERS("http://www.opengis.net/spec/ogcapi-features-3/1.0/conf/queryables-query-parameters"),
    FILTER("http://www.opengis.net/spec/ogcapi-features-3/1.0/conf/filter"),
    FEATURES_FILTER("http://www.opengis.net/spec/ogcapi-features-3/1.0/conf/features-filter"),
    
    // Common Query Language CQL 2
    CQL2_TEXT("http://www.opengis.net/spec/cql2/1.0/conf/cql2-text"),
    CQL2_JSON("http://www.opengis.net/spec/cql2/1.0/conf/cql2-json"),
    BASIC_CQL2("http://www.opengis.net/spec/cql2/1.0/conf/basic-cql2"),
    ADVANCED_COMPARISON_OPERATORS("http://www.opengis.net/spec/cql2/1.0/conf/advanced-comparison-operators"),
    CASE_INSENSITIVE_COMPARISON("http://www.opengis.net/spec/cql2/1.0/conf/case-insensitive-comparison"),
    ACCENT_INSENSITIVE_COMPARISON("http://www.opengis.net/spec/cql2/1.0/conf/accent-insensitive-comparison"),
    BASIC_SPATIAL_FUNCTIONS("http://www.opengis.net/spec/cql2/1.0/conf/basic-spatial-functions"),
    BAISC_SPATIAL_FUNCTIONS_PLUS("http://www.opengis.net/spec/cql2/1.0/conf/basic-spatial-functions-plus"),
    SPATIAL_FUNCTIONS("http://www.opengis.net/spec/cql2/1.0/conf/spatial-functions"),
    TEMPORAL_FUNCTIONS("http://www.opengis.net/spec/cql2/1.0/conf/temporal-functions"),
    ARRAY_FUNCTIONS("http://www.opengis.net/spec/cql2/1.0/conf/array-functions"),
    PROPERTY_PROPERTY("http://www.opengis.net/spec/cql2/1.0/conf/property-property"),
    FUNCTIONS("http://www.opengis.net/spec/cql2/1.0/conf/functions"),
    ARITHMETIC("http://www.opengis.net/spec/cql2/1.0/conf/arithmetic"),

    ;
	
	
    public final String uri;

    private ConformanceClass(String uri) {
        this.uri = uri;
    }

    public static ConformanceClass fromUri(String uri) {
        return fromUri.get(uri);
    }

    private final static Map<String, ConformanceClass> fromUri = new HashMap<>();
    static {
        for (ConformanceClass c : ConformanceClass.values()) {
            fromUri.put(c.uri, c);
        }
    }
}
