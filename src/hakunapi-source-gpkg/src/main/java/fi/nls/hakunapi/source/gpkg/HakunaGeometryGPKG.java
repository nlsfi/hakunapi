package fi.nls.hakunapi.source.gpkg;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBReader;

import fi.nls.hakunapi.core.GeometryWriter;
import fi.nls.hakunapi.core.geom.HakunaGeometry;
import fi.nls.hakunapi.core.geom.HakunaGeometryJTS;
import fi.nls.hakunapi.core.geom.HakunaGeometryType;
import fi.nls.hakunapi.gpkg.GPKGGeometry;

public class HakunaGeometryGPKG implements HakunaGeometry {

    public final ByteBuffer bb;
    public final int wkbOffset;
    public final int dataStart;
    public final int type;
    public final int dimension;
    public final int srid;

    public HakunaGeometryGPKG(byte[] b) {
        this(ByteBuffer.wrap(b));
    }

    public HakunaGeometryGPKG(ByteBuffer bb) {
        this.bb = bb;
        byte magic1 = bb.get();
        byte magic2 = bb.get();
        byte version = bb.get();
        byte flags = bb.get();

        bb.order(GPKGGeometry.getByteOrder(flags));
        srid = bb.getInt();

        int envelopeIndicator = GPKGGeometry.getEnvelopeIndicatorCode(flags);
        // TODO: Read and cache envelope for boundedBy()
        wkbOffset = GPKGGeometry.getWKBOffset(envelopeIndicator);
        bb.position(wkbOffset);

        bb.order(bb.get() == 0 ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
        int typeInt = bb.getInt();
        int thousands = (typeInt & 0xFFFF) / 1000;
        boolean hasZ = thousands == 1 || thousands == 3;
        boolean hasM = thousands == 2 || thousands == 3;
        dimension = 2 + (hasZ ? 1 : 0) + (hasM ? 1 : 0);
        type = typeInt & 0x7;
        dataStart = bb.position();
    }

    @Override
    public byte[] toEWKB() {
        // TODO: Optimize me
        return new HakunaGeometryJTS(toJTSGeometry()).toEWKB();
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
            } else if (dimension == 3) {
                writer.writeCoordinate(bb.getDouble(), bb.getDouble(), bb.getDouble());
            } else if (dimension == 4) {
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
            byte[] blob = bb.array();
            byte[] wkb = Arrays.copyOfRange(blob, wkbOffset, blob.length);
            return new WKBReader().read(wkb);
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

    @Override
    public int getWKBType() {
        return type;
    }

    @Override
    public int getDimension() {
        return dimension;
    }

    @Override
    public int getWKBLength() {
        return bb.array().length - wkbOffset;
    }

    @Override
    public void toWKB(byte[] wkb, int off) {
        System.arraycopy(bb.array(), dataStart, wkb, off, bb.array().length - dataStart);
    }

}
