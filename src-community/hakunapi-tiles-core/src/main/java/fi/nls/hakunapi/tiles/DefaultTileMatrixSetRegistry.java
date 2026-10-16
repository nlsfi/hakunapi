package fi.nls.hakunapi.tiles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DefaultTileMatrixSetRegistry implements TileMatrixSetRegistry {

    public static final String WEB_MERCATOR_QUAD = "WebMercatorQuad";

    private final Map<String, TileMatrixSet> byId = new LinkedHashMap<>();

    public DefaultTileMatrixSetRegistry() {
        this(List.of());
    }

    public DefaultTileMatrixSetRegistry(Collection<TileMatrixSet> additional) {
        TileMatrixSet wmq = webMercatorQuad();
        byId.put(wmq.getId(), wmq);
        for (TileMatrixSet tms : additional) {
            byId.put(tms.getId(), tms);
        }
    }

    @Override
    public Collection<TileMatrixSet> list() {
        return new ArrayList<>(byId.values());
    }

    @Override
    public Optional<TileMatrixSet> get(String tileMatrixSetId) {
        return Optional.ofNullable(byId.get(tileMatrixSetId));
    }

    /** WebMercatorQuad per OGC 17-083r4 Annex D: levels 0-24, 256x256 px tiles. */
    public static TileMatrixSet webMercatorQuad() {
        final double origin = 20037508.342789244;
        final double pointOfOriginX = -origin;
        final double pointOfOriginY = origin;
        final double scaleDenominator0 = 559082264.0287178;
        final double cellSize0 = 156543.03392804097;
        final int tileSize = 256;

        List<TileMatrix> matrices = new ArrayList<>(25);
        for (int z = 0; z <= 24; z++) {
            double factor = Math.pow(2, z);
            double scaleDenominator = scaleDenominator0 / factor;
            double cellSize = cellSize0 / factor;
            int matrixSize = (int) factor;
            matrices.add(new TileMatrix(Integer.toString(z), scaleDenominator, cellSize,
                    pointOfOriginX, pointOfOriginY, tileSize, tileSize, matrixSize, matrixSize));
        }

        return new TileMatrixSet(WEB_MERCATOR_QUAD, "Google Maps Compatible for the World",
                "http://www.opengis.net/def/crs/EPSG/0/3857", 3857, matrices);
    }

}
