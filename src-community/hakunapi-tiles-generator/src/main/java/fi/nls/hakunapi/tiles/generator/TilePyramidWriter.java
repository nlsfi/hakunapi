package fi.nls.hakunapi.tiles.generator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.nls.hakunapi.gpkg.GPKG;
import fi.nls.hakunapi.gpkg.GPKGTileMatrix;
import fi.nls.hakunapi.gpkg.GPKGTilesTable;
import fi.nls.hakunapi.tiles.TileMatrix;
import fi.nls.hakunapi.tiles.TileMatrixSet;

/** Batches but never commits: PyramidTables owns the shared connection's transaction. */
public class TilePyramidWriter implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(TilePyramidWriter.class);

    public static final int BATCH_LIMIT = 1024;

    private final Connection c;
    private final String tableName;
    private final PreparedStatement insert;

    private int pending;
    private long written;
    private boolean closed;

    public TilePyramidWriter(Connection c, String tableName) throws SQLException {
        this.c = c;
        this.tableName = tableName;
        this.insert = c.prepareStatement("INSERT INTO " + GPKG.quote(tableName)
                + " (zoom_level, tile_column, tile_row, tile_data) VALUES (?, ?, ?, ?)");
    }

    public static void createTable(Connection c, String tableName, TileMatrixSet tms,
            int minZoom, int maxZoom) throws SQLException {
        List<TileMatrix> matrices = tms.getTileMatrices();
        TileMatrix first = matrices.get(0);

        double minX = first.getPointOfOriginX();
        double maxY = first.getPointOfOriginY();
        double maxX = minX + first.getMatrixWidth() * first.getTileSpanX();
        double minY = maxY - first.getMatrixHeight() * first.getTileSpanY();

        GPKGTilesTable table = new GPKGTilesTable(tableName, tableName,
                "Vector tiles generated from " + tms.getId(), minX, minY, maxX, maxY, tms.getSrid());

        GPKG.createTilePyramidTable(c, tableName);
        GPKG.insertTilesTableEntry(c, table);
        GPKG.insertTileMatrixSet(c, table);

        for (int zoom = minZoom; zoom <= maxZoom; zoom++) {
            TileMatrix tm = matrices.get(zoom);
            GPKG.insertTileMatrix(c, new GPKGTileMatrix(tableName, zoom,
                    tm.getMatrixWidth(), tm.getMatrixHeight(),
                    tm.getTileWidth(), tm.getTileHeight(),
                    tm.getCellSize(), tm.getCellSize()));
        }
    }

    public static boolean tableExists(Connection c, String tableName) throws SQLException {
        String sql = "SELECT 1 FROM gpkg_contents WHERE table_name = ? AND data_type = 'tiles'";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean write(TileAddress address, byte[] tileData) throws SQLException {
        insert.setInt(1, address.zoom());
        insert.setLong(2, address.col());
        insert.setLong(3, address.row());
        insert.setBytes(4, tileData);
        insert.addBatch();
        written++;
        if (++pending >= BATCH_LIMIT) {
            flush();
            return true;
        }
        return false;
    }

    public boolean flush() throws SQLException {
        if (pending == 0) {
            return false;
        }
        insert.executeBatch();
        pending = 0;
        return true;
    }

    public long getWritten() {
        return written;
    }

    public void finish() throws SQLException {
        flush();
        String sql = "UPDATE gpkg_contents SET last_change = strftime('%Y-%m-%dT%H:%M:%fZ','now')"
                + " WHERE table_name = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, tableName);
            ps.executeUpdate();
        }
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        try {
            insert.close();
        } catch (SQLException e) {
            LOG.warn("Failed to close tile insert statement", e);
        }
    }

}
