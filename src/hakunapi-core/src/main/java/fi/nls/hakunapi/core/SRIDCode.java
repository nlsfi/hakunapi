package fi.nls.hakunapi.core;

import fi.nls.hakunapi.core.geom.HakunaGeometryDimension;
import fi.nls.hakunapi.core.schemas.Crs;

public class SRIDCode {

    public static final SRIDCode CRS84 = new SRIDCode(Crs.CRS84_SRID, false, true, HakunaGeometryDimension.XY);
    public static final SRIDCode WGS84 = new SRIDCode(4326, true, true, HakunaGeometryDimension.XY);

    public static boolean isKnown(int srid) {
        return srid == CRS84.srid || srid == WGS84.srid;
    }

    public static SRIDCode getKnown(int srid) {
        return srid == CRS84.srid ? CRS84 : srid == WGS84.srid ? WGS84 : null;
    }

    private final int srid;
    private final boolean latLon;
    private final boolean degrees;
    private final HakunaGeometryDimension dimension;

    public SRIDCode(int srid, boolean latLon, boolean degrees, HakunaGeometryDimension dimension) {
        this.srid = srid;
        this.latLon = latLon;
        this.degrees = degrees;
        this.dimension = dimension;
    }

    public int getSrid() {
        return srid;
    }

    public boolean isLatLon() {
        return latLon;
    }

    public boolean isDegrees() {
        return degrees;
    }

    public HakunaGeometryDimension getDimension() {
        return dimension;
    }

    public SRIDCode withDimension(HakunaGeometryDimension d) {
        return new SRIDCode(this.srid, this.latLon, this.degrees, d);
    }

}
