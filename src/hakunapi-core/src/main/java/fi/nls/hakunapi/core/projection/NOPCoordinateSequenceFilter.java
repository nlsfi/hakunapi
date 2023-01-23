package fi.nls.hakunapi.core.projection;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFilter;

public class NOPCoordinateSequenceFilter implements CoordinateSequenceFilter {

    public static NOPCoordinateSequenceFilter INSTANCE = new NOPCoordinateSequenceFilter();

    public NOPCoordinateSequenceFilter() {
        // Use INSTANCE
    }

    @Override
    public void filter(CoordinateSequence seq, int i) {
        // NOP
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public boolean isGeometryChanged() {
        return false;
    }

}
