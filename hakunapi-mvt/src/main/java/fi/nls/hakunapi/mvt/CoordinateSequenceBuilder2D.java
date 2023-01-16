package fi.nls.hakunapi.mvt;

import java.util.Arrays;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFactory;
import org.locationtech.jts.geom.GeometryFactory;

public class CoordinateSequenceBuilder2D implements Sink {

    private static final int DEFAULT_SIZE = 16;
    private static final int THRESHOLD_RESET_ARR_ON_CLEAR = 256;

    private double[] ordinates;
    private int off;
    private double px;
    private double py;

    public CoordinateSequenceBuilder2D() {
        ordinates = new double[DEFAULT_SIZE];
        clear();
    }

    public void add(double x, double y) {
        if (x == px && y == py) {
            return;
        }
        px = x;
        py = y;
        if (off == ordinates.length) {
            ordinates = Arrays.copyOf(ordinates, off * 2);
        }
        ordinates[off++] = x;
        ordinates[off++] = y;
    }

    public boolean isEmpty() {
        return off == 0;
    }

    public void clear() {
        // If the array grew too large for our taste we might want to pass it over to GC
        if (ordinates.length >= THRESHOLD_RESET_ARR_ON_CLEAR) {
            ordinates = new double[DEFAULT_SIZE];
        }
        off = 0;
        px = Double.NaN;
        py = Double.NaN;
    }

    @Override
    public void closePolygon() {
        // NOP
    }

    public CoordinateSequence build(GeometryFactory gf, boolean closeRing, int minSize) {
        if (off < 2) {
            return null;
        }
        boolean willClose = closeRing && (ordinates[0] != ordinates[off - 2] || ordinates[1] != ordinates[off - 1]);
        int size = off / 2;
        if (willClose) {
            size++;
        }
        if (size < minSize) {
            return null;
        }
        CoordinateSequenceFactory csf = gf.getCoordinateSequenceFactory();
        CoordinateSequence cs = csf.create(size, 2, 0);
        for (int i = 0, j = 0; j < off; i++) {
            cs.setOrdinate(i, 0, ordinates[j++]);
            cs.setOrdinate(i, 1, ordinates[j++]);
        }
        if (willClose) {
            cs.setOrdinate(size - 1, 0, ordinates[0]);
            cs.setOrdinate(size - 1, 1, ordinates[1]);
        }
        return cs;
    }

    public int size() {
        return off / 2;
    }

}
