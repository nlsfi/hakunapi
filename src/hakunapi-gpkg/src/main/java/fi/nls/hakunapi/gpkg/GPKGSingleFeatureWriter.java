package fi.nls.hakunapi.gpkg;

import java.io.File;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.SingleFeatureWriter;
import fi.nls.hakunapi.core.geom.HakunaGeometry;
import fi.nls.hakunapi.core.schemas.Link;

public class GPKGSingleFeatureWriter extends GPKGFeatureWriter implements SingleFeatureWriter {

    public GPKGSingleFeatureWriter(File dir) {
        super(dir);
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void startFeature(FeatureType ft, String layername, String fid) throws Exception {
        
    }

    @Override
    public void startFeature(FeatureType ft, String layername, long fid) throws Exception {
        
    }

    @Override
    public void endFeature() throws Exception {
        
    }

    @Override
    public void end(boolean timeStamp, List<Link> links, int numberReturned) throws Exception {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void writeGeometry(String name, HakunaGeometry geometry) throws Exception {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void writeProperty(String name, String value) throws Exception {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void writeProperty(String name, LocalDate value) throws Exception {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void writeProperty(String name, Instant value) throws Exception {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void writeProperty(String name, boolean value) throws Exception {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void writeProperty(String name, int value) throws Exception {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void writeProperty(String name, long value) throws Exception {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void writeProperty(String name, float value) throws Exception {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void writeProperty(String name, double value) throws Exception {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void writeNullProperty(String name) throws Exception {
        // TODO Auto-generated method stub
        
    }

}
