package fi.nls.hakunapi.tiles.source.vectortile.geom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RingClipTest {
    private static final double MIN = 0, MAX = 10;

    @Test
    public void ringCrossingEdgesClampedToRect() {
        double[] src = {-5, -5, 15, -5, 15, 15, -5, 15};
        double[] dst = new double[64];
        double[] tmp = new double[64];
        int n = RingClip.clip(src, 4, MIN, MIN, MAX, MAX, dst, tmp);
        assertEquals(4, n);
        for (int i = 0; i < n; i++) {
            double x = dst[i * 2], y = dst[i * 2 + 1];
            assertTrue(x >= MIN - 1e-9 && x <= MAX + 1e-9);
            assertTrue(y >= MIN - 1e-9 && y <= MAX + 1e-9);
        }
    }

    @Test
    public void triangleHalfOutsideAddsIntersections() {
        double[] src = {5, 5, 15, 5, 5, 9};
        double[] dst = new double[64];
        double[] tmp = new double[64];
        int n = RingClip.clip(src, 3, MIN, MIN, MAX, MAX, dst, tmp);
        assertTrue("expected clipped poly to gain a vertex", n >= 4);
        for (int i = 0; i < n; i++) {
            assertTrue(dst[i * 2] <= MAX + 1e-9);
        }
    }
}
