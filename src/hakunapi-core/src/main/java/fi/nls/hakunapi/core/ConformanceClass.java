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
    
    TEMPORAL_OPERATORS("http://www.opengis.net/spec/ogcapi-features-3/1.0/conf/temporal-operators"),
    ARRAY_OPERATORS("http://www.opengis.net/spec/ogcapi-features-3/1.0/conf/array-operators"),
    PROPERTY_PROPERTY_COMPARISONS("http://www.opengis.net/spec/ogcapi-features-3/1.0/conf/property-property"),
    CUSTOM_FUNCTIONS("http://www.opengis.net/spec/ogcapi-features-3/1.0/conf/functions"),
    ARITHMETIC_EXPRESSIONS("http://www.opengis.net/spec/ogcapi-features-3/1.0/conf/arithmetic"),
    
    // Common Query Language CQL 2
    BASIC_CQL("http://www.opengis.net/spec/cql2/1.0/conf/basic-cql2"),
    ADVANCED_COMPARISON_OPERATORS("http://www.opengis.net/spec/cql2/1.0/conf/advanced-comparison-operators"),
    BASIC_SPATIAL_OPERATORS("http://www.opengis.net/spec/cql2/1.0/conf/basic-spatial-operators"),
    SPATIAL_OPERATORS("http://www.opengis.net/spec/cql2/1.0/conf/spatial-operators"),
    FUNCTIONS("http://www.opengis.net/spec/cql2/1.0/conf/functions"),
    CQL2_TEXT_ENCODING("http://www.opengis.net/spec/cql2/1.0/conf/cql2-text"),
    CASE_INSENSITIVE_COMPARISON("http://www.opengis.net/spec/cql2/1.0/conf/case-insensitive-comparison"),

 // ? https://confluence.nls.fi/pages/viewpage.action?pageId=117770040 punaisella conflussa?
    CQL2_JSON_ENCODING("http://www.opengis.net/spec/ogcapi-features-3/1.0/conf/cql2-json"),
    
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
