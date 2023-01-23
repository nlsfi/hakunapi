package fi.nls.hakunapi.simple.postgis.filter;

public class PostGISTouches extends PostGISGeometryFunction {
    
    @Override
    public String getFunctionName() {
        return "ST_Touches";
    }

}
