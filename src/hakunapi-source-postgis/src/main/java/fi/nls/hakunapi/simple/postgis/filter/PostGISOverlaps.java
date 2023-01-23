package fi.nls.hakunapi.simple.postgis.filter;

public class PostGISOverlaps extends PostGISGeometryFunction {
    
    @Override
    public String getFunctionName() {
        return "ST_Overlaps";
    }

}
