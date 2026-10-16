package fi.nls.hakunapi.tiles.source.vectortile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.tiles.TileBody;

public class VectorTileGeneratorProviderTest {
    private static class FakeGenerator implements VectorTileGenerator {
        private final String type;

        FakeGenerator(String type) {
            this.type = type;
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public Optional<TileBody> generate(TileContext ctx, List<FeatureType> collections,
                String mediaType, Map<String, String> requestParams) {
            return Optional.empty();
        }
    }

    @Test
    public void looksUpRegisteredGeneratorByType() {
        VectorTileGenerator g = VectorTileGeneratorProvider.get(EmptyVectorTileGenerator.TYPE);
        assertNotNull(g);
        assertEquals(EmptyVectorTileGenerator.TYPE, g.getType());
    }

    @Test
    public void duplicateTypeIdFailsFast() {
        assertThrows(IllegalStateException.class, () -> VectorTileGeneratorProvider.byType(List.of(
                new FakeGenerator("dup"), new FakeGenerator("dup"))));
    }
}
