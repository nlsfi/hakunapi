package fi.nls.hakunapi.bytes;

import static fi.nls.hakunapi.bytes.Asserts.assertReads;
import static fi.nls.hakunapi.bytes.Asserts.slice;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

public class CachingByteSourceTest {

    private static final int SIZE = 1 << 20;

    @Test
    public void testReads() throws Exception {
        RecordingSource src = new RecordingSource(300_000);
        // Evicting, and with a block that is not a power of two
        try (CachingByteSource r = new CachingByteSource(src, 5000, 64 * 1024, 8)) {
            assertReads(src.data, r);
        }
    }

    @Test
    public void testReadAhead() throws Exception {
        RecordingSource src = new RecordingSource(SIZE);
        try (CachingByteSource r = new CachingByteSource(src, 4096, 1 << 20, 8);
                ByteReader h = r.open()) {
            byte[] buf = new byte[1024];
            for (long off = 0; off < 16 * 4096; off += buf.length) {
                h.read(off, buf, 0, buf.length);
            }
            // blocks 0 | 1-2 | 3-6 | 7-14 | 15-22, capped at 8
            assertEquals(List.of("0+4096", "4096+8192", "12288+16384", "28672+32768", "61440+32768"), src.ranges);

            h.read(100 * 4096, buf, 0, buf.length); // random: back to one block
            h.read(101 * 4096, buf, 0, buf.length); // sequential again: two
            h.read(0, buf, 0, buf.length);          // cached
            assertEquals(List.of("409600+4096", "413696+8192"), src.ranges.subList(5, 7));
            assertEquals(7, r.fetches());
        }
    }

    @Test
    public void testBlocksAreExactlyBlockSize() throws Exception {
        RecordingSource src = new RecordingSource(SIZE);
        try (CachingByteSource r = new CachingByteSource(src, 5000, 1 << 20, 4);
                ByteReader h = r.open()) {
            byte[] buf = new byte[1000];
            for (long off = 0; off < 7 * 5000; off += buf.length) {
                h.read(off, buf, 0, buf.length);
            }
            assertEquals(List.of("0+5000", "5000+10000", "15000+20000"), src.ranges);
        }
    }

    @Test
    public void testReadAheadIsPerReader() throws Exception {
        RecordingSource src = new RecordingSource(SIZE);
        try (CachingByteSource r = new CachingByteSource(src, 4096, 1 << 20, 8);
                ByteReader a = r.open();
                ByteReader b = r.open()) {
            byte[] buf = new byte[4096];
            a.read(0, buf, 0, 4096);
            b.read(50 * 4096, buf, 0, 4096);
            a.read(4096, buf, 0, 4096);
            b.read(51 * 4096, buf, 0, 4096);
            assertEquals(List.of("0+4096", "204800+4096", "4096+8192", "208896+8192"), src.ranges);
        }
    }

    @Test
    public void testConcurrentMissesOfOneBlockMakeOneRequest() throws Exception {
        int threads = 8;
        CountDownLatch arrived = new CountDownLatch(threads);
        RecordingSource src = new RecordingSource(SIZE);
        src.beforeRead = () -> {
            try {
                arrived.await();
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        };
        AtomicReference<Throwable> failure = new AtomicReference<>();
        try (CachingByteSource r = new CachingByteSource(src, 4096, 1 << 20, 8)) {
            Thread[] ts = new Thread[threads];
            for (int t = 0; t < threads; t++) {
                ts[t] = new Thread(() -> {
                    try (ByteReader h = r.open()) {
                        byte[] dst = new byte[100];
                        arrived.countDown();
                        h.read(1000, dst, 0, dst.length);
                        assertArrayEquals(slice(src.data, 1000, 100), dst);
                    } catch (Throwable e) {
                        failure.compareAndSet(null, e);
                    }
                });
                ts[t].start();
            }
            for (Thread t : ts) {
                t.join(30_000);
                assertFalse(t.isAlive());
            }
            assertNull(failure.get());
            assertEquals(1, src.reads.get());
            assertEquals(threads - 1, r.hits());
        }
    }

    @Test
    public void testFailedFetchIsNotCached() throws Exception {
        RecordingSource src = new RecordingSource(SIZE);
        src.beforeRead = () -> {
            throw new IllegalStateException("Connection reset");
        };
        try (CachingByteSource r = new CachingByteSource(src, 4096, 1 << 20, 8);
                ByteReader h = r.open()) {
            byte[] buf = new byte[100];
            assertThrows(IllegalStateException.class, () -> h.read(0, buf, 0, 100));
            src.beforeRead = () -> {};
            assertEquals(100, h.read(0, buf, 0, 100));
            assertArrayEquals(slice(src.data, 0, 100), buf);
        }
    }

    @Test
    public void testWaiterFetchesItselfWhenTheFetchItWaitsForFails() throws Exception {
        RecordingSource src = new RecordingSource(SIZE);
        try (CachingByteSource r = new CachingByteSource(src, 4096, 1 << 20, 8)) {
            CountDownLatch fetching = new CountDownLatch(1);
            AtomicInteger calls = new AtomicInteger();
            src.beforeRead = () -> {
                if (calls.getAndIncrement() == 0) {
                    fetching.countDown();
                    // Fail only once the other thread waits on this fetch
                    while (r.hits() == 0) {
                        Thread.onSpinWait();
                    }
                    throw new IllegalStateException("Interrupted");
                }
            };
            AtomicReference<Throwable> first = new AtomicReference<>();
            Thread a = new Thread(() -> {
                try (ByteReader h = r.open()) {
                    h.read(0, new byte[100], 0, 100);
                } catch (Throwable e) {
                    first.set(e);
                }
            });
            a.start();
            fetching.await();
            try (ByteReader h = r.open()) {
                byte[] buf = new byte[100];
                assertEquals(100, h.read(0, buf, 0, 100));
                assertArrayEquals(slice(src.data, 0, 100), buf);
            }
            a.join(30_000);
            assertTrue(first.get() instanceof IllegalStateException);
            // The failed fetch, and the waiter's own
            assertEquals(2, calls.get());
        }
    }

}
