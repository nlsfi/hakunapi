package fi.nls.hakunapi.tiles.source.vectortile.geom;

/** Snaps clipped coordinates to the MVT grid, dedups and drops collinear points. */
final class SnapSimplify {
    private SnapSimplify() {}

    static void snap(double[] a, int n) {
        int len = n * 2;
        for (int i = 0; i < len; i++) {
            a[i] = Math.rint(a[i]);
        }
    }

    static int snapDedupLine(double[] a, int n) {
        if (n == 0) {
            return 0;
        }
        double px = Math.rint(a[0]);
        double py = Math.rint(a[1]);
        a[0] = px;
        a[1] = py;
        int w = 1;
        for (int i = 1; i < n; i++) {
            double x = Math.rint(a[i * 2]);
            double y = Math.rint(a[i * 2 + 1]);
            if (x != px || y != py) {
                a[w * 2] = x;
                a[w * 2 + 1] = y;
                w++;
                px = x; py = y;
            }
        }
        return w;
    }

    static int snapDedupRing(double[] a, int n) {
        int w = snapDedupLine(a, n);
        while (w > 1 && a[(w - 1) * 2] == a[0] && a[(w - 1) * 2 + 1] == a[1]) {
            w--;
        }
        return w;
    }

    static long snapDedupRingTracked(double[] a, int n) {
        if (n == 0) {
            return 0L;
        }
        double px = Math.rint(a[0]);
        double py = Math.rint(a[1]);
        a[0] = px;
        a[1] = py;
        int w = 1;
        int best = 0;
        double bx = px, by = py;
        for (int i = 1; i < n; i++) {
            double x = Math.rint(a[i * 2]);
            double y = Math.rint(a[i * 2 + 1]);
            if (x != px || y != py) {
                a[w * 2] = x;
                a[w * 2 + 1] = y;
                if (x < bx || (x == bx && y < by)) {
                    best = w; bx = x; by = y;
                }
                w++;
                px = x; py = y;
            }
        }
        while (w > 1 && a[(w - 1) * 2] == a[0] && a[(w - 1) * 2 + 1] == a[1]) {
            w--;
        }
        if (best >= w) {
            best = 0;
        }
        return ((long) best << 32) | (w & 0xffffffffL);
    }

    static int trackedCount(long packed) {
        return (int) packed;
    }

    static int trackedStart(long packed) {
        return (int) (packed >>> 32);
    }

    static int dedupLine(double[] a, int n) {
        if (n <= 1) {
            return n;
        }
        int w = 1;
        double px = a[0], py = a[1];
        for (int i = 1; i < n; i++) {
            double x = a[i * 2], y = a[i * 2 + 1];
            if (x != px || y != py) {
                a[w * 2] = x;
                a[w * 2 + 1] = y;
                w++;
                px = x; py = y;
            }
        }
        return w;
    }

    static int dedupRing(double[] a, int n) {
        int w = dedupLine(a, n);
        while (w > 1 && a[(w - 1) * 2] == a[0] && a[(w - 1) * 2 + 1] == a[1]) {
            w--;
        }
        return w;
    }

    static int dropCollinearLine(double[] a, int n) {
        if (n <= 2) {
            return n;
        }
        int w = 1;
        for (int i = 1; i < n - 1; i++) {
            double bx = a[i * 2], by = a[i * 2 + 1];
            double ax = a[(w - 1) * 2], ay = a[(w - 1) * 2 + 1];
            double cx = a[(i + 1) * 2], cy = a[(i + 1) * 2 + 1];
            if (!collinear(ax, ay, bx, by, cx, cy)) {
                a[w * 2] = bx;
                a[w * 2 + 1] = by;
                w++;
            }
        }
        a[w * 2] = a[(n - 1) * 2];
        a[w * 2 + 1] = a[(n - 1) * 2 + 1];
        w++;
        return w;
    }

    static final class RingResult {
        int n;
        double area;
    }

    static void dropCollinearRing(double[] a, int n, int start, double[] out, RingResult res) {
        if (n <= 3) {
            System.arraycopy(a, 0, out, 0, n * 2);
            res.n = n;
            res.area = shoelace(out, n);
            return;
        }
        double sx0 = a[start * 2], sy0 = a[start * 2 + 1];
        out[0] = sx0;
        out[1] = sy0;
        int w = 1;
        double ax = sx0, ay = sy0;
        double area2 = 0.0;

        for (int k = 1; k < n; k++) {
            int i = idx(start + k, n);
            double bx = a[i * 2], by = a[i * 2 + 1];
            int j = idx(start + k + 1, n);
            double cx = a[j * 2], cy = a[j * 2 + 1];
            if (collinear(ax, ay, bx, by, cx, cy)) {
                continue;
            }
            out[w * 2] = bx;
            out[w * 2 + 1] = by;
            w++;
            area2 += ax * by - bx * ay;
            ax = bx; ay = by;
        }
        area2 += ax * sy0 - sx0 * ay;
        res.n = w;
        res.area = area2;
    }

    private static double shoelace(double[] xy, int n) {
        double a = 0.0;
        int prev = n - 1;
        for (int i = 0; i < n; i++) {
            double x1 = xy[prev * 2], y1 = xy[prev * 2 + 1];
            double x2 = xy[i * 2], y2 = xy[i * 2 + 1];
            a += x1 * y2 - x2 * y1;
            prev = i;
        }
        return a;
    }

    static int dropCollinearRing(double[] a, int n, int start, double[] out) {
        if (n <= 3) {
            System.arraycopy(a, 0, out, 0, n * 2);
            return n;
        }

        double ax = a[start * 2], ay = a[start * 2 + 1];
        out[0] = ax;
        out[1] = ay;
        int w = 1;

        for (int k = 1; k < n; k++) {
            int i = idx(start + k, n);
            double bx = a[i * 2], by = a[i * 2 + 1];
            int j = idx(start + k + 1, n);
            double cx = a[j * 2], cy = a[j * 2 + 1];
            if (collinear(ax, ay, bx, by, cx, cy)) {
                continue;
            }
            out[w * 2] = bx;
            out[w * 2 + 1] = by;
            w++;
            ax = bx; ay = by;
        }
        return w;
    }

    private static int idx(int v, int n) {
        return v >= n ? v - n : v;
    }

    private static boolean collinear(double ax, double ay, double bx, double by,
            double cx, double cy) {
        return (bx - ax) * (cy - ay) - (by - ay) * (cx - ax) == 0.0;
    }
}
