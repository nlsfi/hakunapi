package fi.nls.hakunapi.gpkg.function;

import java.util.Arrays;

import org.locationtech.jts.io.InStream;

public class ByteArrayViewInStream implements InStream {

    private final byte[] buf;
    private int pos;
    private final int end;

    public ByteArrayViewInStream(byte[] buf) {
        this(buf, 0, buf.length);
    }

    public ByteArrayViewInStream(byte[] buf, int off) {
        this(buf, off, buf.length - off);

    }

    public ByteArrayViewInStream(byte[] buf, int off, int length) {
        this.buf = buf;
        this.pos = off;
        this.end = off + length;
    }

    /**
     * Reads up to <tt>buf.length</tt> bytes from the stream into the given byte
     * buffer.
     * 
     * @param out the buffer to receive the bytes
     * @return the number of bytes read, or -1 if at end-of-file
     */
    public int read(final byte[] out) {
        int n = out.length;
        if (pos + n > end) {
            n = end - pos;
            Arrays.fill(out, n, out.length - n, (byte) 0);
        }
        System.arraycopy(buf, pos, out, 0, n);
        pos += n;
        return n;
    }

}
