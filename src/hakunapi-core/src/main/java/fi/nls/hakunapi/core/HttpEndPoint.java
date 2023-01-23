package fi.nls.hakunapi.core;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public class HttpEndPoint {

    public static final int DEFAULT_CONN_TIMEOUT = 1000;
    public static final int DEFAULT_READ_TIMEOUT = 15000;

    private final String baseURL;
    private final String authorization;
    private final int connTimeout;
    private final int readTimeout;

    public HttpEndPoint(String baseURL, String username, String password, int connTimeout, int readTimeout) {
        this.baseURL = baseURL;
        this.authorization = getBasicAuthHeader(username, password);
        this.connTimeout = connTimeout;
        this.readTimeout = readTimeout;
    }
    
    public String getBaseURL() {
        return baseURL;
    }

    private String getBasicAuthHeader(String username, String password) {
        if (username == null && password == null) {
            return null;
        }
        String combined = username + ":" + password;
        Charset cs = StandardCharsets.UTF_8; // This doesn't seem to be super clear whether UTF-8 should be used
        String base64Encoded = Base64.getEncoder().encodeToString(combined.getBytes(cs));
        return "Basic " + base64Encoded;
    }

    public HttpURLConnection get(String uri, Map<String, String> headers) throws IOException {
        URL url = new URL(uri);
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setConnectTimeout(connTimeout);
        c.setReadTimeout(readTimeout);
        if (authorization != null) {
            c.setRequestProperty("Authorization", authorization);
        }
        headers.forEach(c::setRequestProperty);
        return c;
    }

}
