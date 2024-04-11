package fi.nls.hakunapi.simple.servlet.javax;

import java.io.IOException;
import java.util.zip.Deflater;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

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
