package fi.nls.hakunapi.tiles.source.pmtiles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import fi.nls.hakunapi.tiles.TileLocator;

public class BoxedTileIndexTest {
    private static PMTilesArchive openFixture() throws Exception {
        Path path = Paths.get("src/test/resources/test_fixture_1.pmtiles");
        return PMTilesArchive.open(new FileRangeReader(path));
    }

    @Test
    public void answersExactlyAsTheSortedIndexDoes() throws Exception {
        try (PMTilesArchive archive = openFixture()) {
            TileIndex sorted = TileIndex.build(archive);
            BoxedTileIndex boxed = BoxedTileIndex.of(sorted);
            assertNotNull("the fixture should be dense enough to box", boxed);

            int hits = 0;
            for (int z = 0; z <= 4; z++) {
                long dim = 1L << z;

                for (long y = 0; y <= dim; y++) {
                    for (long x = 0; x <= dim; x++) {
                        long a = x < dim && y < dim ? sorted.find(z, x, y) : TileLocator.NOT_FOUND;
                        long b = x < dim && y < dim ? boxed.find(z, x, y) : TileLocator.NOT_FOUND;
                        assertEquals("z" + z + "/" + y + "/" + x, a, b);
                        if (a != TileLocator.NOT_FOUND) {
                            hits++;
                        }
                    }
                }
            }
            assertTrue("the fixture should hold some tiles in z0..4", hits > 0);
        }
    }

    @Test
    public void describesTheArchiveTheSameWay() throws Exception {
        try (PMTilesArchive archive = openFixture()) {
            TileIndex sorted = TileIndex.build(archive);
            BoxedTileIndex boxed = BoxedTileIndex.of(sorted);
            assertNotNull(boxed);
            assertEquals(sorted.getMediaType(), boxed.getMediaType());
            assertEquals(sorted.getContentEncoding(), boxed.getContentEncoding());
        }
    }
}
