package fi.nls.hakunapi.bytes.sozip;

import static fi.nls.hakunapi.bytes.Asserts.assertReads;
import static fi.nls.hakunapi.bytes.Asserts.resource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;

import org.junit.Test;

import fi.nls.hakunapi.bytes.ByteReader;
import fi.nls.hakunapi.bytes.ByteSource;
import fi.nls.hakunapi.bytes.ByteSources;
import fi.nls.hakunapi.bytes.ByteSources.Mode;
import fi.nls.hakunapi.bytes.CachingByteSource;
import fi.nls.hakunapi.bytes.RecordingSource;
import fi.nls.hakunapi.bytes.RangeHttpServer;

/**
 * Archives written by GDAL's sozip, entries are RecordingSource.generate(n):
 *
 * <pre>
 * sozip.zip    sozip --enable-sozip=yes --sozip-chunk-size=4096 -r sozip.zip dir a
 *              dir/data.bin and a/b/data.bin, 100000 bytes each
 * deflate.zip  sozip --enable-sozip=no deflate.zip small.bin
 *              small.bin, 5000 bytes, no index
 * </pre>
 */
public class SOZipTest {

    private static final int SIZE = 100_000;
    private static final int CHUNK = 4096;

    @Test
    public void testReads() throws Exception {
        byte[] data = RecordingSource.generate(SIZE);
        for (String entry : new String[] { "dir/data.bin", "a/b/data.bin" }) {
            try (SOZipEntry e = SOZip.open(archive("sozip.zip"), entry)) {
                assertEquals(CHUNK, e.chunkSize());
                assertReads(data, e);
            }
            try (SOZipEntry e = SOZip.open(archive("sozip.zip"), entry);
                    CachingByteSource c = new CachingByteSource(e, e.chunkSize(), 64 * 1024, 4)) {
                assertReads(data, c);
            }
        }
    }

    @Test
    public void testDeflateWithoutIndexIsInflatedWhole() throws Exception {
        try (SOZipEntry e = SOZip.open(archive("deflate.zip"), null)) {
            assertEquals(5000, e.chunkSize());
            assertReads(RecordingSource.generate(5000), e);
        }
    }

    @Test
    public void testEntrySelection() throws Exception {
        IOException e = assertThrows(IOException.class, () -> SOZip.open(archive("sozip.zip"), null));
        assertTrue(e.getMessage().contains("has 2 entries"));
        e = assertThrows(IOException.class, () -> SOZip.open(archive("sozip.zip"), "c.bin"));
        assertTrue(e.getMessage().startsWith("No entry c.bin"));
    }

    /**
     * A chunk a read covers in part stays in the reader's scratch for the next read.
     */
    @Test
    public void testForwardReadsInflateEachChunkOnce() throws Exception {
        RecordingSource archive = archive("sozip.zip");
        try (SOZipEntry e = SOZip.open(archive, "dir/data.bin"); ByteReader h = e.open()) {
            int opening = archive.reads.get();
            byte[] b = new byte[777];
            for (long off = 0; off < SIZE; off += b.length) {
                h.read(off, b, 0, b.length);
            }
            assertEquals((SIZE + CHUNK - 1) / CHUNK, archive.reads.get() - opening);
        }
    }

    @Test
    public void testOverHttp() throws Exception {
        try (RangeHttpServer server = new RangeHttpServer(Files.readAllBytes(resource("sozip.zip")));
                ByteSource s = ByteSources.open("/vsizip/{/vsicurl/" + server.uri() + "}/dir/data.bin", 1 << 20, Mode.PREAD);
                ByteReader h = s.open()) {
            // Tail, entry's local header, index's local header, its name and extra, index
            assertEquals(5, server.gets());
            byte[] b = new byte[1000];
            for (int pass = 0; pass < 2; pass++) {
                for (long off = 0; off < SIZE; off += b.length) {
                    h.read(off, b, 0, b.length);
                }
                // 25 chunks in read-ahead runs of 1, 2, 4, 8 and 10
                assertEquals(5 + 5, server.gets());
            }
        }
    }

    private static RecordingSource archive(String name) throws Exception {
        return new RecordingSource(Files.readAllBytes(resource(name)));
    }

}
