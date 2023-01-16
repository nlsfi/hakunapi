package fi.nls.hakunapi.tiles;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.nls.hakunapi.core.FeatureCollectionWriter;
import fi.nls.hakunapi.core.FeatureProducer;
import fi.nls.hakunapi.core.FeatureStream;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.ValueProvider;
import fi.nls.hakunapi.core.WFS3Service;
import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.geom.HakunaGeometryFactory;
import fi.nls.hakunapi.core.operation.ParametrizedOperation;
import fi.nls.hakunapi.core.param.BufferParam;
import fi.nls.hakunapi.core.param.CollectionsParam;
import fi.nls.hakunapi.core.param.ExtentParam;
import fi.nls.hakunapi.core.param.GetFeatureParam;
import fi.nls.hakunapi.core.param.LayernamesParam;
import fi.nls.hakunapi.core.param.LimitParam;
import fi.nls.hakunapi.core.param.PropertiesParam;
import fi.nls.hakunapi.core.param.SimplifyParam;
import fi.nls.hakunapi.core.param.ToleranceParam;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.request.GetFeatureCollection;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import fi.nls.hakunapi.core.request.GetTileFeaturesRequest;
import fi.nls.hakunapi.core.tiles.MVT;
import fi.nls.hakunapi.core.tiles.TileMatrix;
import fi.nls.hakunapi.core.tiles.TilingScheme;
import fi.nls.hakunapi.core.util.CrsUtil;
import fi.nls.hakunapi.core.util.SimplifyAlgorithm;
import fi.nls.hakunapi.core.util.U;
import fi.nls.hakunapi.geojson.FeatureCollectionGeoJSON;
import fi.nls.hakunapi.geojson.hakuna.HakunaGeoJSONFeatureCollectionWriter;
import fi.nls.hakunapi.mvt.HakunaMVTFeatureWriter;
import fi.nls.hakunapi.simple.servlet.ResponseUtil;
import fi.nls.hakunapi.simple.servlet.SimpleFeatureWriter;
import fi.nls.hakunapi.simple.servlet.operation.GetFeaturesUtil;
import fi.nls.hakunapi.simple.servlet.operation.OperationUtil;
import fi.nls.hakunapi.simple.servlet.operation.ResponseClass;

@Path("/tiles")
public class GetTilesImpl implements ParametrizedOperation {

    private static final Logger LOG = LoggerFactory.getLogger(GetTilesImpl.class);

    @Inject
    private WFS3Service service;

    @Override
    public List<GetFeatureParam> getParameters() {
        return Arrays.asList(
                new CollectionsParam(),
                new PropertiesParam(),
                new LayernamesParam(),
                new BufferParam(),
                new ExtentParam(),
                new ToleranceParam(),
                new SimplifyParam()
        );
    }

    @GET
    @Path("/{tilingSchemeId}/{level}/{row}/{col}")
    @Produces(HakunaGeoJSONFeatureCollectionWriter.MIME_TYPE)
    @ResponseClass(FeatureCollectionGeoJSON.class)
    public Response getTileAsGeoJSON(@PathParam("tilingSchemeId") String tilingSchemeId,
            @PathParam("level") String level,
            @PathParam("row") String _row,
            @PathParam("col") String _col,
            @Context UriInfo uriInfo) {
        return getTile(tilingSchemeId, level, _row, _col, uriInfo, HakunaGeoJSONFeatureCollectionWriter.MIME_TYPE);
    }

    @GET
    @Path("/{tilingSchemeId}/{level}/{row}/{col}")
    @Produces(HakunaMVTFeatureWriter.MIME_TYPE)
    @ResponseClass(MVT.class)
    public Response getTileAsMVT(@PathParam("tilingSchemeId") String tilingSchemeId,
            @PathParam("level") String level,
            @PathParam("row") String _row,
            @PathParam("col") String _col,
            @Context UriInfo uriInfo) {
        return getTile(tilingSchemeId, level, _row, _col, uriInfo, HakunaMVTFeatureWriter.MIME_TYPE);
    }

