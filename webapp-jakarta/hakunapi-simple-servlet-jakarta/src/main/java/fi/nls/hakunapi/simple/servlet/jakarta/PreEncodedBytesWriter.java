package fi.nls.hakunapi.simple.servlet.jakarta;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;

@Provider
public class PreEncodedBytesWriter implements MessageBodyWriter<PreEncodedBytes> {

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
            MediaType mediaType) {
        return PreEncodedBytes.class == type;
    }

    @Override
    public long getSize(PreEncodedBytes body, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        return body.getBytes().length;
    }

    @Override
    public void writeTo(PreEncodedBytes body, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> headers, OutputStream out)
            throws IOException, WebApplicationException {
        out.write(body.getBytes());
    }

}
