package fi.nls.hakunapi.bytes;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

/**
 * The whole file as one segment, which {@link ByteReader#fetch} returns.
 */
abstract class SegmentSourceBase implements ByteSource {

    private final Arena arena;
    private final MemorySegment segment;
    // Stateless, so every stream can share one
    private final ByteReader reader = new Reader();
    private boolean closed;

    /**
     * Owns arena, which segment belongs to
     */
    protected SegmentSourceBase(Arena arena, MemorySegment segment) {
        this.arena = arena;
        this.segment = segment;
    }

    @Override
    public long size() {
        return segment.byteSize();
    }

    @Override
    public ByteReader open() {
        return reader;
    }

    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }
        closed = true;
        arena.close();
    }

    private class Reader implements ByteReader {

        @Override
        public int read(long off, MemorySegment dst, long dstOff, int len) {
            long size = segment.byteSize();
            if (len <= 0 || off >= size) {
                return 0;
            }
            int want = (int) Math.min(len, size - off);
            MemorySegment.copy(segment, off, dst, dstOff, want);
            return want;
        }

        @Override
        public int read(long off, byte[] dst, int dstOff, int len) {
            long size = segment.byteSize();
            if (len <= 0 || off >= size) {
                return 0;
            }
            int want = (int) Math.min(len, size - off);
            MemorySegment.copy(segment, ValueLayout.JAVA_BYTE, off, dst, dstOff, want);
            return want;
        }

        @Override
        public MemorySegment fetch(long off, int len) {
            return off >= 0 && len >= 0 && off + len <= segment.byteSize() ? segment : null;
        }

        @Override
        public void close() {
            // NOP, the segment belongs to the source
        }

    }

}
