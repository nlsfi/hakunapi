package fi.nls.hakunapi.tiles.generator;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.zip.GZIPInputStream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.config.HakunaConfigParser;
import fi.nls.hakunapi.gpkg.GPKG;
import fi.nls.hakunapi.gpkg.GPKGSpatialRefSystems;
import fi.nls.hakunapi.tiles.TileBody;
import fi.nls.hakunapi.tiles.TileMatrix;
import fi.nls.hakunapi.tiles.TileMatrixSet;
import fi.nls.hakunapi.tiles.source.vectortile.JavaVectorTileGenerator;
import fi.nls.hakunapi.tiles.source.vectortile.TileContext;
import fi.nls.hakunapi.tiles.source.vectortile.VectorTileGenerator;
import fi.nls.hakunapi.tiles.source.vectortile.VectorTileGeneratorSource;

public class PyramidGeneratorTest {

    private static final int SRID = 3067;

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private static TileMatrixSet scheme() {
        List<TileMatrix> matrices = new ArrayList<>(4);
        matrices.add(new TileMatrix("0", 1, 4.0, 0.0, 1024.0, 256, 256, 1, 1));
        matrices.add(new TileMatrix("1", 1, 2.0, 0.0, 1024.0, 256, 256, 2, 2));
        matrices.add(new TileMatrix("2", 1, 1.0, 0.0, 1024.0, 256, 256, 4, 4));
        matrices.add(new TileMatrix("3", 1, 0.5, 0.0, 1024.0, 256, 256, 8, 8));
        return new TileMatrixSet("TEST", "Test scheme", "http://example.org/crs/3067", SRID, matrices);
    }

    private static GeneratorControl control(int minZoom, int maxZoom, String... collectionIds) {
        ZoomRange range = new ZoomRange("all", minZoom, maxZoom, List.of(collectionIds), Map.of());
        return new GeneratorControl("TEST", List.of(range));
    }

    private static Map<String, List<FeatureType>> collectionsOf(GeneratorControl control,
            StubCollection... collections) {
        Map<String, FeatureType> byId = new LinkedHashMap<>();
        for (StubCollection c : collections) {
            byId.put(c.getName(), c);
        }
        Map<String, List<FeatureType>> byRange = new LinkedHashMap<>();
        for (ZoomRange range : control.getRanges()) {
            List<FeatureType> resolved = new ArrayList<>(range.collectionIds().size());
            for (String id : range.collectionIds()) {
                resolved.add(byId.get(id));
            }
            byRange.put(range.name(), resolved);
        }
        return byRange;
    }

    private static TileSources sourcesOf(GeneratorControl control, String... keyValues) {
        Properties p = new Properties();
        for (int i = 0; i < keyValues.length; i += 2) {
            p.setProperty(keyValues[i], keyValues[i + 1]);
        }
        return TileSources.read(new HakunaConfigParser(p), control);
    }

    private Path generate(StubCollection[] stubs, GeneratorControl control, TileSources sources,
            VectorTileGenerator generator, int minZoom, int maxZoom) throws Exception {
        Path out = tmp.newFolder().toPath().resolve("tiles.gpkg");
        TileMatrixSet tms = scheme();
        List<String> tables = sources.getSourceIds();
        try (Connection c = open(out)) {
            GPKG.init(c);
            GPKG.initTiles(c);
            GPKG.insertSpatialRefSys(c, GPKGSpatialRefSystems.get(SRID));
            PyramidTables.createTables(c, tables, tms, minZoom, maxZoom);

            PyramidGenerator pyramid = new PyramidGenerator(control, sources, tms, generator,
                    VectorTileGeneratorSource.DEFAULT_MEDIA_TYPE, collectionsOf(control, stubs));
            try (PyramidTables pyramidTables = new PyramidTables(c, tables)) {
                pyramid.generate(pyramidTables, minZoom, maxZoom, 0, 0, 1024, 1024, 1);
            }
        }
        return out;
    }

    private Path generate(StubCollection[] stubs, GeneratorControl control, TileSources sources,
            int minZoom, int maxZoom) throws Exception {
        return generate(stubs, control, sources, new JavaVectorTileGenerator(), minZoom, maxZoom);
    }

    private static VectorTileGenerator perTileOnly(VectorTileGenerator delegate) {
        return new VectorTileGenerator() {
            @Override
            public String getType() {
                return delegate.getType();
            }

            @Override
            public Optional<TileBody> generate(TileContext ctx, List<FeatureType> collections,
                    String mediaType, Map<String, String> requestParams) throws Exception {
                return delegate.generate(ctx, collections, mediaType, requestParams);
            }
        };
    }

