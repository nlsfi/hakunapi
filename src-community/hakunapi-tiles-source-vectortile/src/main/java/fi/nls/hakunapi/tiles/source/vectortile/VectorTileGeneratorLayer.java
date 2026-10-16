package fi.nls.hakunapi.tiles.source.vectortile;

import java.util.List;
import java.util.Set;

import fi.nls.hakunapi.tiles.VectorTileSource;
import fi.nls.hakunapi.tiles.VectorTileLayer;

public class VectorTileGeneratorLayer extends VectorTileLayer {
    private final VectorTileGenerator generator;
    private final Set<String> passthroughParams;

    public VectorTileGeneratorLayer(String id, String title, String description, VectorTileSource source,
            List<String> tileMatrixSetIds, List<String> mediaTypes, double[] bbox,
            VectorTileGenerator generator, List<String> collectionIds, Set<String> passthroughParams) {
        super(id, title, description, source, collectionIds, tileMatrixSetIds, mediaTypes, bbox);
        this.generator = generator;
        this.passthroughParams = Set.copyOf(passthroughParams);
    }

    public VectorTileGenerator getGenerator() {
        return generator;
    }

    public Set<String> getPassthroughParams() {
        return passthroughParams;
    }
}
