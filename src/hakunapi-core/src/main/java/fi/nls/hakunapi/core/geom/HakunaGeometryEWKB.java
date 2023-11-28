package fi.nls.hakunapi.core.geom;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBConstants;
import org.locationtech.jts.io.WKBReader;

import fi.nls.hakunapi.core.GeometryWriter;

public class HakunaGeometryEWKB implements HakunaGeometry {

    public static final int EWKB_HAS_Z    = 0x80000000;
    public static final int EWKB_HAS_M    = 0x40000000;
    public static final int EWKB_HAS_SRID = 0x20000000;

    public static final int OGC_HAS_Z = 1000;
    public static final int OGC_HAS_M = 2000;

    public static final byte BIG_ENDIAN = 0;
    public static final byte LITTLE_ENDIAN = 1;

    public final ByteBuffer bb;
    public final int dataStart;
    public final int type;
    public final int dimension;
    public final boolean hasZ;
    public final boolean hasM;
    public final boolean hasSrid;
    public final int srid;

    public HakunaGeometryEWKB(byte[] b) {
        this(ByteBuffer.wrap(b));
    }

    public HakunaGeometryEWKB(ByteBuffer bb) {
        this.bb = bb;
        if (bb.get() != 0) {
            bb.order(ByteOrder.LITTLE_ENDIAN);
        }
        int extType = bb.getInt();
        hasZ = hasZ(extType);
        hasM = hasM(extType);
        dimension = 2 + (hasZ ? 1 : 0) + (hasM ? 1 : 0);
        hasSrid = (extType & EWKB_HAS_SRID) != 0;
        srid = hasSrid ? bb.getInt() : 0;
        type = extType & 0x7;
        dataStart = bb.position();
    }

    @Override
    public byte[] toEWKB() {
        return bb.array();
    }

    @Override
    public void write(GeometryWriter writer) throws Exception {
        bb.position(dataStart);
        final int n;
        switch (type) {
        case 1:
            writer.init(HakunaGeometryType.POINT, srid, dimension);
            writePoint(writer);
            writer.end();
            return;
        case 2:
            writer.init(HakunaGeometryType.LINESTRING, srid, dimension);
            writeRing(writer);
            writer.end();
            return;
        case 3:
            writer.init(HakunaGeometryType.POLYGON, srid, dimension);
            writeRings(writer);
            writer.end();
            return;
        case 4:
            writer.init(HakunaGeometryType.MULTIPOINT, srid, dimension);
            writer.startRing();
            n = bb.getInt();
            for (int i = 0; i < n; i++) {
                if (bb.get() != 0) {
                    bb.order(ByteOrder.LITTLE_ENDIAN);
                }
                bb.getInt();
                writePoint(writer);
            }
            writer.endRing();
            writer.end();
            return;
        case 5:
            writer.init(HakunaGeometryType.MULTILINESTRING, srid, dimension);
            writer.startRing();
            n = bb.getInt();
            for (int i = 0; i < n; i++) {
                if (bb.get() != 0) {
                    bb.order(ByteOrder.LITTLE_ENDIAN);
                }
                bb.getInt();
                writeRing(writer);
            }
            writer.endRing();
            writer.end();
            return;
        case 6:
            writer.init(HakunaGeometryType.MULTIPOLYGON, srid, dimension);
            writer.startRing();
            n = bb.getInt();
            for (int i = 0; i < n; i++) {
                if (bb.get() != 0) {
                    bb.order(ByteOrder.LITTLE_ENDIAN);
                }
                bb.getInt();
                writeRings(writer);
            }
            writer.endRing();
            writer.end();
            return;
        default:
            // TODO handle GeometryCollection
            throw new IllegalArgumentException();
        }
    }

    public void writePoint(GeometryWriter writer) throws Exception {
        if (dimension == 2) {
            writer.writeCoordinate(bb.getDouble(), bb.getDouble());
        } else {
            writer.writeCoordinate(bb.getDouble(), bb.getDouble(), bb.getDouble());
        }
    }

