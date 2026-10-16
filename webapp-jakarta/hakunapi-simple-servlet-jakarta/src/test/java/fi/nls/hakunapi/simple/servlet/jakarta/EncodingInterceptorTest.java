package fi.nls.hakunapi.simple.servlet.jakarta;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.junit.Test;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.WriterInterceptorContext;

public class EncodingInterceptorTest {

    private static final byte[] PLAIN = "hello hello hello hello".getBytes();

    private static class Ctx {
        final Object entity;
        final MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        OutputStream out = new ByteArrayOutputStream();
        final ByteArrayOutputStream sink = (ByteArrayOutputStream) out;
        byte[] payload;

        Ctx(Object entity, byte[] payload) {
            this.entity = entity;
            this.payload = payload;
            headers.putSingle(HttpHeaders.CONTENT_ENCODING, "gzip");
            headers.putSingle(HttpHeaders.CONTENT_LENGTH, payload.length);
        }

        WriterInterceptorContext proxy() {
            return (WriterInterceptorContext) Proxy.newProxyInstance(
                    getClass().getClassLoader(), new Class<?>[] { WriterInterceptorContext.class },
                    (p, m, a) -> {
                        switch (m.getName()) {
                        case "getEntity": return entity;
                        case "getHeaders": return headers;
                        case "getOutputStream": return out;
                        case "setOutputStream": out = (OutputStream) a[0]; return null;
                        case "proceed": out.write(payload); out.close(); return null;
                        default: throw new UnsupportedOperationException(m.getName());
                        }
                    });
        }
    }

    private static byte[] gzip(byte[] b) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (GZIPOutputStream g = new GZIPOutputStream(bos)) {
            g.write(b);
        }
        return bos.toByteArray();
    }

    private static byte[] gunzip(byte[] b) throws IOException {
        return new GZIPInputStream(new ByteArrayInputStream(b)).readAllBytes();
    }

    private static GunzipInterceptor gunzipFor(String acceptEncoding) throws Exception {
        GunzipInterceptor i = new GunzipInterceptor();
        HttpHeaders h = (HttpHeaders) Proxy.newProxyInstance(
                EncodingInterceptorTest.class.getClassLoader(), new Class<?>[] { HttpHeaders.class },
                (p, m, a) -> acceptEncoding == null ? List.of() : List.of(acceptEncoding));
        Field f = GunzipInterceptor.class.getDeclaredField("requestHeaders");
        f.setAccessible(true);
        f.set(i, h);
        return i;
    }

    @Test
    public void gzipLeavesPreEncodedBytesAlone() throws Exception {
        byte[] stored = gzip(PLAIN);
        Ctx c = new Ctx(new PreEncodedBytes(stored), stored);
        new GzipInterceptor().aroundWriteTo(c.proxy());
        assertArrayEquals(stored, c.sink.toByteArray());
    }

    @Test
    public void gunzipInflatesWhenClientDoesNotAcceptGzip() throws Exception {
        byte[] stored = gzip(PLAIN);
        for (String acceptEncoding : new String[] { null, "gzip;q=0" }) {
            Ctx c = new Ctx(new PreEncodedBytes(stored), stored);
            gunzipFor(acceptEncoding).aroundWriteTo(c.proxy());
            assertArrayEquals(PLAIN, c.sink.toByteArray());
            assertNull(c.headers.getFirst(HttpHeaders.CONTENT_ENCODING));
            assertNull(c.headers.getFirst(HttpHeaders.CONTENT_LENGTH));
        }
    }
}
