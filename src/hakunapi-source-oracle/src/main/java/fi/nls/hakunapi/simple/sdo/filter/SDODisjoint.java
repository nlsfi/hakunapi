package fi.nls.hakunapi.simple.sdo.filter;


public class SDODisjoint extends SDOGeometryFunction {

    @Override
    public String getMask() {
        return "DISJOINT";
    }

}
