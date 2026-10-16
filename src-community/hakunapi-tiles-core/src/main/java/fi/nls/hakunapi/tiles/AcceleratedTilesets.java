package fi.nls.hakunapi.tiles;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Populated at startup, read per request, never written again; a tileset is in here only when its layer opted in. */
public class AcceleratedTilesets {

    public static final AcceleratedTilesets NONE = new AcceleratedTilesets(Map.of());

    private final Map<String, Entry> byTileset;

    private AcceleratedTilesets(Map<String, Entry> byTileset) {
        this.byTileset = byTileset;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Entry get(String tilesetId) {
        return byTileset.get(tilesetId);
    }

    public boolean isEmpty() {
        return byTileset.isEmpty();
    }

    public Set<String> getTilesetIds() {
        return Collections.unmodifiableSet(byTileset.keySet());
    }

    public record Entry(TileLocator locator, String internalLocation, Path file,
            String sendfilePath, String mediaType, String contentEncoding) {

        public Entry(TileLocator locator, String internalLocation, Path file) {
            // Resolved once here, not per request.
            this(locator, internalLocation, file,
                    file != null ? file.toAbsolutePath().toString() : null,
                    locator.getMediaType(), locator.getContentEncoding());
        }

        /** Redirect URI <location>/<offset>/<lastByte>/<encoding>; last byte rather than length because an HTTP range is inclusive. */
        public String redirectUri(long offset, int length, String encoding) {
            return internalLocation + "/" + offset + "/" + (offset + length - 1)
                    + "/" + (encoding != null ? encoding : "identity");
        }

    }

    public static class Builder {

        private final Map<String, Entry> byTileset = new LinkedHashMap<>();

        public Builder add(String tilesetId, TileLocator locator, String internalLocation, Path file) {
            byTileset.put(tilesetId, new Entry(locator, internalLocation, file));
            return this;
        }

        public AcceleratedTilesets build() {
            return byTileset.isEmpty() ? NONE : new AcceleratedTilesets(Map.copyOf(byTileset));
        }

    }

}
