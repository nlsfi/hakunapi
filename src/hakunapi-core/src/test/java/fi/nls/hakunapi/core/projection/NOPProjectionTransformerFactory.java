package fi.nls.hakunapi.core.projection;

public class NOPProjectionTransformerFactory implements ProjectionTransformerFactory {

    @Override
    public ProjectionTransformer getTransformer(int fromSRID, int toSRID) {
        return NOPProjectionTransformer.INSTANCE;
    }

    @Override
    public ProjectionTransformer toCRS84(int fromSRID) {
        return NOPProjectionTransformer.INSTANCE;
    }

    @Override
    public ProjectionTransformer fromCRS84(int toSRID) {
        return NOPProjectionTransformer.INSTANCE;
    }

    @Override
    public String getWKT(int srid) {
        throw new UnsupportedOperationException();
    }

}
