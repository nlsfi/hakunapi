package fi.nls.hakunapi.gpkg;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.nls.hakunapi.core.FeatureCollectionWriter;
import fi.nls.hakunapi.core.FeatureStream;
import fi.nls.hakunapi.core.NextCursor;
import fi.nls.hakunapi.core.OutputFormat;
import fi.nls.hakunapi.core.SingleFeatureWriter;
import fi.nls.hakunapi.core.param.LimitParam;
import fi.nls.hakunapi.core.request.GetFeatureCollection;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import fi.nls.hakunapi.core.schemas.Link;
import fi.nls.hakunapi.core.util.GetCollectionItemsUtil;
import fi.nls.hakunapi.core.util.Links;
import fi.nls.hakunapi.core.util.StringPair;

public class OutputFormatGPKG implements OutputFormat {

    public static final String MEDIA_TYPE = "application/geopackage+vnd.sqlite3";
    public static final String ID = "gpkg";

    private final File dir;

    public OutputFormatGPKG(File dir) {
        this.dir = dir;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getMimeType() {
        return MEDIA_TYPE;
    }

    @Override
    public FeatureCollectionWriter getFeatureCollectionWriter() {
        return new GPKGFeatureCollectionWriter(dir);
    }

    @Override
    public SingleFeatureWriter getSingleFeatureWriter() {
        return new GPKGSingleFeatureWriter(dir);
    }

    @Override
    public Map<String, String> getResponseHeaders(GetFeatureRequest request, List<Link> links) {
        String filename;
        if (request.getCollections().size() == 1) {
            filename = request.getCollections().get(0).getName() + ".gpkg";
        } else {
            filename = "items.gpkg";
        }

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", getMimeType());
        headers.put("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        addLinkAndNumberReturned(request, links, headers);
        return headers;
    }

    private void addLinkAndNumberReturned(GetFeatureRequest request, List<Link> links, Map<String, String> headers) {
        if (request.getLimit() == LimitParam.UNLIMITED) {
            // No next param available, just set NumberReturned (and NumberMatched)
            String numberMatched = Integer.toString(getFullCount(request));
            headers.put("OGC-NumberReturned", numberMatched);
            headers.put("OGC-NumberMatched", numberMatched);
            return;
        }

        int numberReturned = addNextLink(request, links, headers);
        headers.put("OGC-NumberReturned", Integer.toString(numberReturned));
    }

    private int getFullCount(GetFeatureRequest request) {
        return request.getCollections().stream()
                .mapToInt(c -> {
                    try {
                        return c.getFt().getFeatureProducer().getNumberMatched(request, c);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .sum();
    }

    private int addNextLink(GetFeatureRequest request, List<Link> links, Map<String, String> headers) {
        int numberSkipped = 0;
        int limit = request.getLimit();
        for (GetFeatureCollection c : request.getCollections()) {
            int numberMatched;
            try {
                // TODO: Optimization available: Make numberMatched variant that takes an upper limit
                numberMatched = c.getFt().getFeatureProducer().getNumberMatched(request, c);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            int n = numberSkipped + numberMatched;
            if (n < limit) {
                numberSkipped = n;
                continue;
            }

            NextCursor next = getNextCursor(request, c, limit - numberSkipped);
            if (next != null) {
                links.add(GetCollectionItemsUtil.getNextLink(c, next, links));
            }

            StringPair link = Links.toLinkHeader(links);
            headers.put(link.getLeft(), link.getRight());
            return request.getLimit();
        }
        return numberSkipped;
    }

    private NextCursor getNextCursor(GetFeatureRequest request, GetFeatureCollection c, int limit) {
        // TODO: Optimization available: offset pagination already knows what to return here

        int oldLimit = request.getLimit();
        request.setLimit(limit);
        // TODO: Optimization available: don't fetch all properties, only the ones required by pagination
        try (FeatureStream features = c.getFt().getFeatureProducer().getFeatures(request, c)) {
            for (int i = 0; i < limit; i++) {
                if (!features.hasNext()) {
                    return null;
                }
                // Behave nicely, "read" by calling .next(), but ignore the result
                features.next();
            }
            // The first feature of next page
            return features.hasNext() ? c.getPaginationStrategy().getNextCursor(request, c, features.next()) : null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            request.setLimit(oldLimit);
        }
    }

    @Override
    public String getMediaMainType() {
        return "application";
    }

    @Override
    public String getMediaSubType() {
        return "geopackage+vnd.sqlite3";
    }

}
