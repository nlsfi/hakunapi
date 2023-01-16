package fi.nls.hakunapi.mvt.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.impl.PackedCoordinateSequence;

public class CohenSutherland {

    private static final int LEFT   = 1;
    private static final int RIGHT  = 2;
    private static final int BOTTOM = 4;
    private static final int TOP    = 8;

    protected final double xmin;
    protected final double ymin;
    protected final double xmax;
    protected final double ymax;

    public CohenSutherland(Envelope bbox) {
        this.xmin = bbox.getMinX();
        this.ymin = bbox.getMinY();
        this.xmax = bbox.getMaxX();
        this.ymax = bbox.getMaxY();
    }

    public List<LineString> clipLine(LineString ls, GeometryFactory gf) {
        CoordinateSequence cs = ls.getCoordinateSequence();
        int len = cs.size();

        double[] xy = new double[16];
        int size = 0;

        List<LineString> lines = new ArrayList<>();

        double ax;
        double ay;
        double bx;
        double by;
        double lastx = Double.NaN;
        double lasty = Double.NaN;

        ax = cs.getOrdinate(0, 0);
        ay = cs.getOrdinate(0, 1);
        int codeA = outCode(ax, ay);
        
        Coordinate out = new Coordinate();

        for (int i = 1; i < len; i++) {
            ax = cs.getOrdinate(i - 1, 0);
            ay = cs.getOrdinate(i - 1, 1);
            bx = cs.getOrdinate(i, 0);
            by = cs.getOrdinate(i, 1);
            int codeB = outCode(bx, by);
            int lastCode = codeB;

            while (true) {
                if ((codeA | codeB) == 0) {
                    if (lastx != ax || lasty != ay) {
                        if (size == xy.length) {
                            xy = Arrays.copyOf(xy, xy.length * 2);
                        }
                        xy[size++] = ax;
                        xy[size++] = ay;
                        lastx = ax;
                        lasty = ay;
                    }

                    if (codeB != lastCode) {
                        if (lastx != bx || lasty != by) {
                            if (size == xy.length) {
                                xy = Arrays.copyOf(xy, xy.length * 2);
                            }
                            xy[size++] = bx;
                            xy[size++] = by;
                            lastx = bx;
                            lasty = by;
                        }
                        if (i < len - 1) {
                            CoordinateSequence csq = new PackedCoordinateSequence.Double(Arrays.copyOf(xy, size), 2, 0);
                            if (csq.size() >= 2) {
                                lines.add(gf.createLineString(csq));
                            }
                            size = 0;
                            if (xy.length >= 512) {
                                xy = new double[16];
                            }
                        }
                    } else if (i == len - 1) {
                        if (lastx != bx || lasty != by) {
                            if (size == xy.length) {
                                xy = Arrays.copyOf(xy, xy.length * 2);
                            }
                            xy[size++] = bx;
                            xy[size++] = by;
                        }
                    }
                    break;
                } else if ((codeA & codeB) != 0) {
                    break;
                } else if (codeA != 0) {
                    intersection(ax, ay, bx, by, codeA, out);
                    ax = out.x;
                    ay = out.y;
                    codeA = outCode(ax, ay);
                } else {
                    intersection(ax, ay, bx, by, codeB, out);
                    bx = out.x;
                    by = out.y;
                    codeB = outCode(bx, by);
                }
            }

            codeA = lastCode;
        }

        if (size > 0) {
            CoordinateSequence csq = new PackedCoordinateSequence.Double(Arrays.copyOf(xy, size), 2, 0);
            if (csq.size() >= 2) {
                lines.add(gf.createLineString(csq));
            }
        }

        return lines;
    }


    protected int outCode(double x, double y) {
        int code = 0;
        if (x < xmin) {
            code |= LEFT;
        } else if (x > xmax) {
            code |= RIGHT;
        }
        if (y < ymin) {
            code |= BOTTOM;
        } else if (y > ymax) {
            code |= TOP;
        }
        return code;
    }
    
    private void intersection(double ax, double ay, double bx, double by, int code, Coordinate out) {
        double x;
        double y;
        if ((code & TOP) != 0) {
            y = ymax;
            x = ax + (bx - ax) * (y - ay) / (by - ay);
        } else if ((code & BOTTOM) != 0) {
            y = ymin;
            x = ax + (bx - ax) * (y - ay) / (by - ay);
        } else if ((code & RIGHT) != 0) {
            x = xmax;
            y = ay + (by - ay) * (x - ax) / (bx - ax);
        } else if ((code & LEFT) != 0) {
            x = xmin;
            y = ay + (by - ay) * (x - ax) / (bx - ax);
        } else {
            return;
        }
        out.x = x;
        out.y = y;
    }


}
