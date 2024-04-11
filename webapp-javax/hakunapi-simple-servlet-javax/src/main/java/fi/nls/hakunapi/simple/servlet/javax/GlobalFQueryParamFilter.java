package fi.nls.hakunapi.simple.servlet.javax;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.MetadataFormat;

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
            String expected = Arrays.stream(MetadataFormat.values())
                    .map(it -> it.id)
                    .collect(Collectors.joining(",", "[", "]"));
            req.abortWith(ResponseUtil.exception(
                    Status.BAD_REQUEST,
                    "Invalid value for param '" + F_QUERY_PARAM + "', expected one of " + expected));
        } else {
            String modified = modifyAcceptHeader(accept, mediaTypeToPrefer);
            req.getHeaders().put("accept", List.of(modified));
        }
    }

    private String modifyAcceptHeader(String accept, List<String> mediaTypeToPrefer) {
        String a = mediaTypeToPrefer.stream().collect(Collectors.joining(", "));
        return accept == null || accept.isBlank() ? a : a + ", " + accept;
    }

}
