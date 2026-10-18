package fi.nls.hakunapi.tiles.source.pmtiles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.junit.Test;

import fi.nls.hakunapi.core.config.HakunaConfigParser;
import fi.nls.hakunapi.tiles.DefaultTileMatrixSetRegistry;
import fi.nls.hakunapi.tiles.Tile;
import fi.nls.hakunapi.tiles.VectorTileLayer;

public class PMTilesVectorTileSourceTest {
    private static final String TMS = "WebMercatorQuad";

    private Path fixture() throws Exception {
        return Paths.get(getClass().getResource("/test_fixture_1.pmtiles").toURI());
    }

    private Properties baseProps(Path archive) {
        Properties p = new Properties();
        p.setProperty("tiles.layers", "pm_layer");
        p.setProperty("tiles.layers.pm_layer.type", "pmtiles-vector");
        p.setProperty("tiles.layers.pm_layer.tileMatrixSets", TMS);

        p.setProperty("tiles.layers.pm_layer.pmtiles.path", archive.getFileName().toString());
        return p;
    }

    private VectorTileLayer parse(PMTilesVectorTileSource source, Properties p, Path archive) throws Exception {
        HakunaConfigParser cfg = new HakunaConfigParser(p);
        Path configPath = archive.resolveSibling("service.properties");
        return source.parse(cfg, configPath, new DefaultTileMatrixSetRegistry(), "pm_layer");
    }

    @Test
    public void servesTileBytesVerbatimWithGzipEncoding() throws Exception {
        Path archive = fixture();
        try (PMTilesVectorTileSource source = new PMTilesVectorTileSource()) {
            VectorTileLayer layer = parse(source, baseProps(archive), archive);

            assertEquals("application/vnd.mapbox-vector-tile", layer.getMediaTypes().get(0));

            Optional<Tile> tile = source.getTile(layer, TMS, "0", 0, 0, List.of(),
                    "application/vnd.mapbox-vector-tile", Map.of());

            assertTrue(tile.isPresent());
            byte[] bytes = tile.get().getBytes();

            assertEquals(69, bytes.length);
            assertEquals((byte) 0x1f, bytes[0]);
            assertEquals((byte) 0x8b, bytes[1]);
            assertEquals("application/vnd.mapbox-vector-tile", tile.get().getMediaType());
            assertEquals("gzip", tile.get().getContentEncoding());
        }
    }

    @Test
    public void missingTileReturnsEmpty() throws Exception {
        Path archive = fixture();
        try (PMTilesVectorTileSource source = new PMTilesVectorTileSource()) {
            VectorTileLayer layer = parse(source, baseProps(archive), archive);

            Optional<Tile> tile = source.getTile(layer, TMS, "1", 0, 0, List.of(),
                    "application/vnd.mapbox-vector-tile", Map.of());
            assertFalse(tile.isPresent());
        }
    }

    @Test
    public void outOfRangeTileReturnsEmpty() throws Exception {
        Path archive = fixture();
        try (PMTilesVectorTileSource source = new PMTilesVectorTileSource()) {
            VectorTileLayer layer = parse(source, baseProps(archive), archive);

            Optional<Tile> tile = source.getTile(layer, TMS, "40", 0, 0, List.of(),
                    "application/vnd.mapbox-vector-tile", Map.of());
            assertFalse(tile.isPresent());
        }
    }

}
