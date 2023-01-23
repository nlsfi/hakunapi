package fi.nls.hakunapi.core.projection;

public interface ProjectionTransformer {

    public int getDimension();
    public void transformInPlace(double[] coords, int off, int n) throws Exception;
    public int getFromSRID();
    public int getToSRID();
    public default boolean isNOP() { return getFromSRID() == getToSRID(); }

}
