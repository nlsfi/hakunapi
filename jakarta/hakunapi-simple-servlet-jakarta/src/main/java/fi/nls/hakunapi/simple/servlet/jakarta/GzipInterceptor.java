package fi.nls.hakunapi.simple.servlet.jakarta;

import java.io.IOException;
import java.util.zip.Deflater;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;

import fi.nls.hakunapi.core.util.GzipOutputStream;

@Provider
@Priority(Priorities.ENTITY_CODER)
public class GzipInterceptor implements WriterInterceptor {

    @Override
    public void aroundWriteTo(WriterInterceptorContext context)
            throws IOException, WebApplicationException {
        String contentEncoding = (String) context.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING);
        if ("gzip".equals(contentEncoding)) {
            context.setOutputStream(new GzipOutputStream(context.getOutputStream(), new Deflater(5, true), 8192));
        }
        context.proceed();
    }

}
