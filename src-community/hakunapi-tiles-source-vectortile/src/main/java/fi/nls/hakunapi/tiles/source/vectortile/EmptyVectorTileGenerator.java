package fi.nls.hakunapi.tiles.source.vectortile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.tiles.TileBody;

public class EmptyVectorTileGenerator implements VectorTileGenerator {
    public static final String TYPE = "empty";

    private static final TileBody EMPTY_MVT = out -> {  };

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Optional<TileBody> generate(TileContext ctx, List<FeatureType> collections,
            String mediaType, Map<String, String> requestParams) {
        return Optional.of(EMPTY_MVT);
    }
}
