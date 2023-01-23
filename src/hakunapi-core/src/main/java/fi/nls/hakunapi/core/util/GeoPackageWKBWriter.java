package fi.nls.hakunapi.core.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.WKBConstants;

public class GeoPackageWKBWriter {

    public static final byte WKB_LITTLE_ENDIAN = WKBConstants.wkbNDR;
    public static final byte WKB_BIG_ENDIAN = WKBConstants.wkbXDR;

    public static final int FLAG_Z = 1000;
    public static final int FLAG_M = 2000;

    private GeoPackageWKBWriter() {}

    public static int length(Geometry geom, boolean z, boolean m) {
        final int lenPerCoordinate = 16 + (z ? 8 : 0) + (m ? 8 : 0);

        if (geom instanceof Point) {
            return 1 + 4 + lenPerCoordinate;
        } else if (geom instanceof LineString) {
            return 1 + 4 + 4 + lenPerCoordinate * ((LineString) geom).getCoordinateSequence().size();
        } else if (geom instanceof Polygon) {
            return lengthPolygon((Polygon) geom, lenPerCoordinate);
        } else if (geom instanceof GeometryCollection) {
            int len = 1 + 4 + 4;
            GeometryCollection c = (GeometryCollection) geom;
            int numGeometries = c.getNumGeometries();
            for (int i = 0; i < numGeometries; i++) {
                len += length(c.getGeometryN(0), z, m);
            }
            return len;
        } else {
            throw new IllegalArgumentException("Geometry not Point, LineString, Polygon or GeometryCollection");
        }
    }

    private static int lengthPolygon(Polygon poly, int lenPerCoordinate) {
        int len = 1 + 4 + 4 + 4 + lenPerCoordinate * poly.getExteriorRing().getCoordinateSequence().size();
        for (int i = 0; i < poly.getNumInteriorRing(); i++) {
            len += 4 + lenPerCoordinate * poly.getInteriorRingN(i).getCoordinateSequence().size();
        }
        return len;
    }

    public static void write(Geometry geom, boolean z, boolean m, byte[] buf, int offset, ByteOrder bo) {
        write(geom, z, m, ByteBuffer.wrap(buf, offset, buf.length - offset).order(bo));
    }

    public static void write(Geometry geom, boolean z, boolean m, ByteBuffer buf) {
        if (geom instanceof Point) {
            writePoint((Point) geom, z, m, buf);
        } else if (geom instanceof LineString) {
            writeLineString((LineString) geom, z, m, buf);
        } else if (geom instanceof Polygon) {
            writePolygon((Polygon) geom, z, m, buf);
        } else if (geom instanceof MultiPoint) {
            writeGeometryCollection(WKBConstants.wkbMultiPoint, (MultiPoint) geom, z, m, buf);
        } else if (geom instanceof MultiLineString) {
            writeGeometryCollection(WKBConstants.wkbMultiLineString, (MultiLineString) geom, z, m, buf);
        } else if (geom instanceof MultiPolygon) {
            writeGeometryCollection(WKBConstants.wkbMultiPolygon, (MultiPolygon) geom, z, m, buf);
        } else if (geom instanceof GeometryCollection) {
            writeGeometryCollection(WKBConstants.wkbGeometryCollection, (GeometryCollection) geom, z, m, buf);
        } else {
            throw new IllegalArgumentException("Geometry not Point, LineString, Polygon or GeometryCollection");
        }
    }

    private static void writePoint(Point pt, boolean z, boolean m, ByteBuffer buf) {
        buf.put(getByteOrder(buf));
        buf.putInt(getGeometryType(WKBConstants.wkbPoint, z, m));
        if (pt.isEmpty()) {
            writeEmptyPoint(z, m, buf);
        } else {
            writeCoordinateSequence(pt.getCoordinateSequence(), z, m, buf);
        }
    }

    private static void writeEmptyPoint(boolean z, boolean m, ByteBuffer buf) {
        buf.putDouble(Double.NaN);
        buf.putDouble(Double.NaN);
        if (z) {
            buf.putDouble(Double.NaN);
        }
        if (m) {
            buf.putDouble(Double.NaN);
        }
    }

    private static void writeLineString(LineString line, boolean z, boolean m, ByteBuffer buf) {
        CoordinateSequence seq = line.getCoordinateSequence();
        buf.put(getByteOrder(buf));
        buf.putInt(getGeometryType(WKBConstants.wkbLineString, z, m));
        buf.putInt(seq.size());
        writeCoordinateSequence(seq, z, m, buf);
    }

    private static void writePolygon(Polygon poly, boolean z, boolean m, ByteBuffer buf) {
        CoordinateSequence seq;

        buf.put(getByteOrder(buf));
        buf.putInt(getGeometryType(WKBConstants.wkbPolygon, z, m));
        buf.putInt(poly.getNumInteriorRing() + 1);

        seq = poly.getExteriorRing().getCoordinateSequence();
        buf.putInt(seq.size());
        writeCoordinateSequence(seq, z, m, buf);

        for (int i = 0; i < poly.getNumInteriorRing(); i++) {
            seq = poly.getInteriorRingN(i).getCoordinateSequence();
            buf.putInt(seq.size());
            writeCoordinateSequence(seq, z, m, buf);
        }
    }

    private static void writeGeometryCollection(int geometryType, GeometryCollection gc, boolean z, boolean m, ByteBuffer buf) {
        buf.put(getByteOrder(buf));
        buf.putInt(getGeometryType(geometryType, z, m));
        buf.putInt(gc.getNumGeometries());
        for (int i = 0; i < gc.getNumGeometries(); i++) {
            write(gc.getGeometryN(i), z, m, buf);
        }
    }

    private static byte getByteOrder(ByteBuffer buf) {
        return buf.order() == ByteOrder.LITTLE_ENDIAN ? WKB_LITTLE_ENDIAN : WKB_BIG_ENDIAN;
    }

    private static int getGeometryType(int geometryType, boolean z, boolean m) {
        return geometryType + (z ? FLAG_Z : 0) + (m ? FLAG_M : 0);
    }

    private static void writeCoordinateSequence(CoordinateSequence seq, boolean z, boolean m, ByteBuffer buf) {
        if (!z && !m) {
            for (int i = 0; i < seq.size(); i++) {
                buf.putDouble(seq.getX(i));
                buf.putDouble(seq.getY(i));
            }
        } else if (z && m) {
            for (int i = 0; i < seq.size(); i++) {
                buf.putDouble(seq.getX(i));
                buf.putDouble(seq.getY(i));
                buf.putDouble(seq.getOrdinate(i, 2));
                buf.putDouble(seq.getOrdinate(i, 3));
            }
        } else {
            for (int i = 0; i < seq.size(); i++) {
                buf.putDouble(seq.getX(i));
                buf.putDouble(seq.getY(i));
                buf.putDouble(seq.getOrdinate(i, 2));
            }
        }
    }

}
