package fi.nls.hakunapi.simple.postgis.filter;

public class PostGISWithin extends PostGISGeometryFunction {
    
    @Override
    public String getFunctionName() {
        return "ST_Within";
    }

}
