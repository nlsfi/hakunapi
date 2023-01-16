package fi.nls.hakunapi.mvt;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.util.GeometryTransformer;

import fi.nls.hakunapi.core.geom.HakunaGeometryFactory;
import fi.nls.hakunapi.core.util.SimplifyAlgorithm;
import fi.nls.hakunapi.mvt.algorithm.CohenSutherland;
import fi.nls.hakunapi.mvt.algorithm.DouglasPeucker;
import fi.nls.hakunapi.mvt.algorithm.SutherlandHodgman;
import fi.nls.hakunapi.mvt.algorithm.VisvalingamWhyatt;

public class MVTGeometryEncoder {

    private static final GeometryFactory GF = HakunaGeometryFactory.GF;

    private final Envelope tileEnvelope;
    private final Envelope clipEnvelope;
    private final GeometryTransformer simplify;
    private final ToMVTSpace toMVTSpace;
    private final GeometryRectClipper clipper;

    public MVTGeometryEncoder(Envelope tileEnvelope, int extent, int buffer, double tolerance, SimplifyAlgorithm alg) {
        this.tileEnvelope = new Envelope(tileEnvelope);
        this.clipEnvelope = new Envelope(tileEnvelope);
        if (buffer > 0) {
            double bufferSizePercent = (double) buffer / extent;
            double deltaX = bufferSizePercent * tileEnvelope.getWidth();
            double deltaY = bufferSizePercent * tileEnvelope.getHeight();
            clipEnvelope.expandBy(deltaX, deltaY);
        }
        
        CohenSutherland lineClipper = new CohenSutherland(clipEnvelope);
        SutherlandHodgman polygonClipper = new SutherlandHodgman(clipEnvelope);
        this.clipper = new GeometryRectClipper(lineClipper, polygonClipper);

        switch (alg) {
        case DP:
            simplify = new DouglasPeucker(tolerance);
            break;
        case VW:
            simplify = new VisvalingamWhyatt(tolerance);
            break;
        default:
            throw new IllegalArgumentException();
        }

        double tx = -tileEnvelope.getMinX();
        double ty = -tileEnvelope.getMaxY();
        double sx = (double) extent / tileEnvelope.getWidth();
        double sy = -((double) extent / tileEnvelope.getHeight());
        this.toMVTSpace = new ToMVTSpace(tx, ty, sx, sy);
    }

    public Geometry toMVTGeom(Geometry geom) {
        // Skip null and empty geometries
        if (geom == null || geom.isEmpty()) {
            return null;
        }

        geom = multiGeometriesWithOneGeometryToSingle(geom);

        // Handle points and MultiPoints first
        if (geom instanceof Point) {
            return toMVTGeom((Point) geom);
        } else if (geom instanceof MultiPoint) {
            return toMVTGeom((MultiPoint) geom);
        }

        // For other geometry types remove parts that are obviously disjoint with the tiles envelope
        // (comparing only envelopes)
        geom = removeObviouslyDisjointParts(geom);
        if (geom == null || geom.isEmpty()) {
            return null;
        }

        // Clip the geometry
        geom = clipper.transform(geom);
        if (geom == null || geom.isEmpty()) {
            return null;
        }

        // Snap the geometry to MVT grid (virtual integer coordinates)
        geom.apply(toMVTSpace);

        // Simplify the geometry
        geom = simplify.transform(geom);
        // Which might cause it to disappear
        if (geom == null || geom.isEmpty()) {
            return null;
        }

        return geom;
    }

    private Geometry multiGeometriesWithOneGeometryToSingle(Geometry geom) {
        if (geom instanceof GeometryCollection) {
            GeometryCollection c = (GeometryCollection) geom;
            if (c.getNumGeometries() == 1) {
                return c.getGeometryN(0);
            }
        }
        return geom;
    }

    private Geometry toMVTGeom(Point p) {
        CoordinateSequence cs = p.getCoordinateSequence();
        if (!clipEnvelope.covers(cs.getX(0), cs.getY(0))) {
            return null;
        }
        p.apply(toMVTSpace);
        return p;
    }

    private Geometry toMVTGeom(MultiPoint mp) {
        Geometry geom = removePointsOutsideOfEnvelope(clipEnvelope, mp);
        if (geom == null || geom.isEmpty()) {
            return null;
        }
        geom.apply(toMVTSpace);
        return geom;
    }

    private Geometry removePointsOutsideOfEnvelope(Envelope envelope, MultiPoint mp) {
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < mp.getNumGeometries(); i++) {
            Point p = (Point) mp.getGeometryN(i);
            CoordinateSequence cs = p.getCoordinateSequence();
            if (envelope.covers(cs.getX(0), cs.getY(0))) {
                points.add(p);
            }
        }
        int len = points.size();
        if (len == 0) {
            return null;
        }
        if (len == 1) {
            return points.get(0);
        }
        if (len == mp.getNumGeometries()) {
            return mp;
        }
        return GF.createMultiPoint(points.toArray(new Point[len]));
    }

    protected Geometry removeObviouslyDisjointParts(Geometry geom) {
        return new DisjointGeometryPartRemover(tileEnvelope).transform(geom);
    }

}
