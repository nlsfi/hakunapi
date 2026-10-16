package fi.nls.hakunapi.simple.servlet.jakarta;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.GZIPInputStream;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;

@Provider
@Priority(Priorities.ENTITY_CODER - 1)
public class GunzipInterceptor implements WriterInterceptor {

    @Context
    private HttpHeaders requestHeaders;

    @Override
    public void aroundWriteTo(WriterInterceptorContext context)
            throws IOException, WebApplicationException {
        Object entity = context.getEntity();
        boolean storedCompressed = entity instanceof PreEncodedOutput
                || entity instanceof PreEncodedBytes;
        if (!storedCompressed
                || !"gzip".equals(context.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING))
                || acceptsGzip()) {
            context.proceed();
            return;
        }

        OutputStream out = context.getOutputStream();
        ByteArrayOutputStream compressed = new ByteArrayOutputStream(16 * 1024);
        context.setOutputStream(compressed);
        context.getHeaders().remove(HttpHeaders.CONTENT_ENCODING);
        context.getHeaders().remove(HttpHeaders.CONTENT_LENGTH);
        context.proceed();

        try (InputStream in = new GZIPInputStream(
                new java.io.ByteArrayInputStream(compressed.toByteArray()))) {
            in.transferTo(out);
        }
        context.setOutputStream(out);
    }

    private boolean acceptsGzip() {
        if (requestHeaders == null) {
            return true;
        }
        List<String> accepted = requestHeaders.getRequestHeader(HttpHeaders.ACCEPT_ENCODING);
        if (accepted == null || accepted.isEmpty()) {
            return false;
        }
        for (String value : accepted) {
            if (value == null) {
                continue;
            }
            for (String token : value.split(",")) {
                String[] parts = token.split(";");
                if (!"gzip".equalsIgnoreCase(parts[0].trim())) {
                    continue;
                }
                boolean rejected = false;
                for (int i = 1; i < parts.length; i++) {
                    String param = parts[i].trim();
                    if (param.regionMatches(true, 0, "q=", 0, 2)) {
                        try {
                            rejected = Double.parseDouble(param.substring(2).trim()) == 0;
                        } catch (NumberFormatException ignore) {
                        }
                    }
                }
                if (!rejected) {
                    return true;
                }
            }
        }
        return false;
    }

}