    private Response getTile(String tilingSchemeId,
            String level,
            String _row,
            String _col,
            UriInfo uriInfo,
            String format) {
        String f = uriInfo.getQueryParameters().getFirst("f");
        if (f != null && !f.isEmpty()) {
            format = f;
        }
       
        GetTileFeaturesRequest request = new GetTileFeaturesRequest();
        // Always get all the features for tiles
        request.setLimit(LimitParam.UNLIMITED);

        Envelope bbox;
        int extent;
        int buffer;
        double tolerance;
        SimplifyAlgorithm algorithm;
        
        try {
        	MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
            for (GetFeatureParam paramHandler : getParameters()) {
                GetFeaturesUtil.modify(service, request, paramHandler, queryParams);
            }
            
            request.getCollections().stream()
                    .map(fc -> fc.getFt().getParameters())
                    .forEach(param -> GetFeaturesUtil.modify(service, request, param, queryParams));
            
            TilingScheme tilingScheme = GetTilesUtil.getTilingScheme(service, tilingSchemeId);
            int srid = CrsUtil.parseSRID(tilingScheme.getSupportedCRS(), "");
            request.setSRID(srid);

            TileMatrix tm = GetTilesUtil.getTileMatrix(tilingScheme, level);
            bbox = GetTilesUtil.getTileBounds(tm, _row, _col);
            extent = request.getExtent();
            buffer = request.getBuffer();
            tolerance = request.getTolerance();
            algorithm = request.getAlgorithm();

            Envelope cbox = getCbox(bbox, request.getBuffer(), request.getExtent());
            Geometry intersects = HakunaGeometryFactory.GF.toGeometry(cbox);
            intersects.setSRID(srid);
            for (GetFeatureCollection c : request.getCollections()) {
                c.addFilter(Filter.intersectsIndex(c.getFt().getGeom(), intersects));
            }
            
            OperationUtil.getQueryParams(service, uriInfo).forEach((k, v) -> request.addQueryParam(k, v));
            
            GetTilesUtil.modifyWithCollectionSpecificParams(service, request, queryParams);
            
        } catch (IllegalArgumentException e) {
            LOG.debug(e.getMessage(), e);
            return ResponseUtil.exception(Status.BAD_REQUEST, e.getMessage());
        }

        /*
         * Group the collections by their 'name' - allows clients to group multiple collections
         * into a single 'layer' via fi.nls.hakunapi.core.param.LayernamesParam
         */
        Map<String, List<GetFeatureCollection>> colsByName = groupByName(request.getCollections());
        
        FeatureCollectionWriter writer = getFeatureWriter(format, bbox, extent, buffer, tolerance, algorithm);
        StreamingOutput output = new StreamingOutput() {
            @Override
            public void write(OutputStream out) throws IOException, WebApplicationException {
                try {
                    int written = 0;
                    int srid = request.getSRID();
                    writer.init(out, CrsUtil.getMaxDecimalCoordinates(srid), srid);
                    for (Map.Entry<String, List<GetFeatureCollection>> kvp : colsByName.entrySet()) {
                        String layerName = kvp.getKey();
                        List<GetFeatureCollection> collections = kvp.getValue();
                        written += writeFeatureCollectionGroup(writer, request, layerName, collections);
                    }
                    writer.end(true, Collections.emptyList(), written);
                } catch (Exception e) {
                    LOG.warn(e.getMessage(), e);
                    throw new WebApplicationException("Unexpected error occured");
                } finally {
                    U.closeSilent(writer);
                }
            }
        };

        ResponseBuilder builder = Response.ok();
        request.getResponseHeaders().forEach((k, v) -> builder.header(k, v));
        builder.type(writer.getMimeType());
        builder.entity(output);
        return builder.build();
    }

    private Map<String, List<GetFeatureCollection>> groupByName(List<GetFeatureCollection> collections) {
        /*
         * Make sure we do this in order
         * ?collections=foo,bar,baz&layerNames=abc,def,abc
         * => Write out in order:
         * - abc = [ foo, baz ]
         * - def = [ bar ]
         * While this might sound a bit weird the order is predictable
         * and can leave it up to the client to make only sensible requests
         */
        Map<String, List<GetFeatureCollection>> groups = new LinkedHashMap<>();
        for (GetFeatureCollection c : collections) {
            groups.computeIfAbsent(c.getName(), __ -> new ArrayList<>()).add(c);
        }
        return groups;
    }

    private int writeFeatureCollectionGroup(FeatureCollectionWriter writer, GetFeatureRequest request,
            String layerName, List<GetFeatureCollection> collections) throws Exception {
        int written = 0;

        // This is a hack - we trust that the GeoJSON or MVT writers do not care about
        // the FeatureType so we just pass the FeatureType of the first collection
        writer.startFeatureCollection(collections.get(0).getFt(), layerName);
        for (GetFeatureCollection c : collections) {
            written += writeFeatureCollection(writer, request, c);
        }
        writer.endFeatureCollection();

        return written;
    }

    private int writeFeatureCollection(FeatureCollectionWriter writer, GetFeatureRequest request,
            GetFeatureCollection c) throws Exception {
        FeatureType ft = c.getFt();
        FeatureProducer producer = ft.getFeatureProducer();
        try (FeatureStream features = producer.getFeatures(request, c)) {
            List<HakunaProperty> props = c.getProperties();
            int limit = service.getLimitMaximum();
            int numberReturned = 0;
            for (; numberReturned < limit && features.hasNext(); numberReturned++) {
                SimpleFeatureWriter.writeFeature(writer, ft, props, features.next());
                writer.endFeature();
            }
            return numberReturned;
        }
    }

    private Envelope getCbox(Envelope bbox, int buffer, int extent) {
        Envelope cbox = new Envelope(bbox);
        if (buffer > 0) {
            double bufferSizePercent = (double) buffer / (double) extent;
            double dx = bufferSizePercent * bbox.getWidth();
            double dy = bufferSizePercent * bbox.getHeight();
            cbox.expandBy(dx, dy);
        }
        return cbox;
    }

    private FeatureCollectionWriter getFeatureWriter(String format, Envelope bbox, int extent, int buffer, double tolerance, SimplifyAlgorithm algorithm) {
        switch (format) {
        case "mvt":
        case "pbf":
        case HakunaMVTFeatureWriter.MIME_TYPE:
            return new HakunaMVTFeatureWriter(bbox, extent, buffer, tolerance, algorithm);
        default:
            return new HakunaGeoJSONFeatureCollectionWriter();
        }
    }

}
