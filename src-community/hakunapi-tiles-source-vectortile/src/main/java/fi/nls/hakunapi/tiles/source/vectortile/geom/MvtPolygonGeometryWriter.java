package fi.nls.hakunapi.tiles.source.vectortile.geom;

public final class MvtPolygonGeometryWriter extends AbstractMvtGeometryWriter {
    private final MvtGeometryEncoder enc = new MvtGeometryEncoder();
    private final SnapSimplify.RingResult ringResult = new SnapSimplify.RingResult();

    private final double[] square = new double[8];

    boolean coveringFastPath = true;

    private boolean expectExterior;
    private double exteriorArea;

    public void writeNavigable(fi.nls.hakunapi.core.geom.NavigableHakunaGeometry geom) throws Exception {
        int parts = geom.getNumGeometries();
        for (int p = 0; p < parts; p++) {
            fi.nls.hakunapi.core.geom.NavigableHakunaGeometry part = geom.getGeometryN(p);
            int rings = part.getNumRings();
            exteriorArea = 0.0;
            for (int r = 0; r < rings; r++) {
                loadRing(part, r);
                expectExterior = (r == 0);
                processRing();
                resetRing();
            }
        }
    }

    private boolean emitAsCoveringSquare() {
        if (!ringBboxValid || n < 3
                || ringMinX > clipMinX || ringMaxX < clipMaxX
                || ringMinY > clipMinY || ringMaxY < clipMaxY) {
            return false;
        }
        double turn = convexTurn();
        if (turn == 0.0) {
            return false;
        }
        if (!containsRectCorners()) {
            return false;
        }
        boolean reverse;
        if (isExteriorNow()) {
            exteriorArea = turn;
            reverse = false;
        } else {
            reverse = sameSign(turn, exteriorArea);
        }
        coveringSquare(turn > 0);
        enc.ring(square, 0, 4, reverse);
        return true;
    }

    private boolean isExteriorNow() {
        boolean isExterior = expectExterior;
        expectExterior = false;
        return isExterior;
    }

    private double convexTurn() {
        int sign = 0;
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            int k = (i + 2) % n;
            double ax = scratch[j * 2] - scratch[i * 2];
            double ay = scratch[j * 2 + 1] - scratch[i * 2 + 1];
            double bx = scratch[k * 2] - scratch[j * 2];
            double by = scratch[k * 2 + 1] - scratch[j * 2 + 1];
            double cross = ax * by - ay * bx;
            if (cross == 0.0) {
                return 0.0;
            }
            int s = cross > 0 ? 1 : -1;
            if (sign == 0) {
                sign = s;
            } else if (sign != s) {
                return 0.0;
            }
        }
        return sign;
    }

    private boolean containsRectCorners() {
        return containsPoint(clipMinX, clipMinY) && containsPoint(clipMaxX, clipMinY)
                && containsPoint(clipMaxX, clipMaxY) && containsPoint(clipMinX, clipMaxY);
    }

    private boolean containsPoint(double px, double py) {
        boolean in = false;
        for (int i = 0, j = n - 1; i < n; j = i++) {
            double xi = scratch[i * 2], yi = scratch[i * 2 + 1];
            double xj = scratch[j * 2], yj = scratch[j * 2 + 1];
            if (((yi > py) != (yj > py)) && (px < (xj - xi) * (py - yi) / (yj - yi) + xi)) {
                in = !in;
            }
        }
        return in;
    }

    private void coveringSquare(boolean ccw) {
        double x0 = clipMinX, y0 = clipMinY, x1 = clipMaxX, y1 = clipMaxY;
        if (ccw) {
            square[0] = x0; square[1] = y0;
            square[2] = x1; square[3] = y0;
            square[4] = x1; square[5] = y1;
            square[6] = x0; square[7] = y1;
        } else {
            square[0] = x0; square[1] = y0;
            square[2] = x0; square[3] = y1;
            square[4] = x1; square[5] = y1;
            square[6] = x1; square[7] = y0;
        }
    }

    private void processRing() {
        double[] clipped;
        int m;
        if (ringBboxValid && (ringMaxX < clipMinX || ringMinX > clipMaxX
                || ringMaxY < clipMinY || ringMinY > clipMaxY)) {
            return;
        }
        if (coveringFastPath && emitAsCoveringSquare()) {
            return;
        }
        ensureB(n + 4);
        if (ringBboxValid && ringMinX >= clipMinX && ringMaxX <= clipMaxX
                && ringMinY >= clipMinY && ringMaxY <= clipMaxY) {
            clipped = scratch;
            m = n;
        } else {
            clipped = scratchB;
            m = ringBboxValid
                    ? RingClip.clip(scratch, n, clipMinX, clipMinY, clipMaxX, clipMaxY, clipped, scratchC,
                            ringMinX, ringMinY, ringMaxX, ringMaxY)
                    : RingClip.clip(scratch, n, clipMinX, clipMinY, clipMaxX, clipMaxY, clipped, scratchC);
            if (m == 0) {
                return;
            }
        }
        long snapped = SnapSimplify.snapDedupRingTracked(clipped, m);
        m = SnapSimplify.trackedCount(snapped);
        int start = SnapSimplify.trackedStart(snapped);
        double[] out = (clipped == scratchB) ? scratchC : scratchB;
        SnapSimplify.dropCollinearRing(clipped, m, start, out, ringResult);
        m = ringResult.n;
        clipped = out;
        if (m < 3) {
            return;
        }
        double area = ringResult.area;
        if (area == 0.0) {
            return;
        }

        boolean isExterior = isExteriorNow();
        boolean reverse;
        if (isExterior) {
            exteriorArea = area;
            reverse = false;
        } else {
            reverse = sameSign(area, exteriorArea);
        }
        enc.ring(clipped, 0, m, reverse);
    }

    private static boolean sameSign(double a, double b) {
        return (a > 0) == (b > 0);
    }

    public void resetFeature() {
        enc.reset();
        expectExterior = false;
        exteriorArea = 0.0;
        resetRing();
    }

    public MvtGeometryEncoder encoder() {
        return enc;
    }
}
