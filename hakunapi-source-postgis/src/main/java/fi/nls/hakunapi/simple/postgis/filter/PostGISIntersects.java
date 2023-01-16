package fi.nls.hakunapi.simple.postgis.filter;

public class PostGISIntersects extends PostGISGeometryFunction {

    @Override
    public String getFunctionName() {
        return "ST_Intersects";
    }

}
