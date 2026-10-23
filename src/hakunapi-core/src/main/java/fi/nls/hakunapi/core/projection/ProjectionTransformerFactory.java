package fi.nls.hakunapi.core.projection;

public interface ProjectionTransformerFactory {

    public ProjectionTransformer getTransformer(int fromSRID, int toSRID) throws Exception;
    public ProjectionTransformer toCRS84(int fromSRID) throws Exception;
    public ProjectionTransformer fromCRS84(int toSRID) throws Exception;
    /**
     * @return WKT 1 definition of the CRS, required for every srid this factory can transform
     */
    public String getWKT(int srid) throws Exception;

}
