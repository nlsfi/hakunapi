package fi.nls.hakunapi.tiles.servlet.jakarta;

import java.io.IOException;
import java.util.List;

import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.StreamingOutput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.nls.hakunapi.simple.servlet.jakarta.PreEncodedBytes;
import fi.nls.hakunapi.simple.servlet.jakarta.PreEncodedOutput;
import fi.nls.hakunapi.simple.servlet.jakarta.ResponseUtil;
import fi.nls.hakunapi.tiles.Tile;

/** Streams a {@link Tile} as the HTTP response. */
public final class TileResponse {
    private static final Logger LOG = LoggerFactory.getLogger(TileResponse.class);

    private static final CacheControl IMMUTABLE = immutable();

    private static CacheControl immutable() {
        CacheControl cc = new CacheControl();
        cc.setNoTransform(false);
        cc.setMaxAge(604800);

        cc.getCacheExtension().put("immutable", null);
        return cc;
    }

    private TileResponse() {}

    public static Response ok(Tile tile, String defaultMediaType) {
        boolean preEncoded = tile.getContentEncoding() != null;
        String mediaType = tile.getMediaType() != null ? tile.getMediaType() : defaultMediaType;
        if (tile.getLength() >= 0) {
            return precomputed(tile, mediaType, preEncoded);
        }
        StreamingOutput write = out -> {
            try {
                tile.writeTo(out);
            } catch (Exception e) {
                LOG.warn("Failed to write tile", e);
                throw new IOException(e);
            }
        };

        StreamingOutput body = preEncoded ? (PreEncodedOutput) write::write : write;

        Response.ResponseBuilder rb = Response.ok(body).type(mediaType);
        if (preEncoded) {
            rb.header("Content-Encoding", tile.getContentEncoding());
        }
        return rb.build();
    }

    private static Response precomputed(Tile tile, String mediaType, boolean preEncoded) {
        byte[] bytes;
        try {
            bytes = tile.getBytes();
        } catch (Exception e) {
            LOG.warn("Failed to read tile", e);
            return ResponseUtil.exception(Status.INTERNAL_SERVER_ERROR, "Failed to read tile");
        }

        Object body = preEncoded ? new PreEncodedBytes(bytes) : bytes;
        Response.ResponseBuilder rb = Response.ok(body).type(mediaType);

        rb.header(jakarta.ws.rs.core.HttpHeaders.CONTENT_LENGTH, bytes.length);
        if (preEncoded) {
            rb.header("Content-Encoding", tile.getContentEncoding());
        }
        rb.tag(new EntityTag(tile.getTileMatrix() + '-' + tile.getTileRow() + '-'
                + tile.getTileCol() + '-' + bytes.length));
        rb.cacheControl(IMMUTABLE);
        return rb.build();
    }

    public static Response ok(List<Tile> parts, String mediaType) {
        if (parts.size() == 1) {
            return ok(parts.get(0), mediaType);
        }
        for (int i = 0; i < parts.size(); i++) {
            String encoding = parts.get(i).getContentEncoding();
            if (encoding != null) {
                throw new IllegalArgumentException("Cannot compose a tile from a " + encoding
                        + "-encoded part; a compressed tile layer is served on its own");
            }
        }
        StreamingOutput body = out -> {
            try {
                for (int i = 0; i < parts.size(); i++) {
                    parts.get(i).writeTo(out);
                }
            } catch (Exception e) {
                LOG.warn("Failed to write tile", e);
                throw new IOException(e);
            }
        };
        return Response.ok(body).type(mediaType).build();
    }
}
