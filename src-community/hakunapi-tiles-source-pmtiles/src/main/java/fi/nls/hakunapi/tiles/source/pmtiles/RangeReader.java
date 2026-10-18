package fi.nls.hakunapi.tiles.source.pmtiles;

public interface RangeReader extends AutoCloseable {
    /** Offset and length in bytes; the returned array is owned by the caller. */
    byte[] readRange(long offset, int length) throws Exception;

    @Override
    default void close() throws Exception {
    }
}
