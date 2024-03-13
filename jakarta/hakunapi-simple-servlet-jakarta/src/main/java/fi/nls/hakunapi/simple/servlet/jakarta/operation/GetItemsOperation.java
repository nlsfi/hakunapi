package fi.nls.hakunapi.simple.servlet.jakarta.operation;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotAcceptableException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.StreamingOutput;
import jakarta.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.nls.hakunapi.core.FeatureCollectionWriter;
import fi.nls.hakunapi.core.FeatureProducer;
import fi.nls.hakunapi.core.FeatureStream;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.NextCursor;
import fi.nls.hakunapi.core.OutputFormat;
import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.operation.DynamicResponseOperation;
import fi.nls.hakunapi.core.operation.ParametrizedOperation;
import fi.nls.hakunapi.core.param.CollectionsParam;
import fi.nls.hakunapi.core.param.FParam;
import fi.nls.hakunapi.core.param.GetFeatureParam;
import fi.nls.hakunapi.core.param.LimitParam;
import fi.nls.hakunapi.core.param.NextParam;
import fi.nls.hakunapi.core.request.GetFeatureCollection;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import fi.nls.hakunapi.core.request.WriteReport;
import fi.nls.hakunapi.core.schemas.Link;
import fi.nls.hakunapi.core.util.CrsUtil;
import fi.nls.hakunapi.core.util.Links;
import fi.nls.hakunapi.geojson.FeatureCollectionGeoJSON;
import fi.nls.hakunapi.simple.servlet.jakarta.ResponseUtil;
import fi.nls.hakunapi.simple.servlet.jakarta.SimpleFeatureWriter;

@Path("/items")
public class GetItemsOperation implements ParametrizedOperation, DynamicResponseOperation {

    protected static final Logger LOG = LoggerFactory.getLogger(GetItemsOperation.class);

    @Inject
    protected FeatureServiceConfig service;

    @Override
    public List<GetFeatureParam> getParameters() {
        return ParamUtil.ITEMS_PARAMS;
    }

    @Override
    public Map<String, Class<?>> getResponsesByContentType(FeatureServiceConfig service) {
        Map<String, Class<?>> map = new HashMap<>();
        for (OutputFormat f : service.getOutputFormats()) {
            map.put(f.getMimeType(), FeatureCollectionGeoJSON.class);
        }
        return map;
    }

    @GET
    public Response handle(@Context UriInfo uriInfo, @Context Request wsRequest) {
        GetFeatureRequest request = new GetFeatureRequest();
        request.setFormat(OperationUtil.determineOutputFormat(wsRequest, service.getOutputFormats()));
        try {
            MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
            GetFeaturesUtil.modify(service, request, ParamUtil.ITEMS_PARAMS, queryParams);
            OperationUtil.getQueryParams(service, uriInfo).forEach((k, v) -> request.addQueryParam(k, v));
            request.getCollections().stream()
                    .map(fc -> fc.getFt().getParameters())
                    .forEach(param -> GetFeaturesUtil.modify(service, request, param, queryParams));

            GetCollectionItemsOperation.checkUnknownOutputFormat(request);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.exception(Status.BAD_REQUEST, e.getMessage());
        } catch (NotAcceptableException e) {
            return ResponseUtil.exception(Status.NOT_ACCEPTABLE, e.getMessage());
        }

        StreamingOutput output = new StreamingOutput() {
            @Override
            public void write(OutputStream out) throws IOException, WebApplicationException {
                try (FeatureCollectionWriter writer = request.getFormat().getFeatureCollectionWriter()) {
                    final int totalLimit = request.getLimit();
                    int written = 0;
                    WriteReport report = null;
                    int srid = request.getSRID();
                    
                    writer.init(out, CrsUtil.getMaxDecimalCoordinates(srid), srid);

                    List<GetFeatureCollection> collections = request.getCollections();

                    Iterator<GetFeatureCollection> it = collections.iterator();
                    while (it.hasNext()) {
                        GetFeatureCollection c = it.next();
                        FeatureType ft = c.getFt();
                        FeatureProducer source = ft.getFeatureProducer();
                        try (FeatureStream features = source.getFeatures(request, c)) {
                            writer.startFeatureCollection(ft, c.getName());                            
                            report = SimpleFeatureWriter.writeFeatureCollection(writer, ft, c.getProperties(), features, request.getOffset(), request.getLimit());
                            written += report.numberReturned;
                            if (totalLimit != LimitParam.UNLIMITED) {
                                request.setLimit(totalLimit - written);
                            }
                            if (written == totalLimit) {
                                if (report.next != null) {
                                    break;
                                }
                            }
                            writer.endFeatureCollection();
                        }
                    }

                    List<Link> links = getLinks(request, writer, report.next, collections);
                    writer.end(true, links, written);
                } catch (Exception e) {
                    LOG.warn(e.getMessage(), e);
                    throw new WebApplicationException("Unexpected error occured");
                }
            }
        };

        ResponseBuilder builder = Response.ok();
        request.getResponseHeaders().forEach((k, v) -> builder.header(k, v));
        request.getFormat().getResponseHeaders(request).forEach((k, v) -> builder.header(k, v));
        builder.entity(output);
        return builder.build();
    }

    public List<Link> getLinks(GetFeatureRequest request, FeatureCollectionWriter writer, NextCursor cursor, List<GetFeatureCollection> remainingCollections) {
        Map<String, String> queryParams = request.getQueryParams();
        Map<String, String> queryHeaders = request.getQueryHeaders();

        String path = service.getCurrentServerURL(queryHeaders::get) + "/items";
        String mimeType = writer.getMimeType();

        List<Link> links = new ArrayList<>();

        Link self = Links.getSelfLink(path, queryParams, mimeType);
        links.add(self);

        String fParamValue = queryParams.get(FParam.QUERY_PARAM_NAME);
        for (OutputFormat f : service.getOutputFormats()) {
            if (!f.getId().equals(request.getFormat().getId())) {
                queryParams.put(FParam.QUERY_PARAM_NAME, f.getId());
                Link alt = Links.getAlternateLink(path, queryParams, f.getMimeType(), f.getId());
                links.add(alt);
            }
        }
        queryParams.put(FParam.QUERY_PARAM_NAME, fParamValue);

        if (cursor != null) {
            String collections = remainingCollections.stream()
                    .map(c -> c.getFt().getName())
                    .collect(Collectors.joining(","));
            queryParams.put(NextParam.PARAM_NAME, cursor.toWireFormat());
            queryParams.put(CollectionsParam.PARAM_NAME, collections);
            Link next = Links.getNextLink(path, queryParams, mimeType);
            links.add(next);
        }

        return links;
    }

}
