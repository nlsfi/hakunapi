package fi.nls.hakunapi.simple.sdo.filter;

public class SDOTouches extends SDOGeometryFunction {

    @Override
    public String getMask() {
        return "TOUCH";
    }

}