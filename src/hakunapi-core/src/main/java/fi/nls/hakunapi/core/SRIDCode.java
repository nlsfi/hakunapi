package fi.nls.hakunapi.core;

public class SRIDCode {

    private final int srid;
    private final boolean latLon;

    public SRIDCode(int srid, boolean latLon) {
        this.srid = srid;
        this.latLon = latLon;
    }

    public int getSrid() {
        return srid;
    }

    public boolean isLatLon() {
        return latLon;
    }

}
