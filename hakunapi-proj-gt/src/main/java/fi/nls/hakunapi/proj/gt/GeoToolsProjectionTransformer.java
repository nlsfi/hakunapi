package fi.nls.hakunapi.proj.gt;

import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import fi.nls.hakunapi.core.projection.ProjectionTransformer;

public class GeoToolsProjectionTransformer implements ProjectionTransformer {

    private final int fromSRID;
    private final int toSRID;
    private final MathTransform transform;

    public GeoToolsProjectionTransformer(int fromSRID, int toSRID, MathTransform transform) {
        this.fromSRID = fromSRID;
        this.toSRID = toSRID;
        this.transform = transform;
    }

    @Override
    public void transformInPlace(double[] coords, int off, int n) throws TransformException {
        transform.transform(coords, off, coords, off, n);
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
    public int getDimension() {
        return transform.getTargetDimensions();
    }

}
