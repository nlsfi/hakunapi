package fi.nls.hakunapi.simple.sdo.filter;

public class SDOOverlaps extends SDOGeometryFunction {

    @Override
    public String getMask() {
        return "OVERLAPBDYINTERSECT";
    }

    
}
