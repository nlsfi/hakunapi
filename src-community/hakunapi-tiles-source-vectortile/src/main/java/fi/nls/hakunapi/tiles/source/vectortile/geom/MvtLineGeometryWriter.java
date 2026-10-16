package fi.nls.hakunapi.tiles.source.vectortile.geom;

public final class MvtLineGeometryWriter extends AbstractMvtGeometryWriter {
    private final MvtGeometryEncoder enc = new MvtGeometryEncoder();
    private final LineClip.Parts parts = new LineClip.Parts();

    public void writeNavigable(fi.nls.hakunapi.core.geom.NavigableHakunaGeometry geom) throws Exception {
        int n = geom.getNumGeometries();
        for (int p = 0; p < n; p++) {
            loadRing(geom.getGeometryN(p), 0);
            if (this.n > 0) {
                processLine();
                resetRing();
            }
        }
    }

    private void processLine() {
        if (ringBboxValid && (ringMaxX < clipMinX || ringMinX > clipMaxX
                || ringMaxY < clipMinY || ringMinY > clipMaxY)) {
            return;
        }
        if (ringBboxValid && ringMinX >= clipMinX && ringMaxX <= clipMaxX
                && ringMinY >= clipMinY && ringMaxY <= clipMaxY) {
            emitPart(scratch, 0, n);
            return;
        }
        LineClip.clip(scratch, n, clipMinX, clipMinY, clipMaxX, clipMaxY, parts);
        for (int part = 0; part < parts.partCount; part++) {
            int start = LineClip.partStart(parts, part);
            emitPart(parts.xy, start, parts.partEnd[part] - start);
        }
    }

    private void emitPart(double[] src, int start, int m) {
        if (m < 2) {
            return;
        }
        ensureB(m);
        System.arraycopy(src, start * 2, scratchB, 0, m * 2);
        m = SnapSimplify.snapDedupLine(scratchB, m);
        m = SnapSimplify.dropCollinearLine(scratchB, m);
        if (m < 2) {
            return;
        }
        enc.moveTo((int) scratchB[0], (int) scratchB[1]);
        enc.lineTo(scratchB, 1, m - 1, +1);
    }

    @Override
    public void trimScratch() {
        super.trimScratch();
        parts.trim();
    }

    public void resetFeature() {
        enc.reset();
        resetRing();
    }

    public MvtGeometryEncoder encoder() {
        return enc;
    }
}
