package fi.nls.hakunapi.bytes;

import java.io.IOException;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.channels.ClosedChannelException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A shared LRU cache of fixed-size blocks over another source.
 *
 * Read-ahead is per reader: a miss that starts where
 * the reader's previous fetch ended fetches twice as many blocks as that fetch
 * did, up to maxReadAhead, in one request. Any other miss fetches one block.
 * Blocks served from the cache keep a scan sequential.
 *
 * A miss is fetched outside the lock, and concurrent misses of the same block
 * make one request.
 */
public class CachingByteSource implements ByteSource {

    private static final Logger LOG = LoggerFactory.getLogger(CachingByteSource.class);

    /**
     * Fetches between progress log lines
     */
    private static final int LOG_EVERY = 200;

    /**
     * Largest run fetched in one request, so a run always fits in one array.
     */
    private static final int MAX_RUN_BYTES = 1 << 30;

    private final ByteSource delegate;
    private final int blockSize;
    private final int maxBlocks;
    private final int maxReadAhead;
    private final long size;
    private final long lastBlock;

    private final ReentrantLock lock = new ReentrantLock();
    private final LinkedHashMap<Long, byte[]> blocks;
    private final HashMap<Long, Pending> inFlight = new HashMap<>();

    private long hits;
    private long fetches;
    private boolean closed;

