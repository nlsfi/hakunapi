package fi.nls.hakunapi.mvt;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFilter;

/**
 * This translates the real-world coordinates of a geometry to the inner
 * virtual-coordinate space of the tile. Inside the tile a coordinate consists
 * of two integers (x,y) both in the range of [0,extent] (usually extent=4096).
 * 
 * As a result of this snapping to integer grid, vertices of the original
 * geometry might snap to the same point thereby creating duplicate points.
 */
public class ToMVTSpace implements CoordinateSequenceFilter {

    private final double tx;
    private final double ty;
    private final double sx;
    private final double sy;

    public ToMVTSpace(double tx, double ty, double sx, double sy) {
        this.tx = tx;
        this.ty = ty;
        this.sx = sx;
        this.sy = sy;
    }

    @Override
    public void filter(CoordinateSequence seq, int i) {
        seq.setOrdinate(i, 0, Math.round(sx * (seq.getOrdinate(i, 0) + tx)));
        seq.setOrdinate(i, 1, Math.round(sy * (seq.getOrdinate(i, 1) + ty)));
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean isGeometryChanged() {
        return true;
    }

}
