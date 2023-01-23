package fi.nls.hakunapi.mvt;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.GeometryTransformer;

import fi.nls.hakunapi.mvt.algorithm.CohenSutherland;
import fi.nls.hakunapi.mvt.algorithm.SutherlandHodgman;

public class GeometryRectClipper extends GeometryTransformer {

    private final CohenSutherland lineClipper;
    private final SutherlandHodgman polygonClipper;

    public GeometryRectClipper(CohenSutherland clipper, SutherlandHodgman polygonClipper) {
        this.lineClipper = clipper;
        this.polygonClipper = polygonClipper;
    }

    @Override
    protected Geometry transformLineString(LineString geom, Geometry parent) {
        List<LineString> lines = lineClipper.clipLine(geom, geom.getFactory());
        int n = lines.size();
        if (n == 0) {
            return null;
        }
        if (n == 1) {
            return lines.get(0);
        }
        return geom.getFactory().createMultiLineString(lines.toArray(new LineString[n]));
    }

    @Override
    protected Geometry transformPolygon(Polygon geom, Geometry parent) {
        LinearRing exterior = (LinearRing) geom.getExteriorRing();
        LinearRing shell = polygonClipper.clipPolygon(exterior, geom.getFactory());
        if (shell == null) {
            return null;
        }
        int n = geom.getNumInteriorRing();
        if (n == 0) {
            return geom.getFactory().createPolygon(shell);
        }
        List<LinearRing> holes = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            LinearRing interior = (LinearRing) geom.getInteriorRingN(i);
            LinearRing hole = polygonClipper.clipPolygon(interior, geom.getFactory());
            if (hole != null) {
                holes.add(hole);
            }
        }
        return geom.getFactory().createPolygon(shell, holes.toArray(new LinearRing[holes.size()]));
    }

}
