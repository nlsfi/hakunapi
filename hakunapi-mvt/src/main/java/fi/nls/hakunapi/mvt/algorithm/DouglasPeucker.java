package fi.nls.hakunapi.mvt.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.GeometryTransformer;

public class DouglasPeucker extends GeometryTransformer {

    private static final int THRESHOLD_NUM_COORDS_LINE = 4;
    private static final int THRESHOLD_NUM_COORDS_RING = 8;

    private final double eps2;

    public DouglasPeucker(double eps) {
        this.eps2 = eps * eps;
    }

    @Override
    protected CoordinateSequence transformCoordinates(CoordinateSequence coords, Geometry parent) {
        BitSet skipPt = douglasPeucker(coords);
        int numToSkip = skipPt.cardinality();
        if (numToSkip == 0) {
            return coords;
        }
        int numToKeep = coords.size() - numToSkip;
        int dim = coords.getDimension();
        CoordinateSequence csq = factory.getCoordinateSequenceFactory().create(numToKeep, dim, coords.getMeasures());
        for (int n = 0, i = skipPt.nextClearBit(0); n < numToKeep; n++, i = skipPt.nextClearBit(i + 1)) {
            for (int j = 0; j < dim; j++) {
                csq.setOrdinate(n, j, coords.getOrdinate(i, j));
            }
        }
        return csq;
    }

    private BitSet douglasPeucker(CoordinateSequence coords) {
        BitSet skipPt = new BitSet(coords.size());

        int cap = 16;
        int[] stack = new int[cap];
        int size = 0;
        stack[size++] = 0;
        stack[size++] = coords.size() - 1; 

        Coordinate p = new Coordinate();
        Coordinate A = new Coordinate();
        Coordinate B = new Coordinate();

        while (size > 0) {
            int k = stack[--size];
            int j = stack[--size];
            coords.getCoordinate(j, A);
            coords.getCoordinate(k, B);

            double dmax = -1;
            int index = j;

            for (int i = j + 1; i < k; i++) {
                coords.getCoordinate(i, p);
                double d = pointToSegmentSquared(p, A, B);
                if (d > dmax) {
                    dmax = d;
                    index = i;
                }
            }

            if (dmax > eps2) {
                if (size + 4 > cap) {
                    cap *= 2;
                    stack = Arrays.copyOf(stack, cap);
                }
                stack[size++] = index;
                stack[size++] = k;
                stack[size++] = j;
                stack[size++] = index;
            } else {
                skipPt.set(j + 1, k, true);
            }
        }

        return skipPt;
    }

    private static double pointToSegmentSquared(Coordinate p, Coordinate A, Coordinate B) {
        double a = p.x - A.x;
        double b = p.y - A.y;
        double c = B.x - A.x;
        double d = B.y - A.y;

        if (c == 0 && d == 0) {
            return a * a + b * b;
        }

        // double len2 = c * c + d * d;
        // double dot = a * c + b * d;
        double r = (a * c + b * d) / (c * c + d * d);

        double dx;
        double dy;
        if (r <= 0.0) {
            dx = a;
            dy = b;
        } else if (r >= 1.0) {
            dx = p.x - B.x;
            dy = p.y - B.y;
        } else {
            dx = p.x - (A.x + r * c);
            dy = p.y - (A.y + r * d);
        }
        return dx * dx + dy * dy;
    }

    @Override
    protected Geometry transformLineString(LineString geom, Geometry parent) {
        CoordinateSequence original = geom.getCoordinateSequence();
        if (original.size() < THRESHOLD_NUM_COORDS_LINE) {
            // Don't simplify LineStrings that have less than threshold number of coordinates
            return geom;
        }
        CoordinateSequence transformed = transformCoordinates(geom.getCoordinateSequence(), geom);
        if (transformed == null || transformed.size() < 2) {
            return null;
        }
        return factory.createLineString(transformed);
    }

    @Override
    protected Geometry transformLinearRing(LinearRing geom, Geometry parent) {
        CoordinateSequence original = geom.getCoordinateSequence();
        if (original.size() < THRESHOLD_NUM_COORDS_RING) {
            // Don't simplify LinearRings that have less than threshold number of coordinates
            return geom;
        }
        CoordinateSequence seq = transformCoordinates(geom.getCoordinateSequence(), geom);
        if (seq == null) {
            return factory.createLinearRing((CoordinateSequence) null);
        }
        int seqSize = seq.size();
        // ensure a valid LinearRing
        if (seqSize > 0 && seqSize < 4) {
            return null;
        }
        return factory.createLinearRing(seq);
    }

    @Override
    protected Geometry transformPolygon(Polygon geom, Geometry parent) {
        Geometry shell = transformLinearRing((LinearRing) geom.getExteriorRing(), geom);
        if (shell == null || shell.isEmpty() || !(shell instanceof LinearRing)) {
            return null;
        }

        List<LinearRing> holes = new ArrayList<>();
        for (int i = 0; i < geom.getNumInteriorRing(); i++) {
            Geometry hole = transformLinearRing((LinearRing) geom.getInteriorRingN(i), geom);
            if (hole != null && !hole.isEmpty() && hole instanceof LinearRing) {
                holes.add((LinearRing) hole);
            }
        }

        Geometry g = factory.createPolygon((LinearRing) shell, holes.toArray(new LinearRing[holes.size()]));
        return g.buffer(0);
    }

}
