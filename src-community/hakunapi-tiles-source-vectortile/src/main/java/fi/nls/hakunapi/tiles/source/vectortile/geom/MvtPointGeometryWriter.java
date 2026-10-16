package fi.nls.hakunapi.tiles.source.vectortile.geom;

public final class MvtPointGeometryWriter extends AbstractMvtGeometryWriter {
    private final MvtGeometryEncoder enc = new MvtGeometryEncoder();

    private static final int RETAINED_PTS = 4096;

    private double[] pts = new double[64];
    private int nPts;

    private void accept(double tx, double ty) {
        if (tx < clipMinX || tx > clipMaxX || ty < clipMinY || ty > clipMaxY) {
            return;
        }
        double sx = Math.rint(tx);
        double sy = Math.rint(ty);
        if (nPts > 0 && pts[(nPts - 1) * 2] == sx && pts[(nPts - 1) * 2 + 1] == sy) {
            return;
        }
        int need = (nPts + 1) * 2;
        if (need > pts.length) {
            pts = growKeeping(pts, need);
        }
        pts[nPts * 2] = sx;
        pts[nPts * 2 + 1] = sy;
        nPts++;
    }

    public void writeNavigable(fi.nls.hakunapi.core.geom.NavigableHakunaGeometry geom) throws Exception {
        int parts = geom.getNumGeometries();
        for (int p = 0; p < parts; p++) {
            fi.nls.hakunapi.core.geom.NavigableHakunaGeometry part = geom.getGeometryN(p);
            loadRing(part, 0);
            for (int i = 0; i < n; i++) {
                accept(scratch[i * 2], scratch[i * 2 + 1]);
            }
            resetRing();
        }
    }

    public void end() {
        if (nPts == 0) {
            return;
        }
        enc.moveToRun(pts, nPts);
    }

    public void resetFeature() {
        enc.reset();
        nPts = 0;
    }

    @Override
    public void trimScratch() {
        super.trimScratch();
        if (pts.length > RETAINED_PTS * 2) {
            pts = new double[RETAINED_PTS * 2];
        }
    }

    public MvtGeometryEncoder encoder() {
        return enc;
    }
}
