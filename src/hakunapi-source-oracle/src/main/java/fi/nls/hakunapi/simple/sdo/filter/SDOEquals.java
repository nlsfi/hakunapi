package fi.nls.hakunapi.simple.sdo.filter;

public class SDOEquals extends SDOGeometryFunction {

    @Override
    public String getMask() {
        return "EQUAL";
    }

}
