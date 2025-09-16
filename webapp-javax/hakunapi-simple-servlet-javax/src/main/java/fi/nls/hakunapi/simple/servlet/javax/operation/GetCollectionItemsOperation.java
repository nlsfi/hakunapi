package fi.nls.hakunapi.simple.servlet.javax.operation;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import fi.nls.hakunapi.core.CacheSettings;
import fi.nls.hakunapi.core.FeatureCollectionWriter;
import fi.nls.hakunapi.core.FeatureProducer;
import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.FeatureStream;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.OutputFormat;
import fi.nls.hakunapi.core.SRIDCode;
import fi.nls.hakunapi.core.operation.DynamicPathOperation;
import fi.nls.hakunapi.core.operation.DynamicResponseOperation;
import fi.nls.hakunapi.core.param.FParam;
import fi.nls.hakunapi.core.param.GetFeatureParam;
import fi.nls.hakunapi.core.request.GetFeatureCollection;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import fi.nls.hakunapi.core.request.WriteReport;
import fi.nls.hakunapi.core.schemas.Link;
import fi.nls.hakunapi.core.telemetry.RequestTelemetry;
import fi.nls.hakunapi.core.telemetry.ServiceTelemetry;
import fi.nls.hakunapi.core.telemetry.TelemetrySpan;
import fi.nls.hakunapi.core.util.Links;
import fi.nls.hakunapi.core.util.StringPair;
import fi.nls.hakunapi.geojson.FeatureCollectionGeoJSON;
import fi.nls.hakunapi.simple.servlet.javax.CacheManager;
import fi.nls.hakunapi.simple.servlet.javax.ResponseUtil;
import fi.nls.hakunapi.simple.servlet.javax.SimpleFeatureWriter;

@Path("/collections/{collectionId}/items")
public class GetCollectionItemsOperation implements DynamicPathOperation, DynamicResponseOperation {

    protected static final Logger LOG = LoggerFactory.getLogger(GetCollectionItemsOperation.class);


    @Inject
    protected FeatureServiceConfig service;

    @Inject
    protected CacheManager cacheManager;

    @Override
    public List<String> getValidPaths(FeatureServiceConfig service) {
        Collection<FeatureType> collections = service.getCollections();
        List<String> paths = new ArrayList<>(collections.size());
        for (FeatureType collection : collections) {
            paths.add("/collections/" + collection.getName() + "/items");
        }
        return paths;
    }

    @Override
    public List<GetFeatureParam> getParameters(String path, FeatureServiceConfig service) {
        String id = path.split("/")[2];
        FeatureType collection = service.getCollection(id);
        return ParamUtil.getParameters(collection, service);
    }

    
    @Override
    public Map<String, Class<?>> getResponsesByContentType(FeatureServiceConfig service) {
        Map<String, Class<?>> map = new HashMap<>();
        for (OutputFormat f : service.getOutputFormats()) {
            map.put(f.getMimeType(), FeatureCollectionGeoJSON.class);
        }
        return map;
    }

