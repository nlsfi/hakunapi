package fi.nls.hakunapi.tiles.source.gpkg;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.sqlite.SQLiteConfig;

import fi.nls.hakunapi.core.config.HakunaConfigParser;
import fi.nls.hakunapi.tiles.TileMatrix;
import fi.nls.hakunapi.tiles.TileMatrixSet;
import fi.nls.hakunapi.tiles.TileMatrixSetRegistry;

public final class GpkgTiles {

    public static final double DEFAULT_RESOLUTION_TOLERANCE = 0.01;

    public record ZoomLevel(double pixelSize, int tileWidth, int tileHeight) {
    }

    private GpkgTiles() {}

    public static GpkgTileConfig parse(HakunaConfigParser cfg, Path path, TileMatrixSetRegistry tms,
            String layerId, List<String> tmsIds, String defaultMediaType,
            Map<Path, Connection> openConnections) throws Exception {
        String g = "tiles.layers." + layerId + ".gpkg.";

        String filePath = cfg.getRequired(g + "path");
        Path gpkgPath = path != null ? path.resolveSibling(filePath) : Path.of(filePath);
        String table = cfg.get(g + "table", layerId);
        double tolerance = Double.parseDouble(cfg.get(g + "resolutionTolerance",
                Double.toString(DEFAULT_RESOLUTION_TOLERANCE)));

        Path key = gpkgPath.toAbsolutePath();
        Connection cached = openConnections.get(key);
        Connection connection = cached != null ? cached : openReadOnly(gpkgPath);
        try {
            validateTilesTable(connection, table);
            Map<Integer, ZoomLevel> zoomResolutions = readZoomResolutions(connection, table);
            int srid = readSrid(connection, table);
            Map<String, Map<String, Integer>> zoomByTms = mapZooms(tms, zoomResolutions, tolerance,
                    layerId, tmsIds, gpkgPath, table, srid);

            boolean formatConfigured = cfg.get(g + "format") != null;
            String mediaType = cfg.get(g + "format", defaultMediaType);
            if (mediaType == null) {
                mediaType = sniffTableMediaType(connection, table);
            }
            String contentEncoding = readContentEncoding(cfg, g, layerId);
            openConnections.put(key, connection);
            return GpkgTileConfig.of(connection, table, mediaType, formatConfigured, contentEncoding, zoomByTms);
        } catch (Exception e) {
            // a cached connection still serves other layers
            if (cached == null) {
                connection.close();
            }
            throw e;
        }
    }

    private static Map<String, Map<String, Integer>> mapZooms(TileMatrixSetRegistry tms,
            Map<Integer, ZoomLevel> zoomResolutions, double tolerance, String layerId,
            List<String> tmsIds, Path gpkgPath, String table, int srid) {
        Map<String, Map<String, Integer>> zoomByTms = new LinkedHashMap<>();
        for (String tmsId : tmsIds) {
            TileMatrixSet matrixSet = tms.get(tmsId).orElseThrow(() -> new IllegalArgumentException(
                    "tile layer " + layerId + ": unknown tileMatrixSet " + tmsId));
            if (srid != matrixSet.getSrid()) {
                throw new IllegalArgumentException("tile layer " + layerId + ": table " + table + " in "
                        + gpkgPath + " has srs_id " + srid + " but tileMatrixSet " + tmsId + " has srid "
                        + matrixSet.getSrid());
            }
            Map<String, Integer> byMatrix = new LinkedHashMap<>();
            for (TileMatrix matrix : matrixSet.getTileMatrices()) {
                Integer zoom = matchZoom(zoomResolutions, matrix, tolerance);
                if (zoom != null) {
                    byMatrix.put(matrix.getId(), zoom);
                }
            }
            if (byMatrix.isEmpty()) {
                throw new IllegalArgumentException("tile layer " + layerId + ": no zoom level in "
                        + gpkgPath + " table " + table + " matches the resolutions of tileMatrixSet " + tmsId);
            }
            zoomByTms.put(tmsId, byMatrix);
        }
        return zoomByTms;
    }

