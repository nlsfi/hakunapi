package fi.nls.hakunapi.core.request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.nls.hakunapi.core.OutputFormat;
import fi.nls.hakunapi.core.schemas.Crs;

public class GetFeatureRequest {

    private OutputFormat format;
    private List<GetFeatureCollection> collections;
    private boolean isFirstPage = true;
    private int offset;
    private int limit;
    private int srid = Crs.CRS84_SRID;
    private int filterSrid = Crs.CRS84_SRID;
    private Map<String, String> pathParams;
    private Map<String, String> queryParams;
    private Map<String, String> responseHeaders;

    public OutputFormat getFormat() {
        return format;
    }

    public void setFormat(OutputFormat format) {
        this.format = format;
    }

    public List<GetFeatureCollection> getCollections() {
        return collections == null ? Collections.emptyList() : collections;
    }

    public void addCollection(GetFeatureCollection collection) {
        if (collections == null) {
            collections = new ArrayList<>();
        }
        collections.add(collection);
    }

    public boolean isFirstPage() {
        return isFirstPage;
    }

    public void setFirstPage(boolean isFirstPage) {
        this.isFirstPage = isFirstPage;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getOffset() {
        return offset;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getSRID() {
        return srid;
    }

    public void setSRID(int srid) {
        this.srid = srid;
    }

    public int getFilterSrid() {
        return filterSrid;
    }

    public void setFilterSrid(int srid) {
        this.filterSrid = srid;
    }

    public Map<String, String> getPathParams() {
        return pathParams == null ? new HashMap<>() : pathParams;
    }

    public String getPathParam(String key) {
        return pathParams == null ? null : pathParams.get(key);
    }

    public void addPathParam(String key, String value) {
        if (pathParams == null) {
            pathParams = new HashMap<>();
        }
        pathParams.put(key, value);
    }

    public Map<String, String> getQueryParams() {
        return queryParams == null ? new HashMap<>() : queryParams;
    }

    public String getQueryParam(String key) {
        return queryParams == null ? null : queryParams.get(key);
    }

    public void addQueryParam(String key, String value) {
        if (queryParams == null) {
            queryParams = new HashMap<>();
        }
        queryParams.put(key, value);
    }

    public Map<String, String> getResponseHeaders() {
        return responseHeaders == null ? new HashMap<>() : responseHeaders;
    }

    public String getResponseHeader(String key) {
        return responseHeaders == null ? null : responseHeaders.get(key);
    }

    public void addResponseHeader(String key, String value) {
        if (responseHeaders == null) {
            responseHeaders = new HashMap<>();
        }
        responseHeaders.put(key, value);
    }

}
