package fi.nls.hakunapi.tiles.generator;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import fi.nls.hakunapi.gpkg.GPKG;
import fi.nls.hakunapi.gpkg.GPKGSpatialRefSystems;
import fi.nls.hakunapi.proj.gt.GeoToolsProjectionTransformerFactory;
import fi.nls.hakunapi.tiles.TileMatrix;
import fi.nls.hakunapi.tiles.TileMatrixSet;

public class PyramidBoundsTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private Connection pyramid(int srid, String... tiles) throws Exception {
        List<TileMatrix> matrices = new ArrayList<>();
        for (int z = 0; z < 3; z++) {
            matrices.add(new TileMatrix(String.valueOf(z), 1, 360.0 / 256 / (1 << z), -180.0, 180.0,
                    256, 256, 1 << z, 1 << z));
        }
        Path path = tmp.newFolder().toPath().resolve("p.gpkg");
        Connection c = DriverManager.getConnection("jdbc:sqlite:" + path);
        GPKG.init(c);
        GPKG.initTiles(c);
        GPKG.insertSpatialRefSys(c, GPKGSpatialRefSystems.get(srid, new GeoToolsProjectionTransformerFactory()));
        PyramidTables.createTables(c, List.of("t"), new TileMatrixSet("T", "T", "urn:x", srid, matrices), 0, 2);
        try (Statement st = c.createStatement()) {
            for (String tile : tiles) {
                st.execute("INSERT INTO t (zoom_level, tile_column, tile_row, tile_data) VALUES (" + tile + ", x'00')");
            }
        }
        return c;
    }

    @Test
    public void boundsFollowTheFinestZoomAndTheRowOrigin() throws Exception {
        try (Connection c = pyramid(4326, "0, 0, 0", "2, 2, 1", "2, 3, 1")) {
            assertArrayEquals(new double[] { 0, 0, 180, 90 }, PyramidBounds.wgs84(c, "t", false), 1e-9);
            assertArrayEquals(new double[] { 0, -90, 180, 0 }, PyramidBounds.wgs84(c, "t", true), 1e-9);
        }
    }

    @Test
    public void missingProjectionFactoryAsksForBbox() throws Exception {
        System.setProperty(PyramidBounds.PROJ_PROPERTY, "no.such.Factory");
        try (Connection c = pyramid(3067, "2, 2, 1")) {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> PyramidBounds.wgs84(c, "t", false));
            assertTrue(e.getMessage(), e.getMessage().contains("--bbox"));
        } finally {
            System.clearProperty(PyramidBounds.PROJ_PROPERTY);
        }
    }

}
