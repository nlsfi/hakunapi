package fi.nls.hakunapi.bytes;

import java.io.Closeable;
import java.io.IOException;

/**
 * The bytes of one file, shared by every stream reading it, so thread-safe.
 * Closing the source invalidates its readers.
 */
public interface ByteSource extends Closeable {

    public long size();

    public ByteReader open() throws IOException;

}
