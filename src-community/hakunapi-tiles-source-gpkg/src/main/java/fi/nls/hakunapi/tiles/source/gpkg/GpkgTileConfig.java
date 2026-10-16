package fi.nls.hakunapi.tiles.source.gpkg;

import java.sql.Connection;
import java.util.Map;

/** The connection is shared per file and closed by the owning source. */
public record GpkgTileConfig(
        Connection connection,
        String tableName,
        String mediaType,
        boolean formatConfigured,
        String contentEncoding,
        Map<String, Map<String, Integer>> zoomByTms,
        String tileSql) {

    public static GpkgTileConfig of(Connection connection, String tableName, String mediaType,
            boolean formatConfigured, String contentEncoding, Map<String, Map<String, Integer>> zoomByTms) {
        String sql = "SELECT tile_data FROM \"" + tableName.replace("\"", "\"\"") + "\""
                + " WHERE zoom_level = ? AND tile_column = ? AND tile_row = ?";
        return new GpkgTileConfig(connection, tableName, mediaType, formatConfigured, contentEncoding,
                zoomByTms, sql);
    }
}
