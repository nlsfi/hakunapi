package fi.nls.hakunapi.simple.servlet.javax;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
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

    @Override
    public void filter(ContainerRequestContext req) throws IOException {
        String f = req.getUriInfo().getQueryParameters().getFirst(F_QUERY_PARAM);
        if (f == null || f.isBlank()) {
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
            String accept = req.getHeaders().getFirst("accept");
            String modified = modifyAcceptHeader(accept, mediaTypeToPrefer);
            req.getHeaders().put("accept", List.of(modified));
        }
    }

    private String modifyAcceptHeader(String accept, List<String> mediaTypeToPrefer) {
        String a = mediaTypeToPrefer.stream().collect(Collectors.joining(","));
        return accept == null || accept.isBlank() ? a : a + "," + accept;
    }

}
