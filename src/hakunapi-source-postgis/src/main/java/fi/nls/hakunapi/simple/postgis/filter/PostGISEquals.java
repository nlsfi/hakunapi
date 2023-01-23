package fi.nls.hakunapi.simple.postgis.filter;

public class PostGISEquals extends PostGISGeometryFunction {
    
    @Override
    public String getFunctionName() {
        return "ST_Equals";
    }

}
