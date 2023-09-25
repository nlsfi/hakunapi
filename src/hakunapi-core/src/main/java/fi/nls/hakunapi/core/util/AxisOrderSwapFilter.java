package fi.nls.hakunapi.core.util;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFilter;

public class AxisOrderSwapFilter implements CoordinateSequenceFilter {

    @Override
    public void filter(CoordinateSequence seq, int i) {
        final double x = seq.getX(i);
        final double y = seq.getY(i);
        seq.setOrdinate(i, CoordinateSequence.X, y);
        seq.setOrdinate(i, CoordinateSequence.Y, x);
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
