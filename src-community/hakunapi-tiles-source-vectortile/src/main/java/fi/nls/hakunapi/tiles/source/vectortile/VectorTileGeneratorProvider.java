package fi.nls.hakunapi.tiles.source.vectortile;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;

public class VectorTileGeneratorProvider {
    private static final ServiceLoader<VectorTileGenerator> LOADER =
            ServiceLoader.load(VectorTileGenerator.class);

    public static VectorTileGenerator get(String type) {
        LOADER.reload();
        return byType(LOADER).get(type);
    }

    static Map<String, VectorTileGenerator> byType(Iterable<VectorTileGenerator> generators) {
        Map<String, VectorTileGenerator> byType = new LinkedHashMap<>();
        for (VectorTileGenerator g : generators) {
            VectorTileGenerator prev = byType.put(g.getType(), g);
            if (prev != null) {
                throw new IllegalStateException("Duplicate vector tile generator type '" + g.getType()
                        + "' on the classpath: " + prev.getClass().getName()
                        + " and " + g.getClass().getName());
            }
        }
        return byType;
    }
}