    public void writeRing(GeometryWriter writer) throws Exception {
        writer.startRing();
        final int n = bb.getInt();
        for (int i = 0; i < n; i++) {
            if (dimension == 2) {
                writer.writeCoordinate(bb.getDouble(), bb.getDouble());
            } 
            if (dimension == 3) {
                writer.writeCoordinate(bb.getDouble(), bb.getDouble(), bb.getDouble());
            }
            if (dimension == 4) {
                writer.writeCoordinate(bb.getDouble(), bb.getDouble(), bb.getDouble(), bb.getDouble());
            }
        }
        writer.endRing();
    }

    public void writeRings(GeometryWriter writer) throws Exception {
        writer.startRing();
        final int n = bb.getInt();
        for (int i = 0; i < n; i++) {
            writeRing(writer);
        }
        writer.endRing();
    }

    @Override
    public Geometry toJTSGeometry() {
        try {
            return new WKBReader().read(bb.array());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Envelope boundedBy() {
        Envelope e = new Envelope();
        int n;
        switch (type) {
        case 1:
            expandToIncludeCurrentPoint(e);
            break;
        case 2:
            expandToIncludeCurrentRing(e);
            break;
        case 3:
            expandToIncludeCurrentPolygon(e);
            break;
        case 4:
            n = bb.getInt();
            for (int i = 0; i < n; i++) {
                if (bb.get() != 0) {
                    bb.order(ByteOrder.LITTLE_ENDIAN);
                }
                bb.getInt(); // Skip type
                expandToIncludeCurrentPoint(e);
            }
            break;
        case 5:
            n = bb.getInt();
            for (int i = 0; i < n; i++) {
                if (bb.get() != 0) {
                    bb.order(ByteOrder.LITTLE_ENDIAN);
                }
                bb.getInt(); // Skip type
                expandToIncludeCurrentRing(e);
            }
            break;
        case 6:
            n = bb.getInt();
            for (int i = 0; i < n; i++) {
                if (bb.get() != 0) {
                    bb.order(ByteOrder.LITTLE_ENDIAN);
                }
                bb.getInt(); // Skip type
                expandToIncludeCurrentPolygon(e);
            }
            break;
        default:
            // TODO handle GeometryCollection
            throw new IllegalArgumentException();
        }

        return e;
    }

    private void expandToIncludeCurrentPoint(Envelope e) {
        int extraDimensionSkipPerPoint = (dimension - 2) * 8;
        e.expandToInclude(bb.getDouble(), bb.getDouble());
        bb.position(bb.position() + extraDimensionSkipPerPoint);
    }

    private void expandToIncludeCurrentRing(Envelope e) {
        int extraDimensionSkipPerPoint = (dimension - 2) * 8;
        int numPoints = bb.getInt();
        for (int i = 0; i < numPoints; i++) {
            e.expandToInclude(bb.getDouble(), bb.getDouble());
            bb.position(bb.position() + extraDimensionSkipPerPoint);
        }
    }

    private void expandToIncludeCurrentPolygon(Envelope e) {
        int numRings = bb.getInt();

        // Only expand by exterior ring
        expandToIncludeCurrentRing(e);
        // Ignore interior rings
        for (int i = 1; i < numRings; i++) {
            skipCurrentRing();
        }
    }

    private void skipCurrentRing() {
        int numPoints = bb.getInt();
        int skipPerPoint = dimension * 8;
        int skip = skipPerPoint * numPoints;
        bb.position(bb.position() + skip);
    }

    @Override
    public int getSrid() {
        return srid;
    }

    public static int getType(byte[] wkb) {
        ByteBuffer bb = ByteBuffer.wrap(wkb, 1, 4);
        if (wkb[0] != 0) {
            bb.order(ByteOrder.LITTLE_ENDIAN);
        }
        return bb.getInt(0);
    }

    public static int getBasicType(int type) {
        return type & 0x7;
    }

    public static boolean hasZ(int type) {
        return 0 != (type & EWKB_HAS_Z);
    }

    public static boolean hasM(int type) {
        return 0 != (type & EWKB_HAS_M);
    }

    @Override
    public int getWKBType() {
        return type;
    }

    @Override
    public int getDimension() {
        return 2 + (hasZ ? 1 : 0) + (hasM ? 1 : 0);
    }

    @Override
    public int getWKBLength() {
        return bb.array().length - (hasSrid ? 4 : 0);
    }

    @Override
    public void toWKB(byte[] wkb, int off) {
        byte[] ewkb = bb.array();
        ByteOrder bo = ewkb[0] == 0 ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;

        if (dimension == 2) {
            if (!hasSrid) {
                System.arraycopy(ewkb, 0, wkb, off, ewkb.length);
                return;
            }
            wkb[off] = ewkb[0];
            putInt(wkb, off + 1, bo, type);
            System.arraycopy(ewkb, 9, wkb, off + 5, ewkb.length - 9);
            return;
        }

        wkb[off] = ewkb[0];
        putInt(wkb, off + 1, bo, type + (hasZ ? OGC_HAS_Z : 0) + (hasM ? OGC_HAS_M : 0));
        System.arraycopy(ewkb, dataStart, wkb, off + 5, ewkb.length - dataStart);

        int pos;
        int bytesPerPoint = dimension * Double.BYTES;
        int childType;
        int numPoints, numRings, numGeoms;

        // This is just wrong if there are srids?
        switch (type) {
        case 1:
        case 2:
        case 3:
            break;
        case 4:
            childType = WKBConstants.wkbPoint + (hasZ ? OGC_HAS_Z : 0) + (hasM ? OGC_HAS_M : 0);
            numGeoms = getInt(wkb, off + 5, bo);
            pos = off + 9;
            for (int i = 0; i < numGeoms; i++) {
                bo = wkb[pos++] == 0 ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
                putInt(wkb, pos, bo, childType);
                pos += 4;
                pos += bytesPerPoint; // Skip type, xyzm
            }
            break;
        case 5:
            childType = WKBConstants.wkbLineString + (hasZ ? OGC_HAS_Z : 0) + (hasM ? OGC_HAS_M : 0);
            numGeoms = getInt(wkb, off + 5, bo);
            pos = off + 9;
            for (int i = 0; i < numGeoms; i++) {
                bo = wkb[pos++] == 0 ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
                putInt(wkb, pos, bo, childType);
                pos += 4;
                numPoints = bb.getInt(pos);
                pos += 4 + numPoints * bytesPerPoint;
            }
            break;
        case 6:
            childType = WKBConstants.wkbPolygon + (hasZ ? OGC_HAS_Z : 0) + (hasM ? OGC_HAS_M : 0);
            numGeoms = getInt(wkb, off + 5, bo);
            pos = off + 9;
            for (int i = 0; i < numGeoms; i++) {
                bo = wkb[pos++] == 0 ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
                putInt(wkb, pos, bo, childType);
                pos += 4;
                numRings = getInt(wkb, pos, bo);
                pos += 4;
                for (int j = 0; j < numRings; j++) {
                    numPoints = getInt(wkb, pos, bo);
                    pos += numPoints * bytesPerPoint;
                }
            }
            break;
        default:
            // TODO handle others
            throw new IllegalArgumentException();
        }
    }

    private static final void putInt(final byte[] buf, final int off, final ByteOrder bo, final int v) {
        ByteBuffer.wrap(buf, off, 4).order(bo).putInt(v);
    }

    public static final int getInt(final byte[] b, final int off, final ByteOrder bo) {
        return bo == ByteOrder.BIG_ENDIAN ? readIntBE(b, off) : readIntLE(b, off);
    }

    private static final int readIntBE(final byte[] b, int off) {
        return  ((b[off++] & 0xff) << 24) |
                ((b[off++] & 0xff) << 16) |
                ((b[off++] & 0xff) <<  8) |
                ((b[off++] & 0xff) <<  0);
    }

    private static final int readIntLE(final byte[] b, int off) {
        return  ((b[off++] & 0xff) <<  0) |
                ((b[off++] & 0xff) <<  8) |
                ((b[off++] & 0xff) << 16) |
                ((b[off++] & 0xff) << 24);
    }

}
