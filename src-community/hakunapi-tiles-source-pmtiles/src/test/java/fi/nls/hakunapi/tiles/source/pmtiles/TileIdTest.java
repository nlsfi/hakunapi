package fi.nls.hakunapi.tiles.source.pmtiles;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TileIdTest {
    @Test
    public void roundTripsEveryAddressOfTheLowerZooms() {
        for (int z = 0; z <= 7; z++) {
            long dim = 1L << z;
            for (long y = 0; y < dim; y++) {
                for (long x = 0; x < dim; x++) {
                    long id = TileId.fromZXY(z, x, y);
                    assertArrayEquals("z" + z + "/" + y + "/" + x,
                            new long[] { z, x, y }, TileId.toZXY(id));
                }
            }
        }
    }

    @Test
    public void zoomsOccupyConsecutiveIdRanges() {
        long expected = 0;
        for (int z = 0; z <= 12; z++) {
            assertEquals("start of z" + z, expected, TileId.toZXY(expected)[0] == z ? expected : -1);
            long dim = 1L << z;
            expected += dim * dim;
        }
    }

    @Test
    public void inRangeRejectsAddressesOutsideTheZoom() {
        assertTrue(TileId.inRange(3, 7, 0));
        assertFalse(TileId.inRange(3, 8, 0));
        assertFalse(TileId.inRange(3, -1, 0));
        assertFalse(TileId.inRange(-1, 0, 0));
        assertFalse(TileId.inRange(32, 0, 0));
    }
}
