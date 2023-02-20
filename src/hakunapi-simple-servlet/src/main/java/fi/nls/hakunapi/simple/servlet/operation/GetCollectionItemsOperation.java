package fi.nls.hakunapi.simple.servlet.operation;

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
import fi.nls.hakunapi.core.FeatureStream;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.OutputFormat;
import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.operation.DynamicPathOperation;
import fi.nls.hakunapi.core.operation.DynamicResponseOperation;
import fi.nls.hakunapi.core.param.FParam;
import fi.nls.hakunapi.core.param.GetFeatureParam;
import fi.nls.hakunapi.core.param.NextParam;
import fi.nls.hakunapi.core.request.GetFeatureCollection;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import fi.nls.hakunapi.core.request.WriteReport;
import fi.nls.hakunapi.core.schemas.Link;
import fi.nls.hakunapi.core.util.CrsUtil;
import fi.nls.hakunapi.core.util.Links;
import fi.nls.hakunapi.geojson.FeatureCollectionGeoJSON;
import fi.nls.hakunapi.simple.servlet.CacheManager;
import fi.nls.hakunapi.simple.servlet.ResponseUtil;
import fi.nls.hakunapi.simple.servlet.SimpleFeatureWriter;

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
            @Context Request wsRequest) {
        try {
            GetFeatureRequest request = parseRequest(service, collectionId, uriInfo, wsRequest);
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
            @Context Request wsRequest, @Context HttpHeaders headers) {
        GetFeatureRequest request;
        try {
            request = parseRequest(service, collectionId, uriInfo, wsRequest);
            if (request == null) {
                return ResponseUtil.exception(Status.BAD_REQUEST, "Unknown collection");
            }
        } catch (IllegalArgumentException e) {
            return ResponseUtil.exception(Status.BAD_REQUEST, e.getMessage());
        }

        GetFeatureCollection c = request.getCollections().get(0);
        FeatureType ft = c.getFt();

        Object output;
        if (ft.getCacheSettings() != null) {
            LoadingCache<CacheKey, byte[]> cache = cacheManager.getCache(ft.getName(), () -> getFeatureCache(ft));
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
                        writeResponseBody(request, service, out);
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
    
    protected LoadingCache<CacheKey, byte[]> getFeatureCache(FeatureType ft) {
        CacheSettings settings = ft.getCacheSettings();
        return Caffeine.newBuilder()
                .maximumSize(settings.getMaximumSize())
                .refreshAfterWrite(settings.getRefreshAfterMs(), TimeUnit.MILLISECONDS)
                .expireAfterWrite(settings.getExpireAfterMs(), TimeUnit.MILLISECONDS)
                .build(cacheKey -> {
                    try {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        writeResponseBody(cacheKey.request, service, baos);
                        return baos.toByteArray();
                    } catch (Exception e) {
                        LOG.warn(e.getMessage(), e);
                        return null;
                    }
                });
    }
    
    public static GetFeatureRequest parseRequest(FeatureServiceConfig service, String collectionId, UriInfo uriInfo, Request wsRequest) {
        FeatureType ft = service.getCollection(collectionId);
        if (ft == null) {
            return null;
        }

        GetFeatureCollection c = new GetFeatureCollection(ft);

        GetFeatureRequest request = new GetFeatureRequest();
        request.setFormat(OperationUtil.determineOutputFormat(wsRequest, service.getOutputFormats()));
        request.addPathParam("collectionId", collectionId);
        request.addCollection(c);

        OperationUtil.getQueryParams(service, uriInfo).forEach((k, v) -> request.addQueryParam(k, v));

        GetFeaturesUtil.modify(service, request, ParamUtil.getParameters(ft, service), uriInfo.getQueryParameters());

        checkUnknownParameters(service, ParamUtil.getParameters(ft, service), uriInfo.getQueryParameters());
        
        return request;
    }
    
    public static void writeResponseBody(GetFeatureRequest request, FeatureServiceConfig service, OutputStream out) throws Exception {
        GetFeatureCollection c = request.getCollections().get(0);
        FeatureType ft = c.getFt();
        FeatureProducer producer = c.getFt().getFeatureProducer();
        try (FeatureStream features = producer.getFeatures(request, c);
                FeatureCollectionWriter writer = request.getFormat().getFeatureCollectionWriter()) {
            int srid = request.getSRID();
            writer.init(out, CrsUtil.getMaxDecimalCoordinates(srid), srid);
            writer.initGeometryWriter(
                    CrsUtil.getGeomDimensionForSrid(c.getFt().getGeomDimension(), srid));
            writer.startFeatureCollection(ft, c.getName());
            WriteReport report = SimpleFeatureWriter.writeFeatureCollection(writer, ft, c.getProperties(), features, request.getOffset(), request.getLimit());
            writer.endFeatureCollection();
            writer.end(true, getLinks(service, request, report), report.numberReturned);
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

    public static List<Link> getLinks(FeatureServiceConfig service, GetFeatureRequest request, WriteReport report) {
        GetFeatureCollection c = request.getCollections().get(0);
        String path = Links.getItemsPath(service.getCurrentServerURL(), c.getFt().getName());
        Map<String, String> queryParams = request.getQueryParams();
        String mimeType = request.getFormat().getMimeType();

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

        if (report.next != null) {
            queryParams.put(NextParam.PARAM_NAME, report.next.toWireFormat());
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
