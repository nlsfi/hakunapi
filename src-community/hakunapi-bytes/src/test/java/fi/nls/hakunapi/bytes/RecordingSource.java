package fi.nls.hakunapi.bytes;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Records the reads that reach it.
 */
public class RecordingSource implements ByteSource {

    public final byte[] data;
    public final AtomicInteger reads = new AtomicInteger();
    /**
     * "offset+length" of every read, in order
     */
    public final List<String> ranges = new ArrayList<>();
    /**
     * Runs before every read
     */
    public volatile Runnable beforeRead = () -> {};

    public RecordingSource(int size) {
        this(generate(size));
    }

    public RecordingSource(byte[] data) {
        this.data = data;
    }

    /**
     * The content of the entries in sozip.zip and deflate.zip
     */
    public static byte[] generate(int size) {
        byte[] data = new byte[size];
        for (int i = 0; i < size; i++) {
            data[i] = (byte) (i * 31 + (i >> 8));
        }
        return data;
    }

    @Override
    public long size() {
        return data.length;
    }

    @Override
    public ByteReader open() {
        return new ByteReader() {

            @Override
            public int read(long off, MemorySegment dst, long dstOff, int len) {
                int n = count(off, len);
                MemorySegment.copy(data, (int) off, dst, ValueLayout.JAVA_BYTE, dstOff, n);
                return n;
            }

            @Override
            public int read(long off, byte[] dst, int dstOff, int len) {
                int n = count(off, len);
                System.arraycopy(data, (int) off, dst, dstOff, n);
                return n;
            }

            @Override
            public MemorySegment fetch(long off, int len) {
                return null;
            }

            @Override
            public void close() {
                // NOP
            }
        };
    }

    private int count(long off, int len) {
        beforeRead.run();
        reads.incrementAndGet();
        synchronized (ranges) {
            ranges.add(off + "+" + len);
        }
        return (int) Math.max(0, Math.min(len, data.length - off));
    }

    @Override
    public void close() {
        // NOP
    }

}
