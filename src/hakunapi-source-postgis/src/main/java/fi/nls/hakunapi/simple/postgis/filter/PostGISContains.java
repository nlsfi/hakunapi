package fi.nls.hakunapi.simple.postgis.filter;

public class PostGISContains extends PostGISGeometryFunction {
    
    @Override
    public String getFunctionName() {
        return "ST_Contains";
    }

}
