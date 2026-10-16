package fi.nls.hakunapi.tiles.source.vectortile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import java.util.Map;
import java.util.Properties;

import org.junit.Test;

import fi.nls.hakunapi.core.config.HakunaConfigParser;
import fi.nls.hakunapi.tiles.DefaultTileMatrixSetRegistry;
import fi.nls.hakunapi.tiles.VectorTileLayer;

public class VectorTileGeneratorSourceTest {
    private static final String TMS = "WebMercatorQuad";
    private static final String MVT = "application/vnd.mapbox-vector-tile";

    private static final double WM = 20037508.342789244;
    private static final double EPS = 1e-6;

    private Properties baseProps(String generatorType) {
        Properties p = new Properties();
        p.setProperty("tiles.layers", "vt");
        p.setProperty("tiles.layers.vt.type", "vectortile-generator");
        p.setProperty("tiles.layers.vt.tileMatrixSets", TMS);
        p.setProperty("tiles.layers.vt.vectortile.generator", generatorType);
        return p;
    }

    private VectorTileLayer parse(VectorTileGeneratorSource source, Properties p) throws Exception {
        HakunaConfigParser cfg = new HakunaConfigParser(p);
        return source.parse(cfg, null, new DefaultTileMatrixSetRegistry(), "vt");
    }

    @Test
    public void resolvesTileAddressToGroundBbox() throws Exception {
        try (VectorTileGeneratorSource source = new VectorTileGeneratorSource()) {
            VectorTileLayer layer = parse(source, baseProps("recording"));

            source.getTile(layer, TMS, "0", 0, 0, java.util.List.of(), MVT, Map.of());
            TileContext ctx = RecordingGenerator.last;
            assertNotNull(ctx);
            assertEquals(3857, ctx.srid());
            assertEquals(-WM, ctx.minX(), EPS);
            assertEquals(-WM, ctx.minY(), EPS);
            assertEquals(WM, ctx.maxX(), EPS);
            assertEquals(WM, ctx.maxY(), EPS);
            assertEquals(256, ctx.tileWidth());

            source.getTile(layer, TMS, "1", 0, 1, java.util.List.of(), MVT, Map.of());
            ctx = RecordingGenerator.last;
            assertEquals(0.0, ctx.minX(), EPS);
            assertEquals(WM, ctx.maxX(), EPS);
            assertEquals(0.0, ctx.minY(), EPS);
            assertEquals(WM, ctx.maxY(), EPS);
        }
    }

    @Test
    public void unknownGeneratorTypeFailsParse() throws Exception {
        try (VectorTileGeneratorSource source = new VectorTileGeneratorSource()) {
            assertThrows(IllegalArgumentException.class, () -> parse(source, baseProps("no-such-generator")));
        }
    }

}
