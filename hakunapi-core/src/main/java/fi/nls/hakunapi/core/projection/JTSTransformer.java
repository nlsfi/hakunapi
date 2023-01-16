package fi.nls.hakunapi.core.projection;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFilter;

public class JTSTransformer implements CoordinateSequenceFilter {

    private final ProjectionTransformer t;
    private final double[] arr;
    private final int dimension;

    public JTSTransformer(ProjectionTransformer transformer) {
        this.t = transformer;
        this.arr = new double[transformer.getDimension()];
        this.dimension = transformer.getDimension();
    }

    @Override
    public void filter(CoordinateSequence seq, int i) {
        for (int d = 0; d < dimension; d++) {
            arr[d] = seq.getOrdinate(i, d);
        }
        try {
            t.transformInPlace(arr, 0, 1);
        } catch (Exception e) {
            throw new RuntimeException("Projection transform failed", e);
        }
        for (int d = 0; d < dimension; d++) {
            seq.setOrdinate(i, d, arr[d]);
        }
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
