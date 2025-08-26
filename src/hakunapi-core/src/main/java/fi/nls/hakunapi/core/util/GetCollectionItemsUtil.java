package fi.nls.hakunapi.core.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import fi.nls.hakunapi.core.NextCursor;
import fi.nls.hakunapi.core.param.CollectionsParam;
import fi.nls.hakunapi.core.request.GetFeatureCollection;
import fi.nls.hakunapi.core.schemas.Link;

public class GetCollectionItemsUtil {

    public static Link getNextLink(GetFeatureCollection c, NextCursor next, List<Link> links) {
        return getNextLink(c, next, links, null);
    }

    public static Link getNextLink(GetFeatureCollection c, NextCursor next, List<Link> links, List<GetFeatureCollection> remainingCollections) {
        next = Objects.requireNonNull(next);

        Link self = links.stream()
                .filter(l -> "self".equalsIgnoreCase(l.getRel()))
                .findAny()
                .orElse(null);
        if (self == null) {
            return null;
        }

        String href = self.getHref();
        int i = href.indexOf('?');
        String hrefWithoutQ = i < 0 ? href : href.substring(0, i);
        String q = i < 0 ? "" : href.substring(i + 1);
        Map<String, String> qp = new LinkedHashMap<>();
        for (String kvp : q.split("&")) {
            int j = kvp.indexOf('=');
            if (j < 0) {
                continue;
            }
            String k = kvp.substring(0, j);
            String v = kvp.substring(j + 1);
            qp.put(k, v);
        }

        if (remainingCollections != null) {
            // Adjust `collections` from self link
            String collections = remainingCollections.stream()
                    .map(col -> col.getFt().getName())
                    .collect(Collectors.joining(","));
            qp.put(CollectionsParam.PARAM_NAME, collections);
        }

        StringPair paramNameAndValue = c.getPaginationStrategy().getNextQueryParam(next);
        qp.put(paramNameAndValue.getLeft(), paramNameAndValue.getRight());
        return Links.getNextLink(hrefWithoutQ, qp, self.getType());
    }

}