    @HEAD
    public Response handleHEAD(@PathParam("collectionId") String collectionId,
            @Context UriInfo uriInfo,
            @Context Request wsRequest,
            @Context HttpHeaders headers) {
        try {
            GetFeatureRequest request = parseRequest(service, collectionId, uriInfo, wsRequest, headers);
            if (request == null) {
                return ResponseUtil.exception(Status.BAD_REQUEST, "Unknown collection");
            }
            GetFeatureCollection c = request.getCollections().get(0);
            FeatureType ft = c.getFt();
            int numberMatched = ft.getFeatureProducer().getNumberMatched(request, c);
            ResponseBuilder builder = Response.ok();
            request.getResponseHeaders().forEach((k, v) -> builder.header(k, v));
            builder.header("OGC-NumberMatched", numberMatched);
            return builder.build();
        } catch (IllegalArgumentException e) {
            return ResponseUtil.exception(Status.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.exception(Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @GET
    public Response handleGET(@PathParam("collectionId") String collectionId,
            @Context UriInfo uriInfo,
            @Context Request wsRequest,
            @Context HttpHeaders headers) {
        GetFeatureRequest request;
        try {
            request = parseRequest(service, collectionId, uriInfo, wsRequest, headers);
            if (request == null) {
                return ResponseUtil.exception(Status.BAD_REQUEST, "Unknown collection");
            }
        } catch (IllegalArgumentException e) {
            return ResponseUtil.exception(Status.BAD_REQUEST, e.getMessage());
        } catch (NotAcceptableException e) {
            return ResponseUtil.exception(Status.NOT_ACCEPTABLE, e.getMessage());
        }

        GetFeatureCollection c = request.getCollections().get(0);
        FeatureType ft = c.getFt();
        
        final ServiceTelemetry fst = service.getTelemetry();
        final RequestTelemetry ftt = fst.forRequest(request);

        Object output;
        if (ft.getCacheSettings() != null) {
            LoadingCache<CacheKey, byte[]> cache = cacheManager.getCache(ft.getName(), () -> getFeatureCache(ft, ftt));
            CacheKey key = new CacheKey(request);
            output = cache.get(key);
            if (output == null) {
                return ResponseUtil.exception(Status.INTERNAL_SERVER_ERROR, "Unexpected error occured");
            }
        } else {
            output = new StreamingOutput() {
                @Override
                public void write(OutputStream out) throws WebApplicationException {
                    try {
                        writeResponseBody(request, service, out, ftt);
                    } catch (Exception e) {
                        LOG.warn(e.getMessage(), e);
                        throw new WebApplicationException("Unexpected error occured");
                    }
                }
            };
        }

        ResponseBuilder builder = Response.ok();
        request.getResponseHeaders().forEach((k, v) -> builder.header(k, v));
        request.getFormat().getResponseHeaders(request).forEach((k, v) -> builder.header(k, v));
        builder.entity(output);
        return builder.build();
    }
    
    protected LoadingCache<CacheKey, byte[]> getFeatureCache(FeatureType ft, RequestTelemetry ftt) {
        CacheSettings settings = ft.getCacheSettings();
        return Caffeine.newBuilder()
                .maximumSize(settings.getMaximumSize())
                .refreshAfterWrite(settings.getRefreshAfterMs(), TimeUnit.MILLISECONDS)
                .expireAfterWrite(settings.getExpireAfterMs(), TimeUnit.MILLISECONDS)
                .build(cacheKey -> {
                    try( TelemetrySpan span = ftt.span()) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        writeResponseBody(cacheKey.request, service, baos, ftt);
                        return baos.toByteArray();
                    } catch (Exception e) {
                        LOG.warn(e.getMessage(), e);
                        return null;
                    }
                });
    }
    
    public static GetFeatureRequest parseRequest(FeatureServiceConfig service, String collectionId, UriInfo uriInfo, Request wsRequest, HttpHeaders headers) {
        FeatureType ft = service.getCollection(collectionId);
        if (ft == null) {
            return null;
        }

        GetFeatureCollection c = new GetFeatureCollection(ft);

        GetFeatureRequest request = new GetFeatureRequest();
        request.setFormat(OperationUtil.determineOutputFormat(wsRequest, service.getOutputFormats()));
        request.addPathParam("collectionId", collectionId);
        request.addCollection(c);
        request.setQueryHeaders(OperationUtil.toSimpleMap(headers.getRequestHeaders()));

        OperationUtil.getQueryParams(service, uriInfo).forEach((k, v) -> request.addQueryParam(k, v));

        GetFeaturesUtil.modify(service, request, ParamUtil.getParameters(ft, service), uriInfo.getQueryParameters());

        checkUnknownParameters(service, ParamUtil.getParameters(ft, service), uriInfo.getQueryParameters());

        checkUnknownOutputFormat(request);

        return request;
    }
    
    public static void writeResponseBody(GetFeatureRequest request, FeatureServiceConfig service, OutputStream out, RequestTelemetry ftt) throws Exception {
        GetFeatureCollection c = request.getCollections().get(0);
        FeatureType ft = c.getFt();
        FeatureProducer producer = c.getFt().getFeatureProducer();

        int srid = request.getSRID();

        SRIDCode sridCode = service.getSridCode(srid).get();

        try (FeatureStream features = producer.getFeatures(request, c);
                FeatureCollectionWriter writer = request.getFormat().getFeatureCollectionWriter();
                TelemetrySpan span = ftt.span()) {
            // Buffer some features here so there's higher chance of not being committed to 200 OK response if something goes wrong
            features.hasNext();

            writer.init(out, sridCode);

            writer.startFeatureCollection(ft, c.getName());
            WriteReport report = SimpleFeatureWriter.writeFeatureCollection(writer, ft, c.getProperties(), features, request, c);
            writer.endFeatureCollection();
            writer.end(true, getLinks(service, request, report), report.numberReturned);

            span.counts(report);
        }
    }

    public static void checkUnknownParameters(FeatureServiceConfig service, List<GetFeatureParam> parameters,
            MultivaluedMap<String, String> queryParameters) {
        Set<String> knownParameters = parameters.stream()
                .map(it -> it.getParamName())
                .collect(Collectors.toSet());
        for (String key : queryParameters.keySet()) {
            if (key.equals(service.getApiKeyQueryParam())) {
                continue;
            }
            if (knownParameters.contains(key)) {
                continue;
            }
            String value = queryParameters.getFirst(key);
            if (value == null || value.isEmpty()) {
                continue;
            }
            String err = String.format("Unknown parameter '%s'", key);
            throw new IllegalArgumentException(err);
        }
    }

    public static void checkUnknownOutputFormat(GetFeatureRequest request) {
        if (request.getFormat() == null) {
            throw new NotAcceptableException();
        }
    }

    public static List<Link> getLinks(FeatureServiceConfig service, GetFeatureRequest request, WriteReport report) {
        Map<String, String> queryParams = request.getQueryParams();
        Map<String, String> queryHeaders = request.getQueryHeaders();

        GetFeatureCollection c = request.getCollections().get(0);
        String path = Links.getItemsPath(service.getCurrentServerURL(queryHeaders::get), c.getFt().getName());
        String mimeType = request.getFormat().getMimeType();

        List<Link> links = new ArrayList<>();

        Link self = Links.getSelfLink(path, queryParams, mimeType);
        links.add(self);

        String fParamOriginalValue = queryParams.get(FParam.QUERY_PARAM_NAME);
        for (OutputFormat f : service.getOutputFormats()) {
            if (!f.getId().equals(request.getFormat().getId())) {
                queryParams.remove(FParam.QUERY_PARAM_NAME);
                links.add(Links.getAlternateLink(path, queryParams, f.getMimeType(), f.getId()));
                queryParams.put(FParam.QUERY_PARAM_NAME, f.getId());
                links.add(Links.getAlternateLink(path, queryParams, f.getMimeType(), f.getId()));
            }
        }
        queryParams.put(FParam.QUERY_PARAM_NAME, fParamOriginalValue);

        if (report.next != null) {
            StringPair paramNameAndValue = c.getPaginationStrategy().getNextQueryParam(report.next);
            queryParams.put(paramNameAndValue.getLeft(), paramNameAndValue.getRight());
            Link next = Links.getNextLink(path, queryParams, mimeType);
            links.add(next);
        }

        return links;
    }
    
    protected class CacheKey {
        
        private GetFeatureRequest request;
        private OutputFormat format;
        private Map<String, String> queryParams;
        
        public CacheKey(GetFeatureRequest request) {
            this.request = request;
            this.format = request.getFormat();
            this.queryParams = new HashMap<>(request.getQueryParams());
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + format.hashCode();
            result = prime * result + queryParams.hashCode();
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            CacheKey other = (CacheKey) obj;
            if (!format.equals(other.format)) {
                return false;
            }
            if (!queryParams.equals(other.queryParams)) {
                return false;
            }
            return true;
        }

    }

}
