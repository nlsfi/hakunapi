package fi.nls.hakunapi.tiles.source.pmtiles;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class HttpRangeReader implements RangeReader {
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private final URI uri;

    public HttpRangeReader(URI uri) {
        this.uri = uri;
    }

    @Override
    public byte[] readRange(long offset, int length) throws IOException, InterruptedException {
        long end = offset + length - 1;
        HttpRequest req = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(30))
                .header("Range", "bytes=" + offset + "-" + end)
                .GET()
                .build();
        HttpResponse<byte[]> resp = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
        int status = resp.statusCode();

        if (status == 200) {
            throw new IOException("PMTiles server does not support range requests: " + uri);
        }
        if (status != 206) {
            throw new IOException("PMTiles range request failed: HTTP " + status + " for " + uri);
        }
        byte[] body = resp.body();
        if (body.length != length) {
            throw new IOException("PMTiles short range read: wanted " + length + " got " + body.length);
        }
        return body;
    }

    @Override
    public void close() {
        http.close();
    }
}
