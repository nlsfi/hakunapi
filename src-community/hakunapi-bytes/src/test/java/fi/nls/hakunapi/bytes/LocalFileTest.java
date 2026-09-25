package fi.nls.hakunapi.bytes;

import static fi.nls.hakunapi.bytes.Asserts.assertReads;
import static fi.nls.hakunapi.bytes.Asserts.slice;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.channels.ClosedByInterruptException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class LocalFileTest {

    private static final int SIZE = 300_000;

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void testReads() throws Exception {
        byte[] data = RecordingSource.generate(SIZE);
        Path p = write(data);
        try (PreadFile pread = new PreadFile(p);
                MappedFile mapped = new MappedFile(p);
                InMemoryFile memory = new InMemoryFile(new PreadFile(p))) {
            assertReads(data, pread);
            assertReads(data, mapped);
            assertReads(data, memory);
        }
    }

    @Test
    public void testFetch() throws Exception {
        byte[] data = RecordingSource.generate(SIZE);
        Path p = write(data);
        try (PreadFile pread = new PreadFile(p); ByteReader h = pread.open()) {
            assertNull(h.fetch(0, 10));
        }
        for (ByteSource s : new ByteSource[] { new MappedFile(p), new InMemoryFile(new PreadFile(p)) }) {
            ByteReader h = s.open();
            MemorySegment seg = h.fetch(1000, 100);
            assertEquals(SIZE, seg.byteSize());
            assertArrayEquals(slice(data, 1000, 100), seg.asSlice(1000, 100).toArray(ValueLayout.JAVA_BYTE));
            assertSame(seg, h.fetch(SIZE - 10, 10));
            assertNull(h.fetch(SIZE - 10, 11));
            s.close();
            assertThrows(IllegalStateException.class, () -> seg.get(ValueLayout.JAVA_BYTE, 0));
        }
    }

    @Test
    public void testSurvivesAnInterruptedRead() throws Exception {
        byte[] data = RecordingSource.generate(SIZE);
        try (PreadFile f = new PreadFile(write(data)); ByteReader h = f.open()) {
            byte[] arr = new byte[100];
            Thread.currentThread().interrupt();
            try {
                assertThrows(ClosedByInterruptException.class, () -> h.read(0, arr, 0, 100));
            } finally {
                assertTrue(Thread.interrupted());
            }
            assertEquals(100, h.read(1000, arr, 0, 100));
            assertArrayEquals(slice(data, 1000, 100), arr);
        }
    }

    private Path write(byte[] data) throws Exception {
        Path p = tmp.newFile().toPath();
        Files.write(p, data);
        return p;
    }

}
