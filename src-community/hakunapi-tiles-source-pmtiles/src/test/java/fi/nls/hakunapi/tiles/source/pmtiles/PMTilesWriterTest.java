package fi.nls.hakunapi.tiles.source.pmtiles;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class PMTilesWriterTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private static final int[] BBOX = { -1800000000, -850000000, 1800000000, 850000000 };
    private static final int[] CENTER = { 0, 0 };

    private static byte[] body(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    private Path write(Writing writing) throws Exception {
        Path archive = folder.newFile("out.pmtiles").toPath();
        try (PMTilesWriter writer = new PMTilesWriter(archive, Compression.GZIP, (byte) 1)) {
            writing.write(writer);
            writer.finish("{\"name\":\"test\"}", BBOX, CENTER, 0);
        }
        return archive;
    }

    private interface Writing {
        void write(PMTilesWriter writer) throws Exception;
    }

    private static byte[] read(Path archive, int z, long x, long y) throws Exception {
        try (PMTilesArchive a = PMTilesArchive.open(new FileRangeReader(archive))) {
            return a.getTile(z, x, y).orElse(null);
        }
    }

    private static int indexOf(byte[] haystack, byte[] needle, int from) {
        outer:
        for (int i = from; i <= haystack.length - needle.length; i++) {
            for (int j = 0; j < needle.length; j++) {
                if (haystack[i + j] != needle[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    @Test
    public void tilesReadBackAtTheirAddressAndIdenticalBodiesAreStoredOnce() throws Exception {
        byte[] repeated = body("the very same bytes");
        Path archive = write(w -> {
            w.add(0, 0, 0, body("zero"));
            w.add(1, 0, 0, repeated);
            w.add(1, 0, 1, repeated);
            w.add(1, 1, 1, repeated);
        });

        assertArrayEquals(body("zero"), read(archive, 0, 0, 0));
        assertArrayEquals(repeated, read(archive, 1, 0, 0));
        assertArrayEquals(repeated, read(archive, 1, 0, 1));
        assertArrayEquals(repeated, read(archive, 1, 1, 1));
        assertNull(read(archive, 1, 1, 0));

        byte[] file = Files.readAllBytes(archive);
        int first = indexOf(file, repeated, 0);
        assertTrue(first >= 0);
        assertEquals(-1, indexOf(file, repeated, first + 1));
        ByteBuffer header = ByteBuffer.wrap(file).order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(4, header.getLong(72));
        assertEquals(2, header.getLong(88));
    }

    @Test
    public void aLargeArchiveSplitsIntoLeafDirectoriesAndReadsBack() throws Exception {
        int n = 100_000;
        int z = 9;
        long dim = 1L << z;
        Random random = new Random(42);
        List<long[]> addresses = new ArrayList<>(n);
        for (long i = 0; addresses.size() < n && i < dim * dim; i++) {
            addresses.add(new long[] { i % dim, i / dim });
        }
        addresses.sort((a, b) -> Long.compare(
                TileId.fromZXY(z, a[0], a[1]), TileId.fromZXY(z, b[0], b[1])));
        String[] bodies = new String[n];
        for (int i = 0; i < n; i++) {
            bodies[i] = "tile-" + i + "x".repeat(random.nextInt(200));
        }

        Path archive = write(w -> {
            for (int i = 0; i < addresses.size(); i++) {
                long[] a = addresses.get(i);
                w.add(z, a[0], a[1], body(bodies[i]));
            }
        });

        ByteBuffer header = ByteBuffer.wrap(Files.readAllBytes(archive)).order(ByteOrder.LITTLE_ENDIAN);
        assertNotEquals(0, header.getLong(48));
        assertTrue(header.getLong(16) <= 16_384 - 127);
        for (int i : new int[] { 0, addresses.size() / 2, addresses.size() - 1 }) {
            long[] a = addresses.get(i);
            assertArrayEquals(body(bodies[i]), read(archive, z, a[0], a[1]));
        }
    }

    @Test
    public void theHeaderMatchesTheSpecIndependentlyOfTheReader() throws Exception {
        Path archive = write(w -> w.add(0, 0, 0, body("zero")));

        byte[] file = Files.readAllBytes(archive);
        assertEquals("PMTiles", new String(file, 0, 7, StandardCharsets.US_ASCII));
        assertEquals(3, file[7]);
        assertEquals(1, file[99]);
        assertEquals(127, ByteBuffer.wrap(file).order(ByteOrder.LITTLE_ENDIAN).getLong(8));
        assertEquals(19078479L, TileId.fromZXY(12, 3423, 1763));
    }

    @Test
    public void outOfOrderTilesAndEmptyArchivesAreRefused() throws Exception {
        Path ordered = folder.newFile("order.pmtiles").toPath();
        try (PMTilesWriter writer = new PMTilesWriter(ordered, Compression.GZIP, (byte) 1)) {
            writer.add(1, 1, 1, body("later"));
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> writer.add(0, 0, 0, body("earlier")));
            assertTrue(e.getMessage(), e.getMessage().contains("ascending"));
        }

        Path empty = folder.newFile("empty.pmtiles").toPath();
        try (PMTilesWriter writer = new PMTilesWriter(empty, Compression.GZIP, (byte) 1)) {
            IllegalStateException e = assertThrows(IllegalStateException.class,
                    () -> writer.finish("{}", BBOX, CENTER, 0));
            assertTrue(e.getMessage(), e.getMessage().contains("no tiles"));
        }
    }

}
