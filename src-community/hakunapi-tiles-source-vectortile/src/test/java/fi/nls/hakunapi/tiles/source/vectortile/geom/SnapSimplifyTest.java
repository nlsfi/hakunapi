package fi.nls.hakunapi.tiles.source.vectortile.geom;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SnapSimplifyTest {

    @Test
    public void snapDedupRingDropsClosureAfterRounding() {
        double[] a = {0.2f, -0.1f, 4, 0, 4, 4, 0, 4, -0.3f, 0.4f};
        int n = SnapSimplify.snapDedupRing(a, 5);
        assertEquals(4, n);
        assertArrayEquals(new double[] {0, 0, 4, 0, 4, 4, 0, 4}, slice(a, n), 0);
    }

    @Test
    public void dropCollinearRingAreaSignFollowsWinding() {
        double[] a = {0, 0, 0, 4, 4, 4, 4, 0, 2, 0};
        double[] out = new double[12];
        SnapSimplify.RingResult res = new SnapSimplify.RingResult();
        SnapSimplify.dropCollinearRing(a, 5, 0, out, res);
        assertEquals(4, res.n);
        assertEquals(-32.0, res.area, 0);
    }

    private static double[] slice(double[] a, int pairs) {
        double[] b = new double[pairs * 2];
        System.arraycopy(a, 0, b, 0, pairs * 2);
        return b;
    }
}
