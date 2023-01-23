package fi.nls.hakunapi.core.projection;

public interface ProjectionTransformerFactory {

    public ProjectionTransformer getTransformer(int fromSRID, int toSRID) throws Exception;
    public ProjectionTransformer toCRS84(int fromSRID) throws Exception;
    public ProjectionTransformer fromCRS84(int toSRID) throws Exception;

}
