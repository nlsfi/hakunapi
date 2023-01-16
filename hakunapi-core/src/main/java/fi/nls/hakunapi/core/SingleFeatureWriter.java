package fi.nls.hakunapi.core;

public interface SingleFeatureWriter extends FeatureWriter {
    
    public void startFeature(FeatureType ft, String layername, String fid) throws Exception;
    public void startFeature(FeatureType ft, String layername, long fid) throws Exception;
    public void endFeature() throws Exception;

}
