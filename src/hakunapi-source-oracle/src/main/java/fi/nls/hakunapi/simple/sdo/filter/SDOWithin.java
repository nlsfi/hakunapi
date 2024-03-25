package fi.nls.hakunapi.simple.sdo.filter;

public class SDOWithin extends SDOGeometryFunction {

    @Override
    public String getMask() {
        return "INSIDE+COVEREDBY";
    }

}
