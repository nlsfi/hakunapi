package fi.nls.hakunapi.tiles.source.vectortile.geom;

/** Clips rings to a box. */
final class RingClip {
    private RingClip() {}

    private static final int LEFT = 0;
    private static final int RIGHT = 1;
    private static final int BOTTOM = 2;
    private static final int TOP = 3;

    static int clip(double[] src, int nIn,
            double minX, double minY, double maxX, double maxY,
            double[] dst, double[] tmp) {
        return clip(src, nIn, minX, minY, maxX, maxY, dst, tmp,
                Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    static int clip(double[] src, int nIn,
            double minX, double minY, double maxX, double maxY,
            double[] dst, double[] tmp,
            double ringMinX, double ringMinY, double ringMaxX, double ringMaxY) {
        if (nIn == 0) {
            return 0;
        }
        double[] in = src;
        double[] out = dst;
        int n = nIn;

        if (ringMinX < minX) {
            n = clipEdge(in, n, LEFT, minX, minY, maxX, maxY, out);
            if (n == 0) return 0;
            in = out;
            out = (out == dst) ? tmp : dst;
        }
        if (ringMaxX > maxX) {
            n = clipEdge(in, n, RIGHT, minX, minY, maxX, maxY, out);
            if (n == 0) return 0;
            in = out;
            out = (out == dst) ? tmp : dst;
        }
        if (ringMinY < minY) {
            n = clipEdge(in, n, BOTTOM, minX, minY, maxX, maxY, out);
            if (n == 0) return 0;
            in = out;
            out = (out == dst) ? tmp : dst;
        }
        if (ringMaxY > maxY) {
            n = clipEdge(in, n, TOP, minX, minY, maxX, maxY, out);
            if (n == 0) return 0;
            in = out;
        }
        if (in != dst) {
            System.arraycopy(in, 0, dst, 0, n * 2);
        }
        return n;
    }

    private static boolean inside(int edge, double x, double y,
            double minX, double minY, double maxX, double maxY) {
        switch (edge) {
        case LEFT:   return x >= minX;
        case RIGHT:  return x <= maxX;
        case BOTTOM: return y >= minY;
        default:     return y <= maxY;
        }
    }

    private static double interpX(double ax, double ay, double bx, double by, double edgeX) {
        double t = (edgeX - ax) / (bx - ax);
        return ay + t * (by - ay);
    }

    private static double interpY(double ax, double ay, double bx, double by, double edgeY) {
        double t = (edgeY - ay) / (by - ay);
        return ax + t * (bx - ax);
    }

    private static int clipEdge(double[] in, int nIn, int edge,
            double minX, double minY, double maxX, double maxY, double[] out) {
        int o = 0;
        double px = in[(nIn - 1) * 2];
        double py = in[(nIn - 1) * 2 + 1];
        boolean pIn = inside(edge, px, py, minX, minY, maxX, maxY);
        for (int i = 0; i < nIn; i++) {
            double cx = in[i * 2];
            double cy = in[i * 2 + 1];
            boolean cIn = inside(edge, cx, cy, minX, minY, maxX, maxY);
            if (cIn) {
                if (!pIn) {
                    o = emitCross(out, o, edge, px, py, cx, cy, minX, minY, maxX, maxY);
                }
                out[o * 2] = cx;
                out[o * 2 + 1] = cy;
                o++;
            } else if (pIn) {
                o = emitCross(out, o, edge, px, py, cx, cy, minX, minY, maxX, maxY);
            }
            px = cx;
            py = cy;
            pIn = cIn;
        }
        return o;
    }

    private static int emitCross(double[] out, int o, int edge,
            double px, double py, double cx, double cy,
            double minX, double minY, double maxX, double maxY) {
        double ix, iy;
        switch (edge) {
        case LEFT:
            ix = minX; iy = interpX(px, py, cx, cy, minX); break;
        case RIGHT:
            ix = maxX; iy = interpX(px, py, cx, cy, maxX); break;
        case BOTTOM:
            iy = minY; ix = interpY(px, py, cx, cy, minY); break;
        default:
            iy = maxY; ix = interpY(px, py, cx, cy, maxY); break;
        }
        out[o * 2] = ix;
        out[o * 2 + 1] = iy;
        return o + 1;
    }
}
