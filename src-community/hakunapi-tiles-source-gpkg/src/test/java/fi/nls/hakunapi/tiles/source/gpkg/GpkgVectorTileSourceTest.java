package fi.nls.hakunapi.tiles.source.gpkg;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import fi.nls.hakunapi.core.config.HakunaConfigParser;
import fi.nls.hakunapi.tiles.Tile;
import fi.nls.hakunapi.tiles.TileMatrixSetRegistry;
import fi.nls.hakunapi.tiles.VectorTileLayer;
import fi.nls.hakunapi.tiles.config.TilesConfigParser;

public class GpkgVectorTileSourceTest {

    private static final String TMS = "TEST";
    private static final int SRID = 3067;

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private Path gpkg;

    private static byte[] tileBytes(int zoom, int row, int col) {
        return new byte[] { 0x1a, (byte) zoom, (byte) row, (byte) col };
    }

    @Before
    public void writePyramid() throws Exception {
        gpkg = tmp.newFolder().toPath().resolve("tiles.gpkg");
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + gpkg.toAbsolutePath());
                Statement st = c.createStatement()) {
            st.executeUpdate("CREATE TABLE gpkg_contents (table_name TEXT NOT NULL PRIMARY KEY,"
                    + " data_type TEXT NOT NULL, identifier TEXT, description TEXT,"
                    + " last_change DATETIME, min_x DOUBLE, min_y DOUBLE, max_x DOUBLE, max_y DOUBLE,"
                    + " srs_id INTEGER)");
            st.executeUpdate("CREATE TABLE gpkg_tile_matrix (table_name TEXT NOT NULL, zoom_level INTEGER NOT NULL,"
                    + " matrix_width INTEGER, matrix_height INTEGER, tile_width INTEGER, tile_height INTEGER,"
                    + " pixel_x_size DOUBLE, pixel_y_size DOUBLE)");
            st.executeUpdate("CREATE TABLE mvt (id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + " zoom_level INTEGER NOT NULL, tile_column INTEGER NOT NULL, tile_row INTEGER NOT NULL,"
                    + " tile_data BLOB NOT NULL, UNIQUE (zoom_level, tile_column, tile_row))");
            st.executeUpdate("INSERT INTO gpkg_contents (table_name, data_type, identifier,"
                    + " min_x, min_y, max_x, max_y, srs_id)"
                    + " VALUES ('mvt','tiles','mvt',0,0,1024,1024," + SRID + ")");
            st.executeUpdate("CREATE TABLE gpkg_tile_matrix_set (table_name TEXT NOT NULL PRIMARY KEY,"
                    + " srs_id INTEGER NOT NULL, min_x DOUBLE, min_y DOUBLE, max_x DOUBLE, max_y DOUBLE)");
            st.executeUpdate("INSERT INTO gpkg_tile_matrix_set VALUES ('mvt'," + SRID + ",0,0,1024,1024)");
            for (int[] m : new int[][] { { 0, 1, 4 }, { 1, 2, 2 }, { 2, 4, 1 } }) {
                st.executeUpdate("INSERT INTO gpkg_tile_matrix VALUES ('mvt'," + m[0] + "," + m[1] + ","
                        + m[1] + ",256,256," + m[2] + "," + m[2] + ")");
            }

            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO mvt (zoom_level, tile_column, tile_row, tile_data) VALUES (?,?,?,?)")) {
                for (int[] a : new int[][] { { 0, 0, 0 }, { 1, 0, 0 }, { 1, 1, 1 }, { 2, 3, 2 } }) {
                    ps.setInt(1, a[0]);
                    ps.setInt(2, a[1]);
                    ps.setInt(3, a[2]);
                    ps.setBytes(4, tileBytes(a[0], a[2], a[1]));
                    ps.executeUpdate();
                }
            }
        }
    }

    private Properties baseProps() {
        Properties p = new Properties();
        p.setProperty("tiles.layers", "mtk");
        p.setProperty("tiles.layers.mtk.type", GpkgVectorTileSource.TYPE);
        p.setProperty("tiles.layers.mtk.tileMatrixSets", TMS);
        p.setProperty("tiles.layers.mtk.gpkg.path", gpkg.getFileName().toString());
        p.setProperty("tiles.layers.mtk.gpkg.table", "mvt");

        p.setProperty("tileMatrixSets", TMS);
        p.setProperty("tileMatrixSets." + TMS + ".srid", Integer.toString(SRID));
        p.setProperty("tileMatrixSets." + TMS + ".bbox", "0,0,1024,1024");
        p.setProperty("tileMatrixSets." + TMS + ".resolutions", "4,2,1");
        return p;
    }

    private VectorTileLayer parse(GpkgVectorTileSource source, Properties p) throws Exception {
        HakunaConfigParser cfg = new HakunaConfigParser(p);
        TileMatrixSetRegistry tms = new TilesConfigParser(cfg).readTileMatrixSets();
        return source.parse(cfg, gpkg.resolveSibling("service.properties"), tms, "mtk");
    }

    private static Optional<Tile> get(GpkgVectorTileSource source, VectorTileLayer layer,
            String matrix, long row, long col) throws Exception {
        return source.getTile(layer, TMS, matrix, row, col, Collections.emptyList(),
                GpkgVectorTileSource.DEFAULT_MEDIA_TYPE, Map.of());
    }

    @Test
    public void servesTileBytesUnchanged() throws Exception {
        try (GpkgVectorTileSource source = new GpkgVectorTileSource()) {
            VectorTileLayer layer = parse(source, baseProps());
            Optional<Tile> tile = get(source, layer, "0", 0, 0);
            assertTrue(tile.isPresent());
            assertArrayEquals(tileBytes(0, 0, 0), tile.get().getBytes());
            assertEquals("application/vnd.mapbox-vector-tile", tile.get().getMediaType());
        }
    }

    @Test
    public void missingTileReturnsEmpty() throws Exception {
        try (GpkgVectorTileSource source = new GpkgVectorTileSource()) {
            VectorTileLayer layer = parse(source, baseProps());
            assertFalse(get(source, layer, "2", 0, 0).isPresent());
        }
    }

    @Test
    public void gzipTileGetsGzipEncodingWhenNoneConfigured() throws Exception {
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + gpkg.toAbsolutePath());
                PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO mvt (zoom_level, tile_column, tile_row, tile_data) VALUES (2,0,0,?)")) {
            ps.setBytes(1, new byte[] { 0x1f, (byte) 0x8b, 0x08, 0x00 });
            ps.executeUpdate();
        }
        try (GpkgVectorTileSource source = new GpkgVectorTileSource()) {
            VectorTileLayer layer = parse(source, baseProps());
            Tile tile = get(source, layer, "2", 0, 0).get();
            assertEquals("gzip", tile.getContentEncoding());
        }
    }
}