    private static StubCollection scattered() {
        return new StubCollection("tieviiva", SRID)
                .add(1, 100, 900, 12132).add(2, 500, 500, 12141).add(3, 900, 100, 12132)
                .add(4, 256, 256, 12132).add(5, 512, 512, 12141).add(6, 300, 700, 12132);
    }

    private static Connection open(Path path) throws Exception {
        return DriverManager.getConnection("jdbc:sqlite:" + path.toAbsolutePath());
    }

    @Test
    public void blockOutputEqualsPerTileOutput() throws Exception {
        GeneratorControl control = control(0, 3, "tieviiva");
        StubCollection blockStub = scattered();
        StubCollection tileStub = scattered();
        JavaVectorTileGenerator java = new JavaVectorTileGenerator();
        assertTrue(java.supportsBlocks());

        Path blocks = generate(new StubCollection[] { blockStub }, control,
                sourcesOf(control), java, 0, 3);
        Path perTile = generate(new StubCollection[] { tileStub }, control,
                sourcesOf(control), perTileOnly(java), 0, 3);

        Map<String, String> expected = readAll(perTile, "tieviiva");
        assertFalse(expected.isEmpty());
        assertEquals(expected, readAll(blocks, "tieviiva"));
        assertTrue(blockStub.queries < tileStub.queries);
    }

    @Test
    public void emptyTilesWriteNoRowAndEachCollectionKeepsItsOwnTable() throws Exception {
        StubCollection tieviiva = new StubCollection("tieviiva", SRID).add(1, 100, 900, 12132);
        StubCollection rakennus = new StubCollection("rakennus", SRID);
        GeneratorControl control = control(2, 2, "tieviiva", "rakennus");

        Path out = generate(new StubCollection[] { tieviiva, rakennus }, control,
                sourcesOf(control), 2, 2);

        assertEquals(1, readAll(out, "tieviiva").size());
        assertTrue(readAll(out, "rakennus").isEmpty());
        byte[] tile = readTile(out, "tieviiva", 2, 0, 0);
        assertNotNull(tile);
        assertTrue(MvtTile.hasFeatures(tile));
    }

    @Test
    public void aGroupedTileIsTheGzippedConcatenationOfItsCollectionsAndEmptySourcesWriteNoRow()
            throws Exception {
        StubCollection jarvi = new StubCollection("jarvi", SRID).add(1, 100, 900, 36200);
        StubCollection meri = new StubCollection("meri", SRID).add(2, 120, 880, 36211);
        StubCollection suo = new StubCollection("suo", SRID);
        GeneratorControl control = control(2, 2, "jarvi", "meri", "suo");
        StubCollection[] stubs = { jarvi, meri, suo };

        Path perCollection = generate(stubs, control, sourcesOf(control), 2, 2);
        Path perSource = generate(stubs, control, sourcesOf(control, "sources", "vesi, maasto",
                "vesi.collections", "jarvi, meri", "maasto.collections", "suo"), 2, 2);

        byte[] one = readTile(perCollection, "jarvi", 2, 0, 0);
        byte[] two = readTile(perCollection, "meri", 2, 0, 0);
        byte[] concatenated = new byte[one.length + two.length];
        System.arraycopy(one, 0, concatenated, 0, one.length);
        System.arraycopy(two, 0, concatenated, one.length, two.length);

        assertArrayEquals(concatenated, readTile(perSource, "vesi", 2, 0, 0));
        byte[] stored = readStoredTile(perSource, "vesi", 2, 0, 0);
        assertEquals((byte) 0x1f, stored[0]);
        assertEquals((byte) 0x8b, stored[1]);
        assertTrue(readAll(perSource, "maasto").isEmpty());
    }

