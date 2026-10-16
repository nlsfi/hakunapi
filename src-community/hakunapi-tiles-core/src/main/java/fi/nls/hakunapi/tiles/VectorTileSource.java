package fi.nls.hakunapi.tiles;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.config.HakunaConfigParser;

/** Vector counterpart of MapTileSource; a source serving a precomputed pyramid ignores the collections it is handed. */
public interface VectorTileSource extends AutoCloseable {

    String getType();

    VectorTileLayer parse(HakunaConfigParser cfg, Path path, TileMatrixSetRegistry tms, String layerId) throws Exception;

    /** False for a source reading an offline pyramid, where collection ids are metadata and no feature type need exist. */
    default boolean needsCollections(VectorTileLayer layer) {
        return true;
    }

    default Set<String> getPassthroughParams(VectorTileLayer layer) {
        return Set.of();
    }

    /** collections is never null and unused by a source serving precomputed tiles. */
    Optional<Tile> getTile(VectorTileLayer layer, String tileMatrixSetId, String tileMatrix,
            long tileRow, long tileCol, List<FeatureType> collections,
            String mediaType, Map<String, String> requestParams) throws Exception;

    @Override
    default void close() throws Exception {
        // NOP
    }

}
