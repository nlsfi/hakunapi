package fi.nls.hakunapi.core.geom;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

public class Bbox {

    private Bbox() {}

    public static Envelope parse(String bbox) throws IllegalArgumentException {
        if (bbox == null || bbox.length() == 0) {
            return null;
        }
        String[] split = bbox.split(",");
        if (split.length != 4) {
            throw new IllegalArgumentException("Invalid number of elements in bbox!");
        }
        try {
            double x1 = Double.parseDouble(split[0]);
            double y1 = Double.parseDouble(split[1]);
            double x2 = Double.parseDouble(split[2]);
            double y2 = Double.parseDouble(split[3]);
            return new Envelope(x1, x2, y1, y2);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid bbox!");
        }
    }

    public static Geometry toGeometry(Envelope env) {
        return toGeometry(env, HakunaGeometryFactory.GF);
    }

    public static Geometry toGeometry(Envelope env, GeometryFactory gf) {
        if (gf == null) {
            gf = HakunaGeometryFactory.GF;
        }
        Coordinate[] shell = {
                new Coordinate(env.getMinX(), env.getMinY()),
                new Coordinate(env.getMaxX(), env.getMinY()),
                new Coordinate(env.getMaxX(), env.getMaxY()),
                new Coordinate(env.getMinX(), env.getMaxY()),
                new Coordinate(env.getMinX(), env.getMinY())
        };
        return gf.createPolygon(shell);
    }

}
