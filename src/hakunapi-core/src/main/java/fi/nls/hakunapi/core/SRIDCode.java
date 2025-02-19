package fi.nls.hakunapi.core;

import fi.nls.hakunapi.core.geom.HakunaGeometryDimension;

public class SRIDCode {

    private final int srid;
    private final boolean latLon;
    private final HakunaGeometryDimension sridDefaultDimension;

    public SRIDCode(int srid, boolean latLon, HakunaGeometryDimension dim) {
        this.srid = srid;
        this.latLon = latLon;
        this.sridDefaultDimension = dim;
    }

    public int getSrid() {
        return srid;
    }

    public boolean isLatLon() {
        return latLon;
    }

    public HakunaGeometryDimension getOrDefaultDimension(HakunaGeometryDimension ftDefaultDimension) {
        switch(ftDefaultDimension) {
        case EPSG: // if configured .geometryDimension=EPSG, let's return sridDefaultDimension when available
            return sridDefaultDimension != null ? sridDefaultDimension : ftDefaultDimension;
        case GEOMETRY: 
        case XY:
        case XYZ:
        case XYZM:
        case DEFAULT:
        default:
            return ftDefaultDimension;
        }
    }
    
    

}
