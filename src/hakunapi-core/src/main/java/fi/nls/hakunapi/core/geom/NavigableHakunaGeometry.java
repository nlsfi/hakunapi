package fi.nls.hakunapi.core.geom;

public interface NavigableHakunaGeometry extends HakunaGeometry {

    /** Members of a MULTI*, otherwise 1. */
    int getNumGeometries();

    /** For a simple geometry only n = 0 is valid, returning a view of itself. */
    NavigableHakunaGeometry getGeometryN(int n);

    /** Polygon exterior plus holes, otherwise 1. */
    int getNumRings();

    int getRingSize(int r);

    /** Writes {@code 2 * count} doubles as x,y pairs into dst from dstOff; z/m are dropped. */
    void copyRingXY(int r, double[] dst, int dstOff, int from, int count);

    @Override
    default NavigableHakunaGeometry toNavigable() {
        return this;
    }
}