    /**
     * @param blockSize bytes per block
     * @param cacheBytes total bytes to keep; at least one block is kept
     * @param maxReadAhead most blocks one request fetches; 1 turns read-ahead
     *        off. Capped at what the cache holds.
     */
    public CachingByteSource(ByteSource delegate, int blockSize, long cacheBytes, int maxReadAhead)
            throws IllegalArgumentException {
        if (blockSize <= 0 || blockSize > MAX_RUN_BYTES) {
            throw new IllegalArgumentException("blockSize must be between 1 and " + MAX_RUN_BYTES);
        }
        if (maxReadAhead <= 0) {
            throw new IllegalArgumentException("maxReadAhead must be positive");
        }
        this.delegate = delegate;
        this.blockSize = blockSize;
        this.maxBlocks = (int) Math.max(1, Math.min(cacheBytes / blockSize, Integer.MAX_VALUE));
        this.maxReadAhead = Math.max(1, Math.min(maxReadAhead, Math.min(maxBlocks, MAX_RUN_BYTES / blockSize)));
        this.size = delegate.size();
        this.lastBlock = (size - 1) / blockSize;
        this.blocks = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Long, byte[]> eldest) {
                return size() > maxBlocks;
            }
        };
        LOG.info("Block cache: {} KiB blocks, up to {} of them ({} MiB), read-ahead up to {} blocks",
                blockSize / 1024, maxBlocks, (long) maxBlocks * blockSize / (1024 * 1024),
                this.maxReadAhead);
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public ByteReader open() throws IOException {
        return new Reader(delegate.open());
    }

    /**
     * Cache hits since opening.
     */
    public long hits() {
        lock.lock();
        try {
            return hits;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Requests issued to the delegate since opening, one per run of blocks.
     */
    public long fetches() {
        lock.lock();
        try {
            return fetches;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() throws IOException {
        lock.lock();
        try {
            if (closed) {
                return;
            }
            closed = true;
            blocks.clear();
        } finally {
            lock.unlock();
        }
        LOG.info("Block cache: {} hits, {} fetches", hits, fetches);
        delegate.close();
    }

    /**
     * Serves a read from the blocks covering it, fetching those it does not have.
     * Exactly one of dstArray and dstSegment is non-null; they are handled
     * together because the block walk is the same.
     */
    private int read(Reader h, long off, int len, byte[] dstArray, long dstOff, MemorySegment dstSegment)
            throws IOException {
        if (len <= 0 || off >= size) {
            return 0;
        }
        int want = (int) Math.min(len, size - off);
        int done = 0;
        while (done < want) {
            long offset = off + done;
            long blockIndex = offset / blockSize;
            int within = (int) (offset % blockSize);
            byte[] block = block(h, blockIndex);
            int n = Math.min(want - done, block.length - within);
            if (dstArray != null) {
                System.arraycopy(block, within, dstArray, (int) dstOff + done, n);
            } else {
                MemorySegment.copy(block, within, dstSegment, ValueLayout.JAVA_BYTE, dstOff + done, n);
            }
            done += n;
        }
        return done;
    }

    /**
     * The block at blockIndex, fetching it and maybe the ones after it on a miss.
     */
    private byte[] block(Reader h, long blockIndex) throws IOException {
        while (true) {
            Pending running;
            int count = 0;
            lock.lock();
            try {
                if (closed) {
                    throw new ClosedChannelException();
                }
                byte[] cached = blocks.get(blockIndex);
                if (cached != null) {
                    hits++;
                    h.passed(blockIndex);
                    return cached;
                }
                running = inFlight.get(blockIndex);
                if (running != null) {
                    // Costs no request of its own, so it counts as a hit
                    hits++;
                    h.passed(blockIndex);
                } else {
                    count = startRun(h, blockIndex);
                    fetches++;
                    if (fetches % LOG_EVERY == 0) {
                        LOG.info("Block cache: {} hits, {} fetches, {} blocks held", hits, fetches, blocks.size());
                    }
                }
            } finally {
                lock.unlock();
            }
            if (running == null) {
                return fetchRun(h, blockIndex, count);
            }
            byte[] block = running.await();
            if (block != null) {
                return block;
            }
            // That fetch failed, possibly for a reason of its own thread's
            // (an interrupt), so this thread tries for itself
        }
    }

    /**
     * Decides how many blocks from blockIndex this miss fetches and claims them
     * in {@link #inFlight}. Called under the lock.
     */
    private int startRun(Reader h, long blockIndex) {
        boolean sequential = blockIndex * blockSize == h.lastFetchEnd;
        int want = sequential ? Math.min(h.readAhead * 2, maxReadAhead) : 1;
        int count = 1;
        while (count < want
                && blockIndex + count <= lastBlock
                && !blocks.containsKey(blockIndex + count)
                && !inFlight.containsKey(blockIndex + count)) {
            count++;
        }
        for (int i = 0; i < count; i++) {
            inFlight.put(blockIndex + i, new Pending());
        }
        h.readAhead = want;
        h.lastFetchEnd = (blockIndex + count) * blockSize;
        return count;
    }

    /**
     * Fetches a run claimed by {@link #startRun}, and publishes it.
     */
    private byte[] fetchRun(Reader h, long blockIndex, int count) throws IOException {
        byte[][] run = null;
        try {
            run = fetch(h.source, blockIndex, count);
            return run[0];
        } finally {
            release(blockIndex, count, run);
        }
    }

    /**
     * Puts a fetched run in the cache and hands it to the waiters, or null if
     * the fetch failed.
     */
    private void release(long blockIndex, int count, byte[][] run) {
        Pending[] started = new Pending[count];
        lock.lock();
        try {
            for (int i = 0; i < count; i++) {
                started[i] = inFlight.remove(blockIndex + i);
                // A fetch finishing after close() must not refill the cache
                if (run != null && !closed) {
                    blocks.put(blockIndex + i, run[i]);
                }
            }
        } finally {
            lock.unlock();
        }
        for (int i = 0; i < count; i++) {
            if (started[i] != null) {
                started[i].complete(run != null ? run[i] : null);
            }
        }
    }

    /**
     * count blocks from blockIndex in one read, cut into an array per block so
     * that each block is evicted on its own.
     */
    private byte[][] fetch(ByteReader src, long blockIndex, int count) throws IOException {
        long start = blockIndex * blockSize;
        int length = (int) Math.min((long) count * blockSize, size - start);
        byte[] bytes = new byte[length];
        int read = 0;
        while (read < length) {
            int n = src.read(start + read, bytes, read, length - read);
            if (n <= 0) {
                break;
            }
            read += n;
        }
        if (read < length) {
            // The file changed or its size was wrong
            throw new IOException("Short read of block " + blockIndex + " at " + start
                    + ": expected " + length + " bytes, got " + read);
        }
        if (count == 1) {
            return new byte[][] { bytes };
        }
        byte[][] run = new byte[count][];
        for (int i = 0; i < count; i++) {
            int from = i * blockSize;
            run[i] = Arrays.copyOfRange(bytes, from, Math.min(from + blockSize, length));
        }
        return run;
    }

    /**
     * One stream's read-ahead state.
     */
    private class Reader implements ByteReader {

        private final ByteReader source;
        /**
         * Where this reader's previous fetch ended; a miss starting here is sequential.
         */
        private long lastFetchEnd = -1;
        /**
         * Blocks the previous fetch asked for.
         */
        private int readAhead = 1;

        private Reader(ByteReader source) {
            this.source = source;
        }

        /**
         * A block served without a fetch still moves a sequential scan along.
         */
        private void passed(long blockIndex) {
            if (blockIndex * blockSize == lastFetchEnd) {
                lastFetchEnd += blockSize;
            }
        }

        @Override
        public int read(long off, MemorySegment dst, long dstOff, int len) throws IOException {
            return CachingByteSource.this.read(this, off, len, null, dstOff, dst);
        }

        @Override
        public int read(long off, byte[] dst, int dstOff, int len) throws IOException {
            return CachingByteSource.this.read(this, off, len, dst, dstOff, null);
        }

        @Override
        public MemorySegment fetch(long off, int len) {
            return null;
        }

        @Override
        public void close() {
            source.close();
        }

    }

    /**
     * A block being fetched, which other threads wanting it wait on. Null when
     * the fetch failed.
     */
    private static final class Pending {

        private byte[] block;
        private boolean done;

        synchronized byte[] await() {
            boolean interrupted = false;
            while (!done) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
            return block;
        }

        synchronized void complete(byte[] block) {
            this.block = block;
            this.done = true;
            notifyAll();
        }

    }

}
