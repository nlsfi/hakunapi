package fi.nls.hakunapi.tiles.source.vectortile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.tiles.TileBody;

/** Produces the MVT body of a tile for a list of collections. */
public interface VectorTileGenerator {
    String getType();

    Optional<TileBody> generate(TileContext ctx, List<FeatureType> collections,
            String mediaType, Map<String, String> requestParams) throws Exception;

    public default boolean supportsBlocks() {
        return false;
    }

    public default List<byte[]> generateBlock(List<TileContext> ctxs,
            List<FeatureType> collections, double[] queryBox) throws Exception {
        throw new UnsupportedOperationException(getType() + " does not render blocks");
    }
}
