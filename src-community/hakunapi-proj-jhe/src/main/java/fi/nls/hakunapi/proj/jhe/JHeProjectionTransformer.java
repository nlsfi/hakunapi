package fi.nls.hakunapi.proj.jhe;

import fi.nls.hakunapi.core.projection.ProjectionTransformer;

public class JHeProjectionTransformer implements ProjectionTransformer {

    private final int fromSRID;
    private final int toSRID;
    private final JHeMathTransform transform;

    public JHeProjectionTransformer(int fromSRID, int toSRID, JHeMathTransform transform) {
        this.fromSRID = fromSRID;
        this.toSRID = toSRID;
        this.transform = transform;
    }

    @Override
    public int getFromSRID() {
        return fromSRID;
    }

    @Override
    public int getToSRID() {
        return toSRID;
    }

    @Override
    public void transformInPlace(double[] coords, int off, int n) throws Exception {
        transform.transform(coords, off, coords, off, n);
    }

    @Override
    public int getDimension() {
        return 2;
    }
}
