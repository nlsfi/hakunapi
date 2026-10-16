package fi.nls.hakunapi.tiles.source.gpkg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.junit.Test;

import fi.nls.hakunapi.core.config.HakunaConfigParser;
import fi.nls.hakunapi.tiles.Tile;
import fi.nls.hakunapi.tiles.MapTileLayer;
import fi.nls.hakunapi.tiles.TileMatrixSetRegistry;
import fi.nls.hakunapi.tiles.config.TilesConfigParser;

public class GpkgTileSourceTest {

    private static final String TMS = "Imagery3857";
    private static final double RESOLUTION = 5565.974539663679;

    private Path fixture() throws Exception {
        return Paths.get(getClass().getResource("/3857.gpkg").toURI());
    }

    private Properties baseProps(Path gpkg) {
        Properties p = new Properties();
        p.setProperty("tiles.layers", "img");
        p.setProperty("tiles.layers.img.type", "gpkg");
        p.setProperty("tiles.layers.img.tileMatrixSets", TMS);
        p.setProperty("tiles.layers.img.gpkg.path", gpkg.getFileName().toString());
        p.setProperty("tiles.layers.img.gpkg.table", "imagery");

        p.setProperty("tileMatrixSets", TMS);
        p.setProperty("tileMatrixSets." + TMS + ".srid", "3857");
        p.setProperty("tileMatrixSets." + TMS + ".tileWidth", "450");
        p.setProperty("tileMatrixSets." + TMS + ".tileHeight", "450");
        p.setProperty("tileMatrixSets." + TMS + ".bbox",
                "-20037508.342789244,5009377.085697312,-15028131.257091932,20037508.342789244");
        p.setProperty("tileMatrixSets." + TMS + ".resolutions", Double.toString(RESOLUTION));
        return p;
    }

    private MapTileLayer parse(GpkgTileSource source, Properties p, Path gpkg) throws Exception {
        HakunaConfigParser cfg = new HakunaConfigParser(p);
        TileMatrixSetRegistry tms = new TilesConfigParser(cfg).readTileMatrixSets();
        Path configPath = gpkg.resolveSibling("service.properties");
        return source.parse(cfg, configPath, tms, "img");
    }

    @Test
    public void servesPngTileMappedByResolution() throws Exception {
        Path gpkg = fixture();
        try (GpkgTileSource source = new GpkgTileSource()) {
            MapTileLayer layer = parse(source, baseProps(gpkg), gpkg);

            assertEquals("image/png", layer.getMediaTypes().get(0));

            Optional<Tile> tile = source.getTile(layer, TMS, "0", 0, 0, "image/png", Map.of());
            assertTrue(tile.isPresent());

            byte[] bytes = tile.get().getBytes();
            assertEquals((byte) 0x89, bytes[0]);
            assertEquals((byte) 'P', bytes[1]);
            assertEquals((byte) 'N', bytes[2]);
            assertEquals((byte) 'G', bytes[3]);
            assertEquals("image/png", tile.get().getMediaType());
        }
    }

    @Test
    public void configuredFormatWinsOverTileSniffing() throws Exception {
        Path gpkg = fixture();
        try (GpkgTileSource source = new GpkgTileSource()) {
            Properties p = baseProps(gpkg);
            p.setProperty("tiles.layers.img.gpkg.format", "image/webp");
            MapTileLayer layer = parse(source, p, gpkg);

            Optional<Tile> tile = source.getTile(layer, TMS, "0", 0, 0, "image/webp", Map.of());
            assertTrue(tile.isPresent());
            assertEquals("image/webp", tile.get().getMediaType());
        }
    }

    @Test
    public void unknownTableFailsParse() throws Exception {
        Path gpkg = fixture();
        try (GpkgTileSource source = new GpkgTileSource()) {
            Properties p = baseProps(gpkg);
            p.setProperty("tiles.layers.img.gpkg.table", "no_such_table");
            assertThrows(IllegalArgumentException.class, () -> parse(source, p, gpkg));
        }
    }
}
