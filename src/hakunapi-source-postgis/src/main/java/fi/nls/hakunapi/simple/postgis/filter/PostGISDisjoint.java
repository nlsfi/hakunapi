package fi.nls.hakunapi.simple.postgis.filter;

public class PostGISDisjoint extends PostGISGeometryFunction {
    
    @Override
    public String getFunctionName() {
        return "ST_Disjoint";
    }

}
