package fi.nls.hakunapi.bytes;

import java.io.IOException;
import java.io.InputStream;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A remote file read with one HTTP Range request per read, for exactly the
 * bytes asked for. No caching, no read-ahead.
 */
public class HttpRangeReader implements ByteSource {

    private static final Logger LOG = LoggerFactory.getLogger(HttpRangeReader.class);

    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS = 30_000;

    private final URL url;
    private final long size;

    public HttpRangeReader(URI uri) throws IOException {
        this.url = uri.toURL();
        this.size = readSize();
        LOG.info("Opened {} ({} bytes)", uri, size);
    }

    /**
     * A HEAD, or failing that a one-byte range whose Content-Range carries the total.
     */
    private long readSize() throws IOException {
        HttpURLConnection conn = connect();
        try {
            conn.setRequestMethod("HEAD");
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK
                    && "bytes".equalsIgnoreCase(conn.getHeaderField("Accept-Ranges"))) {
                long length = conn.getContentLengthLong();
                if (length > 0) {
                    return length;
                }
            }
        } finally {
            conn.disconnect();
        }
        return readSizeFromContentRange();
    }

    private long readSizeFromContentRange() throws IOException {
        HttpURLConnection conn = connect();
        try {
            conn.setRequestProperty("Range", "bytes=0-0");
            int status = conn.getResponseCode();
            if (status != HttpURLConnection.HTTP_PARTIAL) {
                throw new IOException("Range requests are not supported by " + url + " (status " + status + ")");
            }
            String contentRange = conn.getHeaderField("Content-Range");
            if (contentRange == null) {
                throw new IOException("No Content-Range in 206 response from " + url);
            }
            int slash = contentRange.lastIndexOf('/');
            if (slash < 0) {
                throw new IOException("Malformed Content-Range from " + url + ": " + contentRange);
            }
            try {
                return Long.parseLong(contentRange.substring(slash + 1));
            } catch (NumberFormatException e) {
                throw new IOException("Unknown total length in Content-Range from " + url + ": " + contentRange);
            }
        } finally {
            conn.disconnect();
        }
    }

    private HttpURLConnection connect() throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
        conn.setReadTimeout(READ_TIMEOUT_MS);
        return conn;
    }

    /**
     * Not disconnected on success: that would close the socket, while draining
     * the stream returns it to the keep-alive pool.
     */
    private int request(long srcOffset, byte[] dst, int dstOff, int len) throws IOException {
        long last = Math.min(srcOffset + len, size) - 1;
        HttpURLConnection conn = connect();
        conn.setRequestProperty("Range", "bytes=" + srcOffset + "-" + last);
        int status = conn.getResponseCode();
        if (status != HttpURLConnection.HTTP_PARTIAL) {
            conn.disconnect();
            throw new IOException("Expected 206 from " + url + " but got " + status);
        }
        int expected = (int) (last - srcOffset + 1);
        try (InputStream in = conn.getInputStream()) {
            int read = 0;
            while (read < expected) {
                int n = in.read(dst, dstOff + read, expected - read);
                if (n < 0) {
                    break;
                }
                read += n;
            }
            if (read < expected) {
                // A server may send less than asked for; returned, it would read as EOF
                throw new IOException("Short read from " + url + " at " + srcOffset
                        + ": expected " + expected + " bytes, got " + read);
            }
            return read;
        }
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public ByteReader open() {
        return new Reader();
    }

    @Override
    public void close() {
        // NOP, every request opens and releases its own connection
    }

    private class Reader implements ByteReader {

        /**
         * InputStream cannot read into a MemorySegment
         */
        private byte[] staging;

        @Override
        public int read(long off, byte[] dst, int dstOff, int len) throws IOException {
            if (len <= 0 || off >= size) {
                return 0;
            }
            return request(off, dst, dstOff, len);
        }

        @Override
        public int read(long off, MemorySegment dst, long dstOff, int len) throws IOException {
            if (len <= 0 || off >= size) {
                return 0;
            }
            if (staging == null || staging.length < len) {
                staging = new byte[len];
            }
            int n = request(off, staging, 0, len);
            MemorySegment.copy(staging, 0, dst, ValueLayout.JAVA_BYTE, dstOff, n);
            return n;
        }

        @Override
        public MemorySegment fetch(long off, int len) {
            return null;
        }

        @Override
        public void close() {
            staging = null;
        }

    }

}