    /** Returns null when the pyramid holds no tile at that address. */
    public static byte[] readTile(GpkgTileConfig cfg, String tileMatrixSetId, String tileMatrix,
            long tileRow, long tileCol) throws Exception {
        Map<String, Integer> byMatrix = cfg.zoomByTms().get(tileMatrixSetId);
        if (byMatrix == null) {
            return null;
        }
        Integer zoom = byMatrix.get(tileMatrix);
        if (zoom == null) {
            return null;
        }

        try (PreparedStatement ps = cfg.connection().prepareStatement(cfg.tileSql())) {
            ps.setInt(1, zoom);
            ps.setLong(2, tileCol);
            ps.setLong(3, tileRow);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBytes(1) : null;
            }
        }
    }

    static void closeAll(Map<Path, Connection> connections) throws Exception {
        Exception first = null;
        for (Connection c : connections.values()) {
            try {
                c.close();
            } catch (Exception e) {
                if (first == null) {
                    first = e;
                }
            }
        }
        connections.clear();
        if (first != null) {
            throw first;
        }
    }

    public static Connection openReadOnly(Path gpkgPath) throws Exception {
        SQLiteConfig sqlite = new SQLiteConfig();
        sqlite.setReadOnly(true);
        return DriverManager.getConnection("jdbc:sqlite:" + gpkgPath.toAbsolutePath(), sqlite.toProperties());
    }

    public static void validateTilesTable(Connection c, String table) throws Exception {
        String sql = "SELECT 1 FROM gpkg_contents WHERE table_name = ? AND data_type = 'tiles'";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, table);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException(
                            "GeoPackage has no tiles table named '" + table + "' in gpkg_contents");
                }
            }
        }
    }

    public static Map<Integer, ZoomLevel> readZoomResolutions(Connection c, String table) throws Exception {
        String sql = "SELECT zoom_level, pixel_x_size, tile_width, tile_height FROM gpkg_tile_matrix"
                + " WHERE table_name = ?";
        Map<Integer, ZoomLevel> out = new LinkedHashMap<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, table);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.put(rs.getInt("zoom_level"), new ZoomLevel(rs.getDouble("pixel_x_size"),
                            rs.getInt("tile_width"), rs.getInt("tile_height")));
                }
            }
        }
        if (out.isEmpty()) {
            throw new IllegalArgumentException("GeoPackage table '" + table + "' has no gpkg_tile_matrix rows");
        }
        return out;
    }

    public static int readSrid(Connection c, String table) throws Exception {
        String sql = "SELECT srs_id FROM gpkg_tile_matrix_set WHERE table_name = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, table);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException(
                            "GeoPackage table '" + table + "' has no gpkg_tile_matrix_set row");
                }
                return rs.getInt(1);
            }
        }
    }

    public static Integer matchZoom(Map<Integer, ZoomLevel> zoomResolutions, TileMatrix matrix, double tolerance) {
        double cellSize = matrix.getCellSize();
        Integer best = null;
        double bestDiff = Double.MAX_VALUE;
        for (Map.Entry<Integer, ZoomLevel> e : zoomResolutions.entrySet()) {
            ZoomLevel level = e.getValue();
            if (level.tileWidth() != matrix.getTileWidth() || level.tileHeight() != matrix.getTileHeight()) {
                continue;
            }
            double diff = Math.abs(level.pixelSize() - cellSize);
            if (diff <= tolerance * cellSize && diff < bestDiff) {
                bestDiff = diff;
                best = e.getKey();
            }
        }
        return best;
    }

    public static String sniffTableMediaType(Connection c, String table) throws Exception {
        String sql = "SELECT tile_data FROM \"" + table.replace("\"", "\"\"") + "\" LIMIT 1";
        try (PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String sniffed = GpkgFormat.sniff(rs.getBytes(1));
                if (sniffed != null) {
                    return sniffed;
                }
            }
        }
        // PNG is the GeoPackage default tile encoding
        return GpkgFormat.PNG;
    }

    private static String readContentEncoding(HakunaConfigParser cfg, String g, String layerId) {
        String encoding = cfg.get(g + "contentEncoding");
        if (encoding == null || encoding.isBlank()) {
            return null;
        }
        encoding = encoding.trim();
        if (!"gzip".equalsIgnoreCase(encoding)) {
            throw new IllegalArgumentException("tiles.layers." + layerId
                    + ".gpkg.contentEncoding: only 'gzip' is supported, got '" + encoding + "'");
        }
        return "gzip";
    }

}
