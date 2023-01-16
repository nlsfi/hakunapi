package fi.nls.hakunapi.core;

public interface FeatureCollectionWriter extends FeatureWriter {

    public void startFeatureCollection(FeatureType ft, String layername) throws Exception;
    public void endFeatureCollection() throws Exception;

    public void startFeature(String fid) throws Exception;
    public void startFeature(long fid) throws Exception;
    public default void startFeature(int fid) throws Exception { startFeature((long) fid); }
    public void endFeature() throws Exception;

}
