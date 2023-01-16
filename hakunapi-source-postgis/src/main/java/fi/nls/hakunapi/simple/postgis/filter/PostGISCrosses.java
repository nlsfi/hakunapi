package fi.nls.hakunapi.simple.postgis.filter;

public class PostGISCrosses extends PostGISGeometryFunction {
    
    @Override
    public String getFunctionName() {
        return "ST_Crosses";
    }

}
