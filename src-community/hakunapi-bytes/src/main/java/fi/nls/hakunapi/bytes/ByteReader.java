package fi.nls.hakunapi.bytes;

import java.io.Closeable;
import java.io.IOException;
import java.lang.foreign.MemorySegment;

/**
 * Not thread-safe
 */
public interface ByteReader extends Closeable {

    public int read(long off, MemorySegment dst, long dstOff, int len) throws IOException;

    public int read(long off, byte[] dst, int dstOff, int len) throws IOException;

    public MemorySegment fetch(long off, int len);

    @Override
    public void close();

}
