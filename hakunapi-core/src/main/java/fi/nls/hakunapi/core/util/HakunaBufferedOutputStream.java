package fi.nls.hakunapi.core.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Do not synchronize
 */
public final class HakunaBufferedOutputStream extends OutputStream {

    private static final int DEFAULT_SIZE = 8192;

    private final OutputStream out;
    private final byte[] buf;
    private final int size;
    private int pos;

    public HakunaBufferedOutputStream(OutputStream out) {
        this(out, DEFAULT_SIZE);
    }

    public HakunaBufferedOutputStream(OutputStream out, int size) {
        this.out = out;
        this.buf = new byte[size];
        this.size = size;
        this.pos = 0;
    }

    @Override
    public final void write(int b) throws IOException {
        if (pos == size) {
            flush();
        }
        buf[pos++] = (byte) b;
    }

    @Override
    public final void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public final void write(byte[] b, int off, int len) throws IOException {
        if (len > size) {
            flush();
            out.write(b, off, len);
            return;
        }
        if (pos + len > size) {
            flush();
        }
        System.arraycopy(b, off, buf, pos, len);
    }

    @Override
    public void flush() throws IOException {
        out.write(buf, 0, pos);
        pos = 0;
    }

    @Override
    public void close() throws IOException {
        flush();
        out.close();
    }

}
