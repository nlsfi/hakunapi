package fi.nls.hakunapi.tiles.zoom;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ZoomBackingTest {

    private static ZoomBacking range(int minZoom, int maxZoom) {
        return new ZoomBacking("b", minZoom, maxZoom, null, null);
    }

    @Test
    public void coversItsRangeInclusively() {
        ZoomBacking b = range(10, 12);
        assertTrue(b.covers("10"));
        assertTrue(b.covers("11"));
        assertTrue(b.covers("12"));
    }

    @Test
    public void doesNotCoverOutsideItsRange() {
        ZoomBacking b = range(10, 12);
        assertFalse(b.covers("9"));
        assertFalse(b.covers("13"));
        assertFalse(b.covers("0"));
    }

    @Test
    public void aSingleZoomRangeCoversOnlyThatZoom() {
        ZoomBacking b = range(7, 7);
        assertTrue(b.covers("7"));
        assertFalse(b.covers("6"));
        assertFalse(b.covers("8"));
    }

    @Test
    public void aNonNumericTileMatrixMatchesNothing() {
        ZoomBacking b = range(0, 20);
        assertFalse(b.covers("overview"));
        assertFalse(b.covers(""));
        assertFalse(b.covers("10k"));
    }

}
