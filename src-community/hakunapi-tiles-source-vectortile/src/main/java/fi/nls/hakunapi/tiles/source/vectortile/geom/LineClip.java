package fi.nls.hakunapi.tiles.source.vectortile.geom;

/** Clips lines to a box. */
final class LineClip {
    private LineClip() {}

    static final class Parts {
        private static final int RETAINED_COORDS = 4096;

        double[] xy = new double[64];
        int n;
        int[] partEnd = new int[8];
        int partCount;

        double sx0, sy0, sx1, sy1;

        void reset() {
            n = 0;
            partCount = 0;
        }

        void trim() {
            if (xy.length > RETAINED_COORDS * 2) {
                xy = new double[RETAINED_COORDS * 2];
            }
        }

        private void ensureXy(int pairs) {
            if (pairs * 2 > xy.length) {
                int cap = xy.length;
                while (cap < pairs * 2) cap <<= 1;
                double[] g = new double[cap];
                System.arraycopy(xy, 0, g, 0, n * 2);
                xy = g;
            }
        }

        private void push(double x, double y) {
            ensureXy(n + 1);
            xy[n * 2] = x;
            xy[n * 2 + 1] = y;
            n++;
        }

        private void endPart() {
            if (partCount == partEnd.length) {
                int[] g = new int[partEnd.length << 1];
                System.arraycopy(partEnd, 0, g, 0, partCount);
                partEnd = g;
            }
            partEnd[partCount++] = n;
        }
    }

    static void clip(double[] src, int nIn,
            double minX, double minY, double maxX, double maxY, Parts out) {
        out.reset();
        if (nIn < 2) {
            return;
        }
        boolean open = false;
        double lastX = 0, lastY = 0;

        for (int i = 0; i < nIn - 1; i++) {
            double ax = src[i * 2], ay = src[i * 2 + 1];
            double bx = src[(i + 1) * 2], by = src[(i + 1) * 2 + 1];

            if (!clipSegment(ax, ay, bx, by, minX, minY, maxX, maxY, out)) {
                if (open) {
                    out.endPart();
                    open = false;
                }
                continue;
            }
            double cx0 = out.sx0, cy0 = out.sy0, cx1 = out.sx1, cy1 = out.sy1;

            if (!open) {
                out.push(cx0, cy0);
                out.push(cx1, cy1);
                open = true;
            } else if (cx0 == lastX && cy0 == lastY) {
                out.push(cx1, cy1);
            } else {
                out.endPart();
                out.push(cx0, cy0);
                out.push(cx1, cy1);
            }
            lastX = cx1;
            lastY = cy1;

            if (cx1 != bx || cy1 != by) {
                out.endPart();
                open = false;
            }
        }
        if (open) {
            out.endPart();
        }
    }

    private static final int OUT_LEFT = 1;
    private static final int OUT_RIGHT = 2;
    private static final int OUT_BOTTOM = 4;
    private static final int OUT_TOP = 8;

    private static int outcode(double x, double y,
            double minX, double minY, double maxX, double maxY) {
        int code = 0;
        if (x < minX) {
            code = OUT_LEFT;
        } else if (x > maxX) {
            code = OUT_RIGHT;
        }
        if (y < minY) {
            code |= OUT_BOTTOM;
        } else if (y > maxY) {
            code |= OUT_TOP;
        }
        return code;
    }

    private static boolean clipSegment(double ax, double ay, double bx, double by,
            double minX, double minY, double maxX, double maxY, Parts out) {
        int ca = outcode(ax, ay, minX, minY, maxX, maxY);
        int cb = outcode(bx, by, minX, minY, maxX, maxY);
        if ((ca | cb) == 0) {
            out.sx0 = ax;
            out.sy0 = ay;
            out.sx1 = bx;
            out.sy1 = by;
            return true;
        }
        if ((ca & cb) != 0) {
            return false;
        }
        return clipSegmentCrossing(ax, ay, bx, by, minX, minY, maxX, maxY, out);
    }

    private static boolean clipSegmentCrossing(double ax, double ay, double bx, double by,
            double minX, double minY, double maxX, double maxY, Parts out) {
        double dx = bx - ax;
        double dy = by - ay;
        double t0 = 0.0, t1 = 1.0;
        double p, q, r;

        p = -dx; q = ax - minX;
        if (p == 0.0) { if (q < 0.0) return false; }
        else { r = q / p; if (p < 0.0) { if (r > t1) return false; if (r > t0) t0 = r; }
                          else         { if (r < t0) return false; if (r < t1) t1 = r; } }
        p = dx; q = maxX - ax;
        if (p == 0.0) { if (q < 0.0) return false; }
        else { r = q / p; if (p < 0.0) { if (r > t1) return false; if (r > t0) t0 = r; }
                          else         { if (r < t0) return false; if (r < t1) t1 = r; } }
        p = -dy; q = ay - minY;
        if (p == 0.0) { if (q < 0.0) return false; }
        else { r = q / p; if (p < 0.0) { if (r > t1) return false; if (r > t0) t0 = r; }
                          else         { if (r < t0) return false; if (r < t1) t1 = r; } }
        p = dy; q = maxY - ay;
        if (p == 0.0) { if (q < 0.0) return false; }
        else { r = q / p; if (p < 0.0) { if (r > t1) return false; if (r > t0) t0 = r; }
                          else         { if (r < t0) return false; if (r < t1) t1 = r; } }

        out.sx0 = ax + t0 * dx;
        out.sy0 = ay + t0 * dy;
        out.sx1 = ax + t1 * dx;
        out.sy1 = ay + t1 * dy;
        return true;
    }

    static int partStart(Parts out, int part) {
        return part == 0 ? 0 : out.partEnd[part - 1];
    }
}
