package fi.nls.hakunapi.core.geom;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

public class Bbox {

    private Bbox() {}

    public static Coordinate[] parseCoordinates(String bbox) throws IllegalArgumentException {
        if (bbox == null || bbox.length() == 0) {
            return null;
        }

        String[] split = bbox.split(",");
        if (split.length != 4 && split.length != 6) {
            throw new IllegalArgumentException("Invalid number of elements in bbox!");
        }

        double[] numbers = new double[split.length];
        try {
            for (int i = 0; i < numbers.length; i++) {
                numbers[i] = Double.parseDouble(split[i]);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid bbox!");
        }

        if (numbers.length == 4) {
            return new Coordinate[] {
                new Coordinate(numbers[0], numbers[1]),
                new Coordinate(numbers[2], numbers[3])
            };
        } else {
            return new Coordinate[] {
                new Coordinate(numbers[0], numbers[1], numbers[2]),
                new Coordinate(numbers[3], numbers[4], numbers[5])
            };
        }
    }

    /**
     * @deprecated Envelope does not handle antimeridian-spanning bboxes correctly.
     * Use parseCoordinates() instead.
     */
    @Deprecated
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

    /**
     * @deprecated Envelope does not handle antimeridian-spanning bboxes correctly.
     * Use parseCoordinates() instead.
     */
    @Deprecated
    public static Geometry toGeometry(Envelope env) {
        return toGeometry(env, HakunaGeometryFactory.GF);
    }

    /**
     * @deprecated Envelope does not handle antimeridian-spanning bboxes correctly.
     * Use parseCoordinates() instead.
     */
    @Deprecated
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

    /**
     * Creates a bbox geometry. When x1 > x2, creates a polygon with "inverted" x coordinates
     * which can be detected and split later based on SRID wrap bounds.
     *
     * @param x1 first x coordinate (minX for normal, west edge for wrap-x crossing)
     * @param y1 first y coordinate (minY)
     * @param x2 second x coordinate (maxX for normal, east edge for wrap-x crossing)
     * @param y2 second y coordinate (maxY)
     * @param gf GeometryFactory to use (null for default)
     * @return Polygon geometry representing the bbox (may need wrap-x splitting later)
     */
    public static Geometry createBboxGeometry(double x1, double y1, double x2, double y2,
                                               GeometryFactory gf) {
        if (gf == null) {
            gf = HakunaGeometryFactory.GF;
        }
        // Create polygon with original coordinates - don't normalize x1/x2
        // This preserves the "inverted" x order for antimeridian detection later
        Coordinate[] shell = {
                new Coordinate(x1, y1),
                new Coordinate(x2, y1),
                new Coordinate(x2, y2),
                new Coordinate(x1, y2),
                new Coordinate(x1, y1)
        };
        return gf.createPolygon(shell);
    }

    /**
     * Checks if a geometry is a bbox-like rectangle that crosses the wrap-x boundary.
     * Detects this by finding exactly two unique x values and checking if the "left" one
     * (appearing first in the coordinate sequence) is greater than the "right" one.
     *
     * @param geom the geometry to check
     * @return true if the geometry appears to be an antimeridian-crossing bbox
     */
    public static boolean isWrapXCrossingBbox(Geometry geom) {
        if (!(geom instanceof Polygon)) {
            return false;
        }
        Coordinate[] coords = geom.getCoordinates();
        if (coords.length != 5) {
            return false; // Not a simple rectangle
        }

        // Find the two unique x values
        double firstX = coords[0].x;
        double secondX = Double.NaN;
        for (int i = 1; i < coords.length - 1; i++) {
            if (coords[i].x != firstX) {
                secondX = coords[i].x;
                break;
            }
        }

        if (Double.isNaN(secondX)) {
            return false; // Degenerate case
        }

        // If firstX > secondX, the bbox crosses the wrap boundary
        return firstX > secondX;
    }

    /**
     * Splits a wrap-x crossing bbox into a MultiPolygon.
     * Should only be called after isWrapXCrossingBbox returns true.
     *
     * @param geom the bbox polygon to split
     * @param wrapXMin minimum x boundary (e.g., -180)
     * @param wrapXMax maximum x boundary (e.g., 180)
     * @return MultiPolygon with western and eastern parts
     */
    public static Geometry splitWrapXBbox(Geometry geom, double wrapXMin, double wrapXMax) {
        Coordinate[] coords = geom.getCoordinates();

        // Find the two unique x values and two unique y values
        double x1 = coords[0].x;
        double x2 = Double.NaN;
        double y1 = coords[0].y;
        double y2 = Double.NaN;

        for (int i = 1; i < coords.length - 1; i++) {
            if (Double.isNaN(x2) && coords[i].x != x1) {
                x2 = coords[i].x;
            }
            if (Double.isNaN(y2) && coords[i].y != y1) {
                y2 = coords[i].y;
            }
        }

        // Ensure y1 < y2
        if (y1 > y2) {
            double tmp = y1;
            y1 = y2;
            y2 = tmp;
        }

        GeometryFactory gf = geom.getFactory();
        Geometry result = createWrapXMultiPolygon(x1, y1, x2, y2, wrapXMin, wrapXMax, gf);
        result.setSRID(geom.getSRID());
        return result;
    }

    private static Geometry createWrapXMultiPolygon(
        double x1, double y1, double x2, double y2,
        double wrapXMin, double wrapXMax,
        GeometryFactory gf) {
        // Western box: from x1 to wrap boundary maximum
        Coordinate[] westernShell = {
                new Coordinate(x1, y1),
                new Coordinate(wrapXMax, y1),
                new Coordinate(wrapXMax, y2),
                new Coordinate(x1, y2),
                new Coordinate(x1, y1)
        };
        Polygon westernBox = gf.createPolygon(westernShell);

        // Eastern box: from wrap boundary minimum to x2
        Coordinate[] easternShell = {
                new Coordinate(wrapXMin, y1),
                new Coordinate(x2, y1),
                new Coordinate(x2, y2),
                new Coordinate(wrapXMin, y2),
                new Coordinate(wrapXMin, y1)
        };
        Polygon easternBox = gf.createPolygon(easternShell);

        return gf.createMultiPolygon(new Polygon[]{westernBox, easternBox});
    }


    /**
     * If the MultiPolygon has exactly 2 touching polygons, merge them into one.
     * This can happen after reprojection when the storage CRS doesn't have a wrap boundary
     * (e.g., reprojecting from CRS84 to a local CRS like EPSG:3067).
     *
     * @param mp the MultiPolygon to potentially merge
     * @return merged Polygon if the two parts touch, otherwise the original MultiPolygon
     */
    public static Geometry tryMergeMultiPolygon(MultiPolygon mp) {
        if (mp.getNumGeometries() == 2) {
            Polygon p1 = (Polygon) mp.getGeometryN(0);
            Polygon p2 = (Polygon) mp.getGeometryN(1);
            if (p1.touches(p2)) {
                Geometry merged = p1.union(p2);
                merged.setSRID(mp.getSRID());
                return merged;
            }
        }
        return mp;
    }

}
