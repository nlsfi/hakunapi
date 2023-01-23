package fi.nls.hakunapi.mvt;

import java.util.Arrays;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.GeometryTransformer;

public class DisjointGeometryPartRemover extends GeometryTransformer {

    private final Envelope envelope;

    public DisjointGeometryPartRemover(Envelope envelope) {
        this.envelope = envelope;
    }

    @Override
    protected Geometry transformLineString(LineString geom, Geometry parent) {
        if (geom.getEnvelopeInternal().intersects(envelope)) {
            return geom;
        }
        return null;
    }

    @Override
    protected Geometry transformPolygon(Polygon geom, Geometry parent) {
        Polygon polygon = (Polygon) geom;
        LinearRing exterior = (LinearRing) polygon.getExteriorRing();
        if (!exterior.getEnvelopeInternal().intersects(envelope)) {
            return null;
        }
        int numInteriorRing = polygon.getNumInteriorRing();
        if (numInteriorRing == 0) {
            return geom;
        }
        LinearRing[] interiorRings = new LinearRing[numInteriorRing];
        int n = 0;
        for (int i = 0; i < numInteriorRing; i++) {
            LinearRing interiorRing = (LinearRing) polygon.getInteriorRingN(i);
            if (interiorRing.getEnvelopeInternal().intersects(envelope)) {
                interiorRings[n++] = interiorRing;
            }
        }
        if (n == 0) {
            return factory.createPolygon(exterior);
        }
        if (n == numInteriorRing) {
            return geom;
        }
        return factory.createPolygon(exterior, Arrays.copyOf(interiorRings, n));
    }

}
