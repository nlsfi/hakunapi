package fi.nls.hakunapi.tiles.zoom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.junit.Test;

import fi.nls.hakunapi.core.config.HakunaConfigParser;
import fi.nls.hakunapi.tiles.Tile;
import fi.nls.hakunapi.tiles.TileMatrixSetRegistry;
import fi.nls.hakunapi.tiles.VectorTileLayer;
import fi.nls.hakunapi.tiles.VectorTileSource;
import fi.nls.hakunapi.tiles.config.TilesConfigParser;

public class ZoomRoutedVectorTileSourceTest {

    private static final String TMS = "TEST";

    private static Properties baseProps() {
        Properties p = new Properties();
        p.setProperty("tileMatrixSets", TMS);
        p.setProperty("tileMatrixSets." + TMS + ".srid", "3067");
        p.setProperty("tileMatrixSets." + TMS + ".bbox", "0,0,1024,1024");
        p.setProperty("tileMatrixSets." + TMS + ".resolutions", "4,2,1");

        p.setProperty("tiles.layers", "mtk");
        p.setProperty("tiles.layers.mtk.type", ZoomRoutedVectorTileSource.TYPE);
        p.setProperty("tiles.layers.mtk.tileMatrixSets", TMS);
        return p;
    }

    private static VectorTileLayer parse(ZoomRoutedVectorTileSource source, Properties p) throws Exception {
        HakunaConfigParser cfg = new HakunaConfigParser(p);
        TileMatrixSetRegistry tms = new TilesConfigParser(cfg).readTileMatrixSets();
        return source.parse(cfg, Paths.get("service.properties"), tms, "mtk");
    }

    private static Optional<Tile> get(ZoomRoutedVectorTileSource source, VectorTileLayer layer, String matrix)
            throws Exception {
        return source.getTile(layer, TMS, matrix, 0, 0, List.of(),
                "application/vnd.mapbox-vector-tile", Map.of());
    }

    private static String body(Optional<Tile> tile) throws Exception {
        assertTrue("a zoom-routed layer always answers with a tile", tile.isPresent());
        return new String(tile.get().getBytes(), java.nio.charset.StandardCharsets.UTF_8);
    }

    @Test
    public void dispatchesByZoomToTheCoveringBacking() throws Exception {
        Properties p = baseProps();
        p.setProperty("tiles.layers.mtk.backings", "coarse, fine");
        p.setProperty("tiles.layers.mtk.coarse.type", StubVectorTileSource.TYPE);
        p.setProperty("tiles.layers.mtk.coarse.zoom", "0-1");
        p.setProperty("tiles.layers.mtk.coarse.stub.body", "COARSE");
        p.setProperty("tiles.layers.mtk.fine.type", StubVectorTileSource.TYPE);
        p.setProperty("tiles.layers.mtk.fine.zoom", "2");
        p.setProperty("tiles.layers.mtk.fine.stub.body", "FINE");

        try (ZoomRoutedVectorTileSource source = new ZoomRoutedVectorTileSource()) {
            VectorTileLayer layer = parse(source, p);
            assertEquals("COARSE", body(get(source, layer, "0")));
            assertEquals("COARSE", body(get(source, layer, "1")));
            assertEquals("FINE", body(get(source, layer, "2")));
        }
    }

    @Test
    public void theFirstCoveringBackingWins() throws Exception {
        Properties p = baseProps();
        p.setProperty("tiles.layers.mtk.backings", "new, old");
        p.setProperty("tiles.layers.mtk.new.type", StubVectorTileSource.TYPE);
        p.setProperty("tiles.layers.mtk.new.zoom", "0-2");
        p.setProperty("tiles.layers.mtk.new.stub.body", "NEW");
        p.setProperty("tiles.layers.mtk.old.type", StubVectorTileSource.TYPE);
        p.setProperty("tiles.layers.mtk.old.zoom", "0-2");
        p.setProperty("tiles.layers.mtk.old.stub.body", "OLD");

        try (ZoomRoutedVectorTileSource source = new ZoomRoutedVectorTileSource()) {
            VectorTileLayer layer = parse(source, p);
            assertEquals("NEW", body(get(source, layer, "1")));
        }
    }

