package fi.nls.hakunapi.tiles.generator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.nls.hakunapi.tiles.TileMatrixSet;

public class PyramidTables implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(PyramidTables.class);

    private final Connection c;

    private final Map<String, TilePyramidWriter> writers = new LinkedHashMap<>();

    public PyramidTables(Connection c, List<String> sourceIds) throws SQLException {
        this.c = c;
        try {
            for (String sourceId : sourceIds) {
                writers.put(sourceId, new TilePyramidWriter(c, sourceId));
            }
        } catch (SQLException e) {
            close();
            throw e;
        }
        c.setAutoCommit(false);
    }

    public static void createTables(Connection c, List<String> sourceIds, TileMatrixSet tms,
            int minZoom, int maxZoom) throws SQLException {
        c.setAutoCommit(false);
        try {
            for (String sourceId : sourceIds) {
                TilePyramidWriter.createTable(c, sourceId, tms, minZoom, maxZoom);
            }
            c.commit();
        } catch (SQLException | RuntimeException e) {
            c.rollback();
            throw e;
        } finally {
            c.setAutoCommit(true);
        }
        LOG.info("Created {} pyramid table(s) for zooms {}-{}", sourceIds.size(), minZoom, maxZoom);
    }

    public static List<String> existingTables(Connection c, List<String> sourceIds) throws SQLException {
        List<String> existing = new ArrayList<>();
        for (String sourceId : sourceIds) {
            if (TilePyramidWriter.tableExists(c, sourceId)) {
                existing.add(sourceId);
            }
        }
        return existing;
    }

    public void write(String sourceId, TileAddress address, byte[] tileData) throws SQLException {
        TilePyramidWriter writer = writers.get(sourceId);
        if (writer == null) {
            throw new IllegalArgumentException("No pyramid table open for source " + sourceId);
        }
        if (writer.write(address, tileData)) {
            flush();
        }
    }

    public void flush() throws SQLException {
        boolean any = false;
        for (TilePyramidWriter writer : writers.values()) {
            any |= writer.flush();
        }
        if (any) {
            c.commit();
        }
    }

    public long getWritten() {
        long total = 0;
        for (TilePyramidWriter writer : writers.values()) {
            total += writer.getWritten();
        }
        return total;
    }

    public Map<String, Long> getWrittenBySource() {
        Map<String, Long> byCollection = new LinkedHashMap<>();
        for (Map.Entry<String, TilePyramidWriter> e : writers.entrySet()) {
            byCollection.put(e.getKey(), e.getValue().getWritten());
        }
        return byCollection;
    }

    public void finish() throws SQLException {
        for (TilePyramidWriter writer : writers.values()) {
            writer.finish();
        }
        c.commit();
    }

    @Override
    public void close() {
        for (TilePyramidWriter writer : writers.values()) {
            writer.close();
        }
        writers.clear();
    }

}
