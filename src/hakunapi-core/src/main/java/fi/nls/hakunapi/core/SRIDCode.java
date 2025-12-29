package fi.nls.hakunapi.core;

import fi.nls.hakunapi.core.geom.HakunaGeometryDimension;
import fi.nls.hakunapi.core.schemas.Crs;

public class SRIDCode {

    // Geographic wrap bounds (degrees)
    private static final double GEO_WRAP_X_MIN = -180.0;
    private static final double GEO_WRAP_X_MAX = 180.0;

    public static final SRIDCode CRS84 = new SRIDCode(Crs.CRS84_SRID, false, true, HakunaGeometryDimension.XY, GEO_WRAP_X_MIN, GEO_WRAP_X_MAX);
    public static final SRIDCode WGS84 = new SRIDCode(4326, true, true, HakunaGeometryDimension.XY, GEO_WRAP_X_MIN, GEO_WRAP_X_MAX);

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
    private final Double wrapXMin;
    private final Double wrapXMax;

    public SRIDCode(int srid, boolean latLon, boolean degrees, HakunaGeometryDimension dimension) {
        this(srid, latLon, degrees, dimension, null, null);
    }

    public SRIDCode(int srid, boolean latLon, boolean degrees, HakunaGeometryDimension dimension,
                    Double wrapXMin, Double wrapXMax) {
        this.srid = srid;
        this.latLon = latLon;
        this.degrees = degrees;
        this.dimension = dimension;
        this.wrapXMin = wrapXMin;
        this.wrapXMax = wrapXMax;
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

    public Double getWrapXMin() {
        return wrapXMin;
    }

    public Double getWrapXMax() {
        return wrapXMax;
    }

    public boolean supportsWrapX() {
        return wrapXMin != null && wrapXMax != null;
    }

    public SRIDCode withDimension(HakunaGeometryDimension d) {
        return new SRIDCode(this.srid, this.latLon, this.degrees, d, this.wrapXMin, this.wrapXMax);
    }

}
