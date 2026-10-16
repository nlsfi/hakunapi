package fi.nls.hakunapi.tiles;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import fi.nls.hakunapi.core.config.HakunaConfigParser;

/** Discovered via ServiceLoader and keyed by getType(). */
public interface MapTileSource extends AutoCloseable {

    String getType();

    MapTileLayer parse(HakunaConfigParser cfg, Path path, TileMatrixSetRegistry tms, String layerId) throws Exception;

    /** Query-param names forwarded to the upstream; everything else is dropped so a client cannot inject params. */
    default Set<String> getPassthroughParams(MapTileLayer layer) {
        return Set.of();
    }

    /** requestParams holds only whitelisted names and is never null; empty when no tile exists at the address. */
    Optional<Tile> getTile(MapTileLayer layer, String tileMatrixSetId, String tileMatrix,
            long tileRow, long tileCol, String mediaType, Map<String, String> requestParams) throws Exception;

    @Override
    default void close() throws Exception {
        // NOP
    }

}
