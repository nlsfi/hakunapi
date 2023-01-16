package fi.nls.hakunapi.simple.servlet;

import java.io.IOException;
import java.util.List;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.HEADER_DECORATOR)
public class GzipFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) throws IOException {
        // if Content-Encoding is already set, don't do anything
        if (response.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING) != null) {
            return;
        }

        List<String> acceptEncoding = request.getHeaders().get(HttpHeaders.ACCEPT_ENCODING);
        // if empty, don't do anything
        if (acceptEncoding == null || acceptEncoding.isEmpty()) {
            return;
        }

        if (acceptEncoding.stream().anyMatch(it -> it.toLowerCase().contains("gzip"))) {
            response.getHeaders().putSingle(HttpHeaders.CONTENT_ENCODING, "gzip");
        }
    }

}