    @Test
    public void aFailedRenderKeepsTheWrittenTilesAndSkipsFinish() throws Exception {
        GeneratorControl control = control(0, 3, "tieviiva");
        VectorTileGenerator java = new JavaVectorTileGenerator();
        VectorTileGenerator failing = new VectorTileGenerator() {
            @Override
            public String getType() {
                return java.getType();
            }

            @Override
            public Optional<TileBody> generate(TileContext ctx, List<FeatureType> collections,
                    String mediaType, Map<String, String> requestParams) throws Exception {
                if (ctx.resolution() <= 0.5) {
                    throw new IllegalStateException("boom");
                }
                return java.generate(ctx, collections, mediaType, requestParams);
            }
        };
        Path out = tmp.newFolder().toPath().resolve("tiles.gpkg");
        TileSources sources = sourcesOf(control);
        List<String> tables = sources.getSourceIds();
        TileMatrixSet tms = scheme();
        try (Connection c = open(out)) {
            GPKG.init(c);
            GPKG.initTiles(c);
            GPKG.insertSpatialRefSys(c, GPKGSpatialRefSystems.get(SRID));
            PyramidTables.createTables(c, tables, tms, 0, 3);
            PyramidGenerator pyramid = new PyramidGenerator(control, sources, tms, failing,
                    VectorTileGeneratorSource.DEFAULT_MEDIA_TYPE,
                    collectionsOf(control, scattered()));
            try (PyramidTables pyramidTables = new PyramidTables(c, tables)) {
                ExecutionException e = assertThrows(ExecutionException.class,
                        () -> pyramid.generate(pyramidTables, 0, 3, 0, 0, 1024, 1024, 1));
                assertEquals("boom", e.getCause().getMessage());
            }
        }
        Map<String, String> kept = readAll(out, "tieviiva");
        assertFalse(kept.isEmpty());
        assertFalse(kept.keySet().stream().anyMatch(k -> k.startsWith("3/")));
    }

    @Test
    public void tileMetadataIsCompleteAndInitTilesIsIdempotent() throws Exception {
        Path out = tmp.newFolder().toPath().resolve("tiles.gpkg");
        try (Connection c = open(out)) {
            GPKG.init(c);
            GPKG.initTiles(c);
            GPKG.initTiles(c);
            GPKG.insertSpatialRefSys(c, GPKGSpatialRefSystems.get(SRID));
            PyramidTables.createTables(c, List.of("tieviiva"), scheme(), 1, 2);
            assertTrue(PyramidTables.existingTables(c, List.of("tieviiva", "x")).equals(List.of("tieviiva")));
            try (Statement st = c.createStatement()) {
                try (ResultSet rs = st.executeQuery(
                        "SELECT zoom_level, matrix_width FROM gpkg_tile_matrix ORDER BY zoom_level")) {
                    assertTrue(rs.next());
                    assertEquals(1, rs.getInt(1));
                    assertEquals(2, rs.getInt(2));
                    assertTrue(rs.next());
                    assertEquals(4, rs.getInt(2));
                    assertFalse(rs.next());
                }
                try (ResultSet rs = st.executeQuery(
                        "SELECT min_x, max_x, max_y FROM gpkg_contents WHERE table_name = 'tieviiva'")) {
                    assertTrue(rs.next());
                    assertEquals(0.0, rs.getDouble(1), 0);
                    assertEquals(1024.0, rs.getDouble(2), 0);
                    assertEquals(1024.0, rs.getDouble(3), 0);
                }
                try (ResultSet rs = st.executeQuery(
                        "SELECT extension_name, column_name FROM gpkg_extensions WHERE table_name = 'tieviiva'")) {
                    assertTrue(rs.next());
                    assertEquals("hakunapi_mvt", rs.getString(1));
                    assertEquals("tile_data", rs.getString(2));
                }
            }
        }
    }

    private static Map<String, String> readAll(Path gpkg, String table) throws Exception {
        Map<String, String> out = new LinkedHashMap<>();
        try (Connection c = open(gpkg);
                PreparedStatement ps = c.prepareStatement(
                        "SELECT zoom_level, tile_row, tile_column, hex(tile_data) FROM "
                        + GPKG.quote(table) + " ORDER BY zoom_level, tile_row, tile_column");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.put(rs.getInt(1) + "/" + rs.getInt(2) + "/" + rs.getInt(3), rs.getString(4));
            }
        }
        return out;
    }

    private static byte[] readTile(Path gpkg, String table, int zoom, int row, int col) throws Exception {
        byte[] stored = readStoredTile(gpkg, table, zoom, row, col);
        if (stored == null) {
            return null;
        }
        try (GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(stored))) {
            return in.readAllBytes();
        }
    }

    private static byte[] readStoredTile(Path gpkg, String table, int zoom, int row, int col) throws Exception {
        try (Connection c = open(gpkg);
                PreparedStatement ps = c.prepareStatement("SELECT tile_data FROM " + GPKG.quote(table)
                        + " WHERE zoom_level = ? AND tile_row = ? AND tile_column = ?")) {
            ps.setInt(1, zoom);
            ps.setInt(2, row);
            ps.setInt(3, col);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBytes(1) : null;
            }
        }
    }

}
