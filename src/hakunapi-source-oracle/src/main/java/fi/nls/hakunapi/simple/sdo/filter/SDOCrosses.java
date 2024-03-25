package fi.nls.hakunapi.simple.sdo.filter;

public class SDOCrosses extends SDOGeometryFunction {

    @Override
    public String getMask() {
        return "OVERLAPBDYDISJOINT";
    }

}