    @Test
    public void anUncoveredZoomIsEmptyRatherThanAbsent() throws Exception {
        Properties p = baseProps();
        p.setProperty("tiles.layers.mtk.backings", "fine");
        p.setProperty("tiles.layers.mtk.fine.type", StubVectorTileSource.TYPE);
        p.setProperty("tiles.layers.mtk.fine.zoom", "2");
        p.setProperty("tiles.layers.mtk.fine.stub.body", "FINE");

        try (ZoomRoutedVectorTileSource source = new ZoomRoutedVectorTileSource()) {
            VectorTileLayer layer = parse(source, p);
            assertEquals("", body(get(source, layer, "0")));
            assertEquals("", body(get(source, layer, "1")));
            assertEquals("FINE", body(get(source, layer, "2")));
        }
    }

    @Test
    public void aBackingWithNoTileIsEmptyAndDoesNotFallThrough() throws Exception {
        Properties p = baseProps();
        p.setProperty("tiles.layers.mtk.backings", "sparse, fallback");
        p.setProperty("tiles.layers.mtk.sparse.type", StubVectorTileSource.TYPE);
        p.setProperty("tiles.layers.mtk.sparse.zoom", "0-2");
        p.setProperty("tiles.layers.mtk.sparse.stub.absent", "true");
        p.setProperty("tiles.layers.mtk.fallback.type", StubVectorTileSource.TYPE);
        p.setProperty("tiles.layers.mtk.fallback.zoom", "0-2");
        p.setProperty("tiles.layers.mtk.fallback.stub.body", "FALLBACK");

        try (ZoomRoutedVectorTileSource source = new ZoomRoutedVectorTileSource()) {
            VectorTileLayer layer = parse(source, p);
            assertEquals("", body(get(source, layer, "1")));
        }
    }

    @Test
    public void mediaTypesAreTheUnionOfTheBackings() throws Exception {
        Properties p = baseProps();
        p.setProperty("tiles.layers.mtk.backings", "a, b");
        p.setProperty("tiles.layers.mtk.a.type", StubVectorTileSource.TYPE);
        p.setProperty("tiles.layers.mtk.a.zoom", "0-1");
        p.setProperty("tiles.layers.mtk.a.format", "application/vnd.mapbox-vector-tile");
        p.setProperty("tiles.layers.mtk.b.type", StubVectorTileSource.TYPE);
        p.setProperty("tiles.layers.mtk.b.zoom", "2");
        p.setProperty("tiles.layers.mtk.b.format", "application/x-protobuf");

        try (ZoomRoutedVectorTileSource source = new ZoomRoutedVectorTileSource()) {
            assertEquals(List.of("application/vnd.mapbox-vector-tile", "application/x-protobuf"),
                    parse(source, p).getMediaTypes());
        }
    }

    @Test
    public void backingsInheritTheLayersTileMatrixSets() throws Exception {
        Properties p = baseProps();
        p.setProperty("tiles.layers.mtk.backings", "only");
        p.setProperty("tiles.layers.mtk.only.type", StubVectorTileSource.TYPE);
        p.setProperty("tiles.layers.mtk.only.zoom", "0-2");
        p.setProperty("tiles.layers.mtk.only.stub.body", "X");

        try (ZoomRoutedVectorTileSource source = new ZoomRoutedVectorTileSource()) {
            VectorTileLayer layer = parse(source, p);
            assertEquals(List.of(TMS), layer.getTileMatrixSetIds());
            assertEquals("X", body(get(source, layer, "0")));
        }
    }

    @Test
    public void constructingDoesNotResolveBackingSources() throws Exception {
        try (ZoomRoutedVectorTileSource source = new ZoomRoutedVectorTileSource()) {
            assertEquals(ZoomRoutedVectorTileSource.TYPE, source.getType());
        }
    }

