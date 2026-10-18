package fi.nls.hakunapi.tiles.generator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import fi.nls.hakunapi.core.projection.ProjectionTransformer;
import fi.nls.hakunapi.core.projection.ProjectionTransformerFactory;
import fi.nls.hakunapi.gpkg.GPKG;

/** The WGS84 extent of the tiles a pyramid table holds, not of the tiling scheme it was generated for. */
public class PyramidBounds {

    public static final String PROJ_PROPERTY = "hakunapi.proj";
    public static final String DEFAULT_PROJ = "fi.nls.hakunapi.proj.gt.GeoToolsProjectionTransformerFactory";

    private static final int WGS84 = 4326;

    private PyramidBounds() {
        // Block init
    }

    /** Tiles of the finest zoom only: a tile at a coarse zoom spans the whole scheme and says nothing about the data. */
    public static double[] wgs84(Connection c, String table, boolean flip) throws Exception {
        int zoom;
        try (Statement st = c.createStatement();
                ResultSet rs = st.executeQuery("SELECT MAX(zoom_level) FROM " + GPKG.quote(table))) {
            rs.next();
            zoom = rs.getInt(1);
        }

        long minCol, maxCol, minRow, maxRow;
        String sql = "SELECT MIN(tile_column), MAX(tile_column), MIN(tile_row), MAX(tile_row) FROM "
                + GPKG.quote(table) + " WHERE zoom_level = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, zoom);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                minCol = rs.getLong(1);
                maxCol = rs.getLong(2);
                minRow = rs.getLong(3);
                maxRow = rs.getLong(4);
            }
        }

        int srid;
        double originX, originY, spanX, spanY;
        long matrixHeight;
        sql = "SELECT s.srs_id, s.min_x, s.max_y, m.matrix_height, m.tile_width * m.pixel_x_size,"
                + " m.tile_height * m.pixel_y_size FROM gpkg_tile_matrix_set s"
                + " JOIN gpkg_tile_matrix m ON m.table_name = s.table_name"
                + " WHERE s.table_name = ? AND m.zoom_level = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, table);
            ps.setInt(2, zoom);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Table " + table
                            + " has no tile matrix for zoom " + zoom + "; pass --bbox");
                }
                srid = rs.getInt(1);
                originX = rs.getDouble(2);
                originY = rs.getDouble(3);
                matrixHeight = rs.getLong(4);
                spanX = rs.getDouble(5);
                spanY = rs.getDouble(6);
            }
        }

        if (flip) {
            long top = matrixHeight - 1 - maxRow;
            maxRow = matrixHeight - 1 - minRow;
            minRow = top;
        }

        double minX = originX + minCol * spanX;
        double maxX = originX + (maxCol + 1) * spanX;
        double maxY = originY - minRow * spanY;
        double minY = originY - (maxRow + 1) * spanY;

        // Corners and edge midpoints: a projected rectangle is not a rectangle in lon/lat
        double midX = (minX + maxX) / 2;
        double midY = (minY + maxY) / 2;
        double[] xy = { minX, minY, midX, minY, maxX, minY, maxX, midY, maxX, maxY, midX, maxY, minX, maxY, minX, midY };
        if (srid != WGS84) {
            toCRS84(srid).transformInPlace(xy, 0, xy.length / 2);
        }

        double[] bbox = { Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY };
        for (int i = 0; i < xy.length; i++) {
            if (!Double.isFinite(xy[i])) {
                throw new IllegalArgumentException("The pyramid's extent lies outside the projection's valid area"
                        + " in EPSG:" + srid + "; pass --bbox");
            }
        }
        for (int i = 0; i < xy.length; i += 2) {
            bbox[0] = Math.min(bbox[0], xy[i]);
            bbox[1] = Math.min(bbox[1], xy[i + 1]);
            bbox[2] = Math.max(bbox[2], xy[i]);
            bbox[3] = Math.max(bbox[3], xy[i + 1]);
        }
        return bbox;
    }

    private static ProjectionTransformer toCRS84(int srid) {
        String className = System.getProperty(PROJ_PROPERTY, DEFAULT_PROJ);
        try {
            ProjectionTransformerFactory factory = (ProjectionTransformerFactory) Class.forName(className)
                    .getDeclaredConstructor().newInstance();
            return factory.toCRS84(srid);
        } catch (Exception | LinkageError e) {
            throw new IllegalArgumentException("Cannot convert the pyramid's EPSG:" + srid
                    + " extent to WGS84 with " + className + " (" + e + "); put a projection factory on the"
                    + " classpath (-D" + PROJ_PROPERTY + "=<class>) or pass --bbox");
        }
    }

}
