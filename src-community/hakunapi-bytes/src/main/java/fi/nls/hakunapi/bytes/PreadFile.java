package fi.nls.hakunapi.bytes;

import java.io.IOException;
import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A local file read with positional reads on one shared FileChannel.
 */
public class PreadFile implements ByteSource {

    private static final Logger LOG = LoggerFactory.getLogger(PreadFile.class);

    private final Path path;
    private final long size;
    private volatile FileChannel channel;
    private volatile boolean closed;

    public PreadFile(Path path) throws IOException {
        this.path = path;
        this.channel = FileChannel.open(path, StandardOpenOption.READ);
        this.size = channel.size();
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public ByteReader open() {
        return new Reader();
    }

    @Override
    public synchronized void close() throws IOException {
        if (closed) {
            return;
        }
        closed = true;
        channel.close();
    }

    /**
     * Fills bb from off, stopping short only at end of file.
     */
    private int read(long off, ByteBuffer bb) throws IOException {
        int total = 0;
        while (bb.hasRemaining()) {
            int n = pread(bb, off + total);
            if (n < 0) {
                break;
            }
            total += n;
        }
        return total;
    }

    /**
     * An interrupt closes the shared channel. The interrupted thread gets its
     * exception, and the channel is reopened for the others.
     */
    private int pread(ByteBuffer bb, long pos) throws IOException {
        FileChannel ch = channel;
        try {
            return ch.read(bb, pos);
        } catch (ClosedByInterruptException e) {
            reopen(ch);
            throw e;
        } catch (ClosedChannelException e) {
            if (closed) {
                throw e;
            }
            return reopen(ch).read(bb, pos);
        }
    }

    private synchronized FileChannel reopen(FileChannel broken) throws IOException {
        if (closed) {
            throw new ClosedChannelException();
        }
        if (channel == broken) {
            channel = FileChannel.open(path, StandardOpenOption.READ);
            LOG.warn("Reopened {}, an interrupted read had closed it", path);
        }
        return channel;
    }

    /**
     * Reuses the ByteBuffer view of the caller's last buffer. A segment over
     * 2 GB has no view, so only the part read is viewed.
     */
    private class Reader implements ByteReader {

        private MemorySegment lastSegment;
        private ByteBuffer segmentBuffer;
        private byte[] lastArray;
        private ByteBuffer arrayBuffer;

        @Override
        public int read(long off, MemorySegment dst, long dstOff, int len) throws IOException {
            if (len <= 0 || off >= size) {
                return 0;
            }
            int want = (int) Math.min(len, size - off);
            if (dst.byteSize() > Integer.MAX_VALUE) {
                return PreadFile.this.read(off, dst.asSlice(dstOff, want).asByteBuffer());
            }
            if (dst != lastSegment) {
                lastSegment = dst;
                segmentBuffer = dst.asByteBuffer();
            }
            segmentBuffer.clear().position((int) dstOff).limit((int) dstOff + want);
            return PreadFile.this.read(off, segmentBuffer);
        }

        @Override
        public int read(long off, byte[] dst, int dstOff, int len) throws IOException {
            if (len <= 0 || off >= size) {
                return 0;
            }
            int want = (int) Math.min(len, size - off);
            if (dst != lastArray) {
                lastArray = dst;
                arrayBuffer = ByteBuffer.wrap(dst);
            }
            arrayBuffer.clear().position(dstOff).limit(dstOff + want);
            return PreadFile.this.read(off, arrayBuffer);
        }

        @Override
        public MemorySegment fetch(long off, int len) {
            return null;
        }

        @Override
        public void close() {
            // NOP, the channel belongs to the source
        }

    }

}
