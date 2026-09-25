package fi.nls.hakunapi.bytes;

import static fi.nls.hakunapi.bytes.Asserts.assertReads;
import static fi.nls.hakunapi.bytes.Asserts.resource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fi.nls.hakunapi.bytes.ByteSources.Location;
import fi.nls.hakunapi.bytes.ByteSources.Mode;
import fi.nls.hakunapi.bytes.sozip.SOZipEntry;

public class ByteSourcesTest {

    @Test
    public void testParse() throws Exception {
        assertEquals(new Location("/data/a.gpkg", false, false, null), ByteSources.parse("/data/a.gpkg"));
        assertEquals(new Location("https://h/a.gpkg", true, false, null), ByteSources.parse("https://h/a.gpkg"));
        assertEquals(new Location("https://h/a.gpkg", true, false, null), ByteSources.parse("/vsicurl/https://h/a.gpkg"));
        assertEquals(new Location("/data/a.ZIP", false, true, "dir/m.gpkg"), ByteSources.parse("/vsizip//data/a.ZIP/dir/m.gpkg"));
        assertEquals(new Location("/data/a.zip", false, true, null), ByteSources.parse("/vsizip//data/a.zip"));
        assertEquals(new Location("/data/a.zip", false, true, "b.zip/c"), ByteSources.parse("/vsizip//data/a.zip/b.zip/c"));
        assertEquals(new Location("https://h/a.zip", true, true, "m.gpkg"),
                ByteSources.parse("/vsizip//vsicurl/https://h/a.zip/m.gpkg"));
        assertEquals(new Location("https://h/a.zip", true, true, "m.gpkg"),
                ByteSources.parse("/vsizip/vsicurl/https://h/a.zip//m.gpkg"));
        assertEquals(new Location("https://h/a.zip?X-Sig=1/2", true, true, "m.gpkg"),
                ByteSources.parse("/vsizip/{/vsicurl/https://h/a.zip?X-Sig=1/2}/m.gpkg"));
        assertEquals(new Location("/./vsizip/a.gpkg", false, false, null), ByteSources.parse("/./vsizip/a.gpkg"));

        assertThrows(IllegalArgumentException.class, () -> ByteSources.parse("/vsizip//data/a.gpkg"));
        assertThrows(IllegalArgumentException.class, () -> ByteSources.parse("/vsizip/{/data/a.zip"));
        assertThrows(IllegalArgumentException.class, () -> ByteSources.parse("/vsizip/https://h/a.zip/m.gpkg"));
        assertThrows(IllegalArgumentException.class, () -> ByteSources.parse("/vsis3/bucket/a.gpkg"));
    }

    @Test
    public void testOpen() throws Exception {
        String zip = resource("sozip.zip").toString();
        try (ByteSource s = ByteSources.open(zip, 1 << 20, Mode.PREAD)) {
            assertTrue(s instanceof PreadFile);
        }
        try (ByteSource s = ByteSources.open(zip, 1 << 20, Mode.MMAP)) {
            assertTrue(s instanceof MappedFile);
        }
        try (ByteSource s = ByteSources.open("/vsizip/" + zip + "/dir/data.bin", 1 << 20, Mode.PREAD)) {
            assertTrue(s instanceof CachingByteSource);
        }
        try (ByteSource s = ByteSources.open("/vsizip/" + zip + "/dir/data.bin", 0, Mode.PREAD)) {
            assertTrue(s instanceof SOZipEntry);
        }
        try (ByteSource s = ByteSources.open("/vsizip/" + zip + "/dir/data.bin", 1 << 20, Mode.MEMORY)) {
            assertTrue(s instanceof InMemoryFile);
            assertReads(RecordingSource.generate(100_000), s);
        }
        try (RangeHttpServer server = new RangeHttpServer(new byte[100]);
                ByteSource s = ByteSources.open(server.uri().toString(), 1 << 20, Mode.PREAD)) {
            assertTrue(s instanceof CachingByteSource);
        }
    }

}
