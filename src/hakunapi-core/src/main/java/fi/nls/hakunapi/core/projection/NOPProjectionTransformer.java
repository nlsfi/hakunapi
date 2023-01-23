package fi.nls.hakunapi.core.projection;

public class NOPProjectionTransformer implements ProjectionTransformer {

    public static final NOPProjectionTransformer INSTANCE = new NOPProjectionTransformer();

    @Override
    public void transformInPlace(double[] coords, int off, int n) throws Exception {
        // NOP
    }

    private NOPProjectionTransformer() {
        // NOPProjectionTransformer.INSTANCE
    }

    @Override
    public int getFromSRID() {
        return 0;
    }

    @Override
    public int getToSRID() {
        return 0;
    }

    @Override
    public int getDimension() {
        return 0;
    }

    @Override
    public boolean isNOP() {
        return true;
    }

}
