package fi.nls.hakunapi.core.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import fi.nls.hakunapi.core.param.LangParam;
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

    /**
     * @param lang emitted both as the lang query parameter and as hreflang
     */
    public static Link getAlternateLangLink(String path, Map<String, String> queryParams, String mimeType,
            String lang) {
        Map<String, String> withLang = new LinkedHashMap<>();
        if (queryParams != null) {
            withLang.putAll(queryParams);
        }
        withLang.put(LangParam.PARAM_NAME, lang);
        String href = addQueryParams(path, withLang);
        return new Link(href, "alternate", mimeType, "This document in " + lang, lang);
    }

    /**
     * One rel=alternate hreflang link per language other than lang, so a client
     * can find the other representations of this resource.
     *
     * @param available every language this resource can be served in, lang included
     * @return empty if there is nothing to switch to
     */
    public static List<Link> getAlternateLangLinks(String path, Map<String, String> queryParams, String mimeType,
            List<String> available, String lang) {
        if (lang == null || available.size() < 2) {
            return Collections.emptyList();
        }

        Map<String, String> params = new LinkedHashMap<>(queryParams);
        params.remove(LangParam.PARAM_NAME);
        List<Link> links = new ArrayList<>(available.size() - 1);
        for (String other : available) {
            if (!other.equals(lang)) {
                links.add(getAlternateLangLink(path, params, mimeType, other));
            }
        }
        return links;
    }

    /**
     * Populating hreflang on language-dependent links is what lets a client tell
     * which representation it got (OGC API Features Part 1 Core 7.10).
     */
    public static Link getSelfLink(String path, Map<String, String> queryParams, String mimeType, String lang) {
        String href = addQueryParams(path, queryParams);
        return new Link(href, "self", mimeType, "This document", lang);
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

    public static StringPair toLinkHeader(List<Link> links) {
        String key = "Link";
        String value = links.stream().map(Link::toLinkHeader).collect(Collectors.joining(", "));
        return new StringPair(key, value);
    }

}
