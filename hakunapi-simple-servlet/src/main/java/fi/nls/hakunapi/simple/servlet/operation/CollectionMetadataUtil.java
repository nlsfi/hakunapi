package fi.nls.hakunapi.simple.servlet.operation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.OutputFormat;
import fi.nls.hakunapi.core.WFS3Service;
import fi.nls.hakunapi.core.schemas.CollectionInfo;
import fi.nls.hakunapi.core.schemas.Crs;
import fi.nls.hakunapi.core.schemas.Extent;
import fi.nls.hakunapi.core.schemas.Link;
import fi.nls.hakunapi.core.schemas.SpatialExtent;
import fi.nls.hakunapi.core.schemas.TemporalExtent;
import fi.nls.hakunapi.core.schemas.Trs;
import fi.nls.hakunapi.core.util.CrsUtil;
import fi.nls.hakunapi.core.util.U;

public class CollectionMetadataUtil {
    
    private static final String ITEMS_REL = "items";

    public static CollectionInfo toCollectionInfo(WFS3Service service, FeatureType ft, Map<String, String> queryParams) {
        String id = ft.getName();
        String title = ft.getTitle();
        String description = ft.getDescription();

        List<Link> links = new ArrayList<>();
        for (OutputFormat f : service.getOutputFormats()) {
            links.add(getItemsLink(service, ft, f.getMimeType()));
        }
        for (OutputFormat f : service.getOutputFormats()) {
            links.add(getItemsLinkWithF(service, ft, queryParams, f));
        }
        links.add(getDescribedByLink(service, queryParams, ft));
        links.add(getQueryablesLinks(service, queryParams, ft));

        String[] crs = null;
        String storageCrs = null;
        if (ft.getGeom() != null) {
            int[] srids = ft.getGeom().getSrid();
            crs = new String[srids.length];
            for (int i = 0; i < srids.length; i++) {
                crs[i] = CrsUtil.toUri(srids[i]);
            }
            storageCrs = CrsUtil.toUri(ft.getGeom().getStorageSRID());
        }

        double[] bbox = ft.getSpatialExtent();
        Instant[] temporal = ft.getTemporalExtent();
        SpatialExtent spatialExtent = bbox == null ? null : new SpatialExtent(bbox, Crs.CRS84);
        TemporalExtent temporalExtent = temporal == null ? null : new TemporalExtent(temporal, Trs.Gregorian);
        Extent extent = new Extent(spatialExtent, temporalExtent);
        
        CollectionInfo ci = new CollectionInfo(id, title, description, links, extent, crs, storageCrs);
        ci.setExtensions(ft.getMetadata());
        return ci;
    }

    public static Link getItemsLink(WFS3Service service, FeatureType ft, String mimeType) {
        String path = "/collections/" + ft.getName() + "/items";
        String href = service.getCurrentServerURL() + path;
        String rel = ITEMS_REL;
        String type = mimeType;
        return new Link(href, rel, type);
    }

    public static Link getItemsLinkWithF(WFS3Service service, FeatureType ft, Map<String, String> queryParams, OutputFormat format) {
        queryParams.put("f", format.getId());
        String query = U.toQuery(queryParams);
        String path = "/collections/" + ft.getName() + "/items" + query;
        String href = service.getCurrentServerURL() + path;
        String rel = ITEMS_REL;
        String type = format.getMimeType();
        queryParams.remove("f");
        return new Link(href, rel, type);
    }
    
    private static Link getDescribedByLink(WFS3Service service, Map<String, String> queryParams, FeatureType ft) {
        String query = U.toQuery(queryParams);
        String path = "/collections/" + ft.getName() + "/schema" + query;
        String href = service.getCurrentServerURL() + path;
        String rel = "describedBy";
        String type = "application/schema+json";
        return new Link(href, rel, type);
    }

    private static Link getQueryablesLinks(WFS3Service service, Map<String, String> queryParams, FeatureType ft) {
        String query = U.toQuery(queryParams);
        String path = "/collections/" + ft.getName() + "/queryables" + query;
        String href = service.getCurrentServerURL() + path;
        String rel = "http://www.opengis.net/def/rel/ogc/1.0/queryables";
        String type = "application/schema+json";
        return new Link(href, rel, type);
    }

}
