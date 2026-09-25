package fi.nls.hakunapi.bytes;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * Serves one file on an ephemeral local port, answering HEAD with
 * Content-Length and GET with a single Range. Records the range of every GET.
 */
public class RangeHttpServer implements AutoCloseable {

    /**
     * "first-last" of every GET, in order
     */
    public final List<String> ranges = new ArrayList<>();
    /**
     * Answer every range with only its first half, which a server is allowed to do
     */
    public volatile boolean halfRanges;

    private final byte[] content;
    private final HttpServer server;

    public RangeHttpServer(byte[] content) throws IOException {
        this.content = content;
        this.server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/file", this::handle);
        server.start();
    }

    public URI uri() {
        return URI.create("http://localhost:" + server.getAddress().getPort() + "/file");
    }

    public int gets() {
        synchronized (ranges) {
            return ranges.size();
        }
    }

    private void handle(HttpExchange exchange) throws IOException {
        try (exchange) {
            if ("HEAD".equals(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().set("Accept-Ranges", "bytes");
                exchange.getResponseHeaders().set("Content-Length", Integer.toString(content.length));
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            // bytes=<first>-<last>, last inclusive
            String range = exchange.getRequestHeaders().getFirst("Range").substring("bytes=".length());
            synchronized (ranges) {
                ranges.add(range);
            }
            String[] firstLast = range.split("-");
            int first = Integer.parseInt(firstLast[0]);
            if (first >= content.length) {
                exchange.sendResponseHeaders(416, -1);
                return;
            }
            // Like a real server, clamp last to EOF
            int last = Math.min(Integer.parseInt(firstLast[1]), content.length - 1);
            if (halfRanges) {
                last = first + (last - first) / 2;
            }
            int len = last - first + 1;
            exchange.getResponseHeaders().set("Content-Range", "bytes " + first + "-" + last + "/" + content.length);
            exchange.sendResponseHeaders(206, len);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(content, first, len);
            }
        }
    }

    @Override
    public void close() {
        server.stop(0);
    }

}
