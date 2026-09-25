package fi.nls.hakunapi.bytes;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

/**
 * Any source read whole into native memory when constructed. Takes src and
 * closes it once copied, or if copying fails.
 */
public class InMemoryFile extends SegmentSourceBase {

    /**
     * Bytes per read of src; over HTTP each is a request
     */
    private static final int COPY_CHUNK = 8 << 20;

    public InMemoryFile(ByteSource src) throws IOException {
        this(src, Arena.ofShared());
    }

    private InMemoryFile(ByteSource src, Arena arena) throws IOException {
        super(arena, copy(src, arena));
    }

    private static MemorySegment copy(ByteSource src, Arena arena) throws IOException {
        try (src; ByteReader h = src.open()) {
            long size = src.size();
            MemorySegment segment = arena.allocate(size);
            long off = 0;
            while (off < size) {
                int n = h.read(off, segment, off, (int) Math.min(COPY_CHUNK, size - off));
                if (n <= 0) {
                    throw new IOException("Short read at " + off + " of " + size + " bytes");
                }
                off += n;
            }
            return segment;
        } catch (IOException | RuntimeException e) {
            arena.close();
            throw e;
        }
    }

}
