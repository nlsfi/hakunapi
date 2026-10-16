package fi.nls.hakunapi.simple.servlet.jakarta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.Provider;

import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.MetadataFormat;
import fi.nls.hakunapi.core.OutputFormat;
import fi.nls.hakunapi.core.extension.ApiExtension;

@Provider
@PreMatching
public class GlobalFQueryParamFilter implements ContainerRequestFilter {

    private final FeatureServiceConfig service;

    public GlobalFQueryParamFilter(FeatureServiceConfig service) {
        this.service = service;
    }

    private static final String F_QUERY_PARAM = "f";
    
    private static final List<String> DEFAULT_ACCEPT_VALUES = 
            List.of(MediaType.APPLICATION_JSON, MediaTypes.APPLICATION_GEOJSON, 
                    MediaTypes.APPLICATION_SCHEMA, MediaTypes.APPLICATION_OPENAPI_V3,
                    "*/*" );

    @Override
    public void filter(ContainerRequestContext req) throws IOException {
        String f = req.getUriInfo().getQueryParameters().getFirst(F_QUERY_PARAM);
        String accept = req.getHeaders().getFirst("accept");
        if ((f == null || f.isBlank() ) && (accept == null || accept.isBlank())) {
            // if f not present and accept header not present
            // => force json 
            req.getHeaders().put("accept", DEFAULT_ACCEPT_VALUES);
            return;
        } else if (f == null || f.isBlank()) {
            return;
        }

        List<String> mediaTypeToPrefer = Arrays.stream(MetadataFormat.values())
                .filter(it -> it.id.equals(f))
                .map(it -> it.contentTypes)
                .findAny()
                .orElseGet(() -> service.getOutputFormat(f) != null ? List.of(service.getOutputFormat(f).getMimeType()) : null);

        if (mediaTypeToPrefer == null) {
            mediaTypeToPrefer = extensionMediaTypes(f);
        }

        if (mediaTypeToPrefer == null) {
            req.abortWith(ResponseUtil.exception(
                    Status.BAD_REQUEST,
                    "Invalid value for param '" + F_QUERY_PARAM + "', expected one of " + expectedValues()));
        } else {
            String modified = modifyAcceptHeader(accept, mediaTypeToPrefer);
            req.getHeaders().put("accept", List.of(modified));
        }
    }

    private List<String> extensionMediaTypes(String f) {
        for (ApiExtension extension : service.getApiExtensions()) {
            List<String> mediaTypes = extension.getFormatMediaTypes().get(f);
            if (mediaTypes != null && !mediaTypes.isEmpty()) {
                return mediaTypes;
            }
        }
        return null;
    }

    private String expectedValues() {
        List<String> ids = new ArrayList<>();
        for (MetadataFormat format : MetadataFormat.values()) {
            ids.add(format.id);
        }
        for (OutputFormat format : service.getOutputFormats()) {
            if (!ids.contains(format.getId())) {
                ids.add(format.getId());
            }
        }
        for (ApiExtension extension : service.getApiExtensions()) {
            for (String id : extension.getFormatMediaTypes().keySet()) {
                if (!ids.contains(id)) {
                    ids.add(id);
                }
            }
        }
        return ids.stream().collect(Collectors.joining(",", "[", "]"));
    }

    private String modifyAcceptHeader(String accept, List<String> mediaTypeToPrefer) {
        String a = mediaTypeToPrefer.stream().collect(Collectors.joining(", "));
        return accept == null || accept.isBlank() ? a : a + ", " + accept;
    }

}
