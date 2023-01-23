package fi.nls.hakunapi.core.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import fi.nls.hakunapi.core.schemas.Link;

public class Links {
    
    public static String getItemsPath(String currentServerURL, String collectionId) {
        return currentServerURL + "/collections/" + collectionId + "/items";
    }
    
    public static Link getSelfLink(String path, Map<String, String> queryParams, String mimeType) {
        String href = addQueryParams(path, queryParams);
        String rel = "self";
        return new Link(href.toString(), rel, mimeType, "This document");
    }
    
    public static Link getNextLink(String path, Map<String, String> queryParams, String mimeType) {
        String href = addQueryParams(path, queryParams);
        String rel = "next";
        return new Link(href, rel, mimeType, "Next page");
    }

    public static Link getCollectionLink(String path, Map<String, String> queryParams, String mimeType) {
        String href = addQueryParams(path, queryParams);
        String rel = "collection";
        return new Link(href, rel, mimeType, "The collection document");
    }
    
    public static Link getAlternateLink(String path, Map<String, String> queryParams, String mimeType, String mimeTypeHuman) {
        String href = addQueryParams(path, queryParams);
        String rel = "alternate";
        return new Link(href, rel, mimeType, "This document as " + mimeTypeHuman);
    }

    private static String addQueryParams(String path, Map<String, String> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return path;
        }

        StringBuilder sb = new StringBuilder(path);

        boolean first = true;
        for (Map.Entry<String, String> kvp : queryParams.entrySet()) {
            String key = kvp.getKey();
            String value = kvp.getValue();
            if (value == null || value.isEmpty()) {
                continue;
            }

            if (first) {
                sb.append('?');
                first = false;
            } else {
                sb.append('&');
            }
            
            sb.append(key).append('=').append(urlEncode(value));
        }
        
        return sb.toString();
    }

    private static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException ignore) {
            // Ignore the exception, 'UTF-8' is supported
        }
        // return something, this code is unreachable
        return s;
    }

}
