package fi.nls.hakunapi.bytes;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

import fi.nls.hakunapi.bytes.sozip.SOZip;
import fi.nls.hakunapi.bytes.sozip.SOZipEntry;

/**
 * Opens a location written in a subset of GDAL's virtual file syntax, see the
 * README.
 */
public final class ByteSources {

    private static final String VSIZIP = "/vsizip/";
    private static final String VSICURL = "/vsicurl/";

    private static final int HTTP_BLOCK = 4096;
    private static final int HTTP_READ_AHEAD = 512 * 1024;

    private ByteSources() {
        // Use static methods
    }

    public enum Mode {
        PREAD,
        MMAP,
        MEMORY
    }

    /**
     * entry is null for an archive's only entry
     */
    public record Location(String path, boolean http, boolean zip, String entry) {}

    public static ByteSource open(String location, long cacheBytes, Mode mode)
            throws IllegalArgumentException, IOException {
        Location loc = parse(location);
        ByteSource raw = loc.http()
                ? new HttpRangeReader(URI.create(loc.path()))
                : mode == Mode.MMAP ? new MappedFile(Path.of(loc.path())) : new PreadFile(Path.of(loc.path()));
        ByteSource src = loc.zip() ? SOZip.open(raw, loc.entry()) : raw;
        if (mode == Mode.MEMORY) {
            return new InMemoryFile(src);
        }
        if (cacheBytes <= 0 || !(loc.zip() || loc.http())) {
            return src;
        }
        try {
            if (src instanceof SOZipEntry e) {
                int readAhead = loc.http() ? Math.max(1, HTTP_READ_AHEAD / e.chunkSize()) : 1;
                return new CachingByteSource(e, e.chunkSize(), cacheBytes, readAhead);
            }
            return new CachingByteSource(src, HTTP_BLOCK, cacheBytes, HTTP_READ_AHEAD / HTTP_BLOCK);
        } catch (RuntimeException e) {
            src.close();
            throw e;
        }
    }

    public static Location parse(String location) throws IllegalArgumentException {
        String path = location;
        boolean zip = false;
        String entry = null;
        if (path.startsWith(VSIZIP)) {
            zip = true;
            String rest = path.substring(VSIZIP.length());
            if (rest.startsWith("vsi")) {
                rest = "/" + rest;
            }
            int split;
            if (rest.startsWith("{")) {
                int close = rest.indexOf('}');
                if (close < 0 || (close + 1 < rest.length() && rest.charAt(close + 1) != '/')) {
                    throw new IllegalArgumentException("Unclosed { or no / after } in " + location);
                }
                path = rest.substring(1, close);
                split = close + 1;
            } else {
                split = endOfZip(rest);
                if (split < 0) {
                    throw new IllegalArgumentException("No .zip archive in " + location);
                }
                path = rest.substring(0, split);
            }
            int e = split;
            while (e < rest.length() && rest.charAt(e) == '/') {
                e++;
            }
            if (e < rest.length()) {
                entry = rest.substring(e);
            }
        }
        boolean http = false;
        if (path.startsWith(VSICURL)) {
            path = path.substring(VSICURL.length());
            if (!isHttp(path)) {
                throw new IllegalArgumentException("Not an http(s) URL after /vsicurl/ in " + location);
            }
            http = true;
        } else if (isHttp(path)) {
            if (zip) {
                throw new IllegalArgumentException("A URL inside /vsizip/ needs /vsicurl/ in " + location);
            }
            http = true;
        } else if (path.startsWith("/vsi")) {
            throw new IllegalArgumentException("Unsupported virtual file in " + location
                    + ", only /vsizip/ and /vsicurl/ are supported (a local path is written /./vsi...)");
        }
        return new Location(path, http, zip, entry);
    }

    /**
     * Where the first path segment ending in .zip (any case) ends, or -1.
     */
    private static int endOfZip(String s) {
        for (int i = s.indexOf('.'); i >= 0; i = s.indexOf('.', i + 1)) {
            int end = i + 4;
            if (end <= s.length() && s.regionMatches(true, i, ".zip", 0, 4)
                    && (end == s.length() || s.charAt(end) == '/')
                    && i > 0 && s.charAt(i - 1) != '/') {
                return end;
            }
        }
        return -1;
    }

    private static boolean isHttp(String s) {
        return s.regionMatches(true, 0, "http://", 0, 7) || s.regionMatches(true, 0, "https://", 0, 8);
    }

}