    @Test
    public void layersShareTheBackingSourceInstances() throws Exception {
        Properties p = baseProps();
        p.setProperty("tiles.layers", "mtk, other");
        p.setProperty("tiles.layers.other.type", ZoomRoutedVectorTileSource.TYPE);
        p.setProperty("tiles.layers.other.tileMatrixSets", TMS);
        for (String layer : new String[] { "mtk", "other" }) {
            p.setProperty("tiles.layers." + layer + ".backings", "only");
            p.setProperty("tiles.layers." + layer + ".only.type", StubVectorTileSource.TYPE);
            p.setProperty("tiles.layers." + layer + ".only.zoom", "0-2");
            p.setProperty("tiles.layers." + layer + ".only.stub.body", "X");
        }

        try (ZoomRoutedVectorTileSource source = new ZoomRoutedVectorTileSource()) {
            HakunaConfigParser cfg = new HakunaConfigParser(p);
            TileMatrixSetRegistry tms = new TilesConfigParser(cfg).readTileMatrixSets();
            VectorTileLayer a = source.parse(cfg, Paths.get("service.properties"), tms, "mtk");
            VectorTileLayer b = source.parse(cfg, Paths.get("service.properties"), tms, "other");
            assertSame("both layers' backings come from one source instance",
                    backingSourceOf(source, a), backingSourceOf(source, b));
        }
    }

    private static VectorTileSource backingSourceOf(ZoomRoutedVectorTileSource source, VectorTileLayer layer)
            throws Exception {
        Optional<Tile> tile = source.getTile(layer, TMS, "0", 0, 0, List.of(),
                "application/vnd.mapbox-vector-tile", Map.of());
        assertTrue("the backing served the tile", tile.isPresent());
        return layer.getSource();
    }

    @Test
    public void noBackingsIsRejected() throws Exception {
        try (ZoomRoutedVectorTileSource source = new ZoomRoutedVectorTileSource()) {
            parse(source, baseProps());
            fail("a zoom-routed layer with no backings should be rejected");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("backings"));
        }
    }

    @Test
    public void aBackingWithoutAZoomRangeIsRejected() throws Exception {
        Properties p = baseProps();
        p.setProperty("tiles.layers.mtk.backings", "b");
        p.setProperty("tiles.layers.mtk.b.type", StubVectorTileSource.TYPE);

        try (ZoomRoutedVectorTileSource source = new ZoomRoutedVectorTileSource()) {
            parse(source, p);
            fail("a backing with no zoom range should be rejected");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("zoom"));
        }
    }

    @Test
    public void anInvertedZoomRangeIsRejected() throws Exception {
        Properties p = baseProps();
        p.setProperty("tiles.layers.mtk.backings", "b");
        p.setProperty("tiles.layers.mtk.b.type", StubVectorTileSource.TYPE);
        p.setProperty("tiles.layers.mtk.b.zoom", "9-2");

        try (ZoomRoutedVectorTileSource source = new ZoomRoutedVectorTileSource()) {
            parse(source, p);
            fail("an inverted zoom range should be rejected");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("inverted"));
        }
    }

    @Test
    public void anUnknownBackingTypeIsRejected() throws Exception {
        Properties p = baseProps();
        p.setProperty("tiles.layers.mtk.backings", "b");
        p.setProperty("tiles.layers.mtk.b.type", "eiolemassa");
        p.setProperty("tiles.layers.mtk.b.zoom", "0-2");

        try (ZoomRoutedVectorTileSource source = new ZoomRoutedVectorTileSource()) {
            parse(source, p);
            fail("an unknown backing type should be rejected");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("eiolemassa"));
        }
    }

    @Test
    public void aZoomRoutedBackingIsRejected() throws Exception {
        Properties p = baseProps();
        p.setProperty("tiles.layers.mtk.backings", "b");
        p.setProperty("tiles.layers.mtk.b.type", ZoomRoutedVectorTileSource.TYPE);
        p.setProperty("tiles.layers.mtk.b.zoom", "0-2");

        try (ZoomRoutedVectorTileSource source = new ZoomRoutedVectorTileSource()) {
            parse(source, p);
            fail("a zoom-routed-vector backing should be rejected");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), e.getMessage().contains(ZoomRoutedVectorTileSource.TYPE));
        }
    }

    @Test
    public void aBackingWithoutATypeIsRejected() throws Exception {
        Properties p = baseProps();
        p.setProperty("tiles.layers.mtk.backings", "b");
        p.setProperty("tiles.layers.mtk.b.zoom", "0-2");

        try (ZoomRoutedVectorTileSource source = new ZoomRoutedVectorTileSource()) {
            parse(source, p);
            fail("a backing with no type should be rejected");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("type"));
        }
    }

}
