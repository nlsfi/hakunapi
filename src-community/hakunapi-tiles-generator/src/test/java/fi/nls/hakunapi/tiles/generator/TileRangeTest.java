package fi.nls.hakunapi.tiles.generator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fi.nls.hakunapi.tiles.TileMatrix;

public class TileRangeTest {

    private static TileMatrix matrix() {
        return new TileMatrix("2", 1000, 1.0, 0.0, 1024.0, 256, 256, 4, 4);
    }

    @Test
    public void rowsGrowDownwardAndATileAlignedEdgeDoesNotPullInTheNextTile() {
        TileRange bottomLeft = TileRange.of(matrix(), 2, 0, 0, 256, 256);
        assertEquals(3, bottomLeft.minRow());
        assertEquals(3, bottomLeft.maxRow());
        assertEquals(0, bottomLeft.minCol());
        assertEquals(0, bottomLeft.maxCol());

        TileRange topTwoColumns = TileRange.of(matrix(), 2, 0, 768, 512, 1024);
        assertEquals(0, topTwoColumns.minRow());
        assertEquals(0, topTwoColumns.maxRow());
        assertEquals(0, topTwoColumns.minCol());
        assertEquals(1, topTwoColumns.maxCol());
    }

    @Test
    public void extentsAreClampedToTheMatrixAndOutsideOnesAreEmpty() {
        TileRange all = TileRange.of(matrix(), 2, -5000, -5000, 5000, 5000);
        assertEquals(0, all.minRow());
        assertEquals(3, all.maxRow());
        assertEquals(0, all.minCol());
        assertEquals(3, all.maxCol());
        assertEquals(16, all.size());

        TileRange outside = TileRange.of(matrix(), 2, 5000, 5000, 6000, 6000);
        assertTrue(outside.isEmpty());
        assertEquals(0, outside.size());
    }

}
