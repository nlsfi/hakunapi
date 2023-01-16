package fi.nls.hakunapi.mvt.algorithm;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;

import fi.nls.hakunapi.mvt.CoordinateSequenceBuilder2D;
import fi.nls.hakunapi.mvt.Sink;

public class SutherlandHodgman extends CohenSutherland {

    private final CoordinateSequenceBuilder2D csb;
    private final ClippingPlane pipeline;

    public SutherlandHodgman(Envelope bbox) {
        super(bbox);
        this.csb = new CoordinateSequenceBuilder2D();
        ClippingPlane bot   = new ClippingPlane(3, ymin, csb);
        ClippingPlane top   = new ClippingPlane(2, ymax, bot);
        ClippingPlane right = new ClippingPlane(1, xmax, top);
        ClippingPlane left  = new ClippingPlane(0, xmin, right);
        this.pipeline = left;
    }

    public LinearRing clipPolygon(LinearRing ring, GeometryFactory gf) {
        CoordinateSequence cs = ring.getCoordinateSequence();
        int len = cs.size();

        csb.clear();

        boolean reject;
        boolean edge = false;
        boolean mode = false;
        int subspace;
        int region = 0;

        double Sx = cs.getOrdinate(0, 0);
        double Sy = cs.getOrdinate(0, 1);
        subspace = outCode(Sx, Sy);
        if (subspace == 0) {
            reject = false;
        } else  {
            reject = true;
            region = subspace;
        }
        pipeline.add(Sx, Sy);

        for (int i = 1; i < len - 1; i++) {
            double x = cs.getOrdinate(i, 0);
            double y = cs.getOrdinate(i, 1);
            if (mode) {
                pipeline.add(x, y);
            } else {
                subspace = outCode(x, y);
                if (reject) {
                    if ((subspace & region) != 0) {
                        Sx = x;
                        Sy = y;
                        region &= subspace;
                        edge = true;
                    } else {
                        mode = true;
                        if (edge) {
                            pipeline.add(Sx, Sy);
                        }
                        pipeline.add(x, y);
                    }
                } else {
                    if (subspace == 0) {
                        csb.add(x, y);
                        Sx = x;
                        Sy = y;
                        edge = true;
                    } else {
                        mode = true;
                        if (edge) {
                            pipeline.add(Sx, Sy);
                        }
                        pipeline.add(x, y);
                    }
                }
            }
        }

        pipeline.closePolygon();

        CoordinateSequence clipped = csb.build(gf, true, 4);
        if (clipped == null) {
            return null;
        }
        return gf.createLinearRing(clipped);
    }

    private static class ClippingPlane implements Sink {

        private final int edge; 
        private final double limit;
        private final Sink sink;
        private double x0, y0;
        private double x1, y1;
        private boolean first, prevVisible, wasOutput;

        public ClippingPlane(int edge, double limit, Sink sink) {
            this.edge = edge;
            this.limit = limit;
            this.sink = sink;
            this.first = true;
            this.wasOutput = false;
        }

        public void add(double x2, double y2) {
            if (first) {
                first = false;
                x0 = x1 = x2;
                y0 = y1 = y2;
                prevVisible = isVisible(x2, y2);
                if (prevVisible) {
                    sink.add(x2, y2);
                    wasOutput = true;
                }
            } else {
                boolean inside = isVisible(x2, y2);
                if (prevVisible != inside) {
                    double ix, iy;
                    if (edge < 2) {
                        ix = limit;
                        iy = y1 + (y2 - y1) * (ix - x1) / (x2 - x1);
                    } else {
                        iy = limit;
                        ix = x1 + (x2 - x1) * (iy - y1) / (y2 - y1);
                    }
                    sink.add(ix, iy);
                    wasOutput = true;
                }
                if (inside) {
                    sink.add(x2, y2);
                    wasOutput = true;
                }
                x1 = x2;
                y1 = y2;
                prevVisible = inside;
            }
        }

        private boolean isVisible(double x, double y) {
            switch (edge) {
            case 0: // LEFT
            return x >= limit;
            case 1: // RIGHT
                return x <= limit;
            case 2: // TOP
                return y <= limit;
                // case 3: // BOTTOM
            default:
                return y >= limit;
            }
        }

        private void outputIntersection(double x2, double y2) {
            double ix, iy;
            if (edge < 2) {
                ix = limit;
                iy = y1 + (y2 - y1) * (ix - x1) / (x2 - x1);
            } else {
                iy = limit;
                ix = x1 + (x2 - x1) * (iy - y1) / (y2 - y1);
            }
            add(ix, iy);
        }

        public void closePolygon() {
            if (wasOutput) {
                boolean firstVisible = isVisible(x0, y0);
                if (prevVisible != firstVisible) {
                    outputIntersection(x0, y0);
                }
            }
            first = true;
            wasOutput = false;
            sink.closePolygon();
        }

    }

}
