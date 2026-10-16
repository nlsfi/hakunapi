package fi.nls.hakunapi.tiles.source.vectortile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.tiles.TileBody;

public class RecordingGenerator implements VectorTileGenerator {
    static volatile TileContext last;

    @Override
    public String getType() {
        return "recording";
    }

    @Override
    public Optional<TileBody> generate(TileContext ctx, List<FeatureType> collections,
            String mediaType, Map<String, String> requestParams) {
        last = ctx;
        return Optional.of(out -> {  });
    }
}
