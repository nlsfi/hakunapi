package fi.nls.hakunapi.core.geom;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import fi.nls.hakunapi.core.GeometryWriter;

public final class NavigableEWKB implements NavigableHakunaGeometry {

    private final WKBBacked geom;
    // Own cursor and byte order: the shared buffer must not see per-part order changes.
    private final ByteBuffer reader;
    private final int dimension;
    private final int strideBytes; // dimension * 8

    private final ByteOrder[] partOrder; // byte order of each part
    private final int[] partRingStart;   // partRingStart[p]..partRingStart[p+1] = part p's rings
    private final int[] ringOffset;       // byte offset of ring's first ordinate
    private final int[] ringCount;        // coordinate count of ring

    private final int partLo;
    private final int partHi;

    public NavigableEWKB(WKBBacked geom) {
        this.geom = geom;
        this.dimension = geom.getDimension();
        this.strideBytes = dimension * Double.BYTES;

        ByteBuffer src = geom.getBuffer();
        ByteBuffer b = src.duplicate();
        b.order(src.order());
        b.position(geom.getDataStart());
        this.reader = b;

        int type = geom.getGeometryType();
        int numParts;
        switch (type) {
        case 1: case 2: case 3:
            numParts = 1;
            break;
        case 4: case 5: case 6:
            numParts = b.getInt();
            break;
        default:
            throw new IllegalArgumentException("unsupported EWKB type " + type);
        }

        this.partOrder = new ByteOrder[numParts];
        this.partRingStart = new int[numParts + 1];

        int[] ro = new int[numParts]; // grows
        int[] rc = new int[numParts];
        int nRings = 0;

        int childType = baseType(type); // ring layout of each part
        for (int p = 0; p < numParts; p++) {
            ByteOrder ord = b.order();
            if (type >= 4) {
                // each member re-states byte order + its own type
                ord = (b.get() != 0) ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
                b.order(ord);
                b.getInt(); // member type, layout known from childType
            }
            partOrder[p] = ord;
            partRingStart[p] = nRings;

            int ringsInPart;
            switch (childType) {
            case 1: ringsInPart = 1; break;          // point: one "ring" of 1 coord
            case 2: ringsInPart = 1; break;          // linestring: one ring
            case 3: ringsInPart = b.getInt(); break;  // polygon: nrings
            default: throw new IllegalArgumentException("unsupported child type " + childType);
            }

            if (nRings + ringsInPart > ro.length) {
                int cap = Math.max(ro.length * 2, nRings + ringsInPart);
                ro = grow(ro, cap);
                rc = grow(rc, cap);
            }

            for (int r = 0; r < ringsInPart; r++) {
                int count = (childType == 1) ? 1 : b.getInt();
                ro[nRings] = b.position();
                rc[nRings] = count;
                nRings++;
                b.position(b.position() + count * strideBytes);
            }
        }
        partRingStart[numParts] = nRings;

        this.ringOffset = (ro.length == nRings) ? ro : grow(ro, nRings);
        this.ringCount = (rc.length == nRings) ? rc : grow(rc, nRings);
        this.partLo = 0;
        this.partHi = numParts;
    }

    private NavigableEWKB(NavigableEWKB src, int part) {
        this.geom = src.geom;
        this.reader = src.reader;
        this.dimension = src.dimension;
        this.strideBytes = src.strideBytes;
        this.partOrder = src.partOrder;
        this.partRingStart = src.partRingStart;
        this.ringOffset = src.ringOffset;
        this.ringCount = src.ringCount;
        this.partLo = part;
        this.partHi = part + 1;
    }

    private static int baseType(int type) {
        switch (type) {
        case 4: return 1; // multipoint -> point parts
        case 5: return 2; // multilinestring -> linestring parts
        case 6: return 3; // multipolygon -> polygon parts
        default: return type; // simple: own layout
        }
    }

    private static int[] grow(int[] a, int n) {
        int[] b = new int[n];
        System.arraycopy(a, 0, b, 0, Math.min(a.length, n));
        return b;
    }

    @Override
    public int getNumGeometries() {
        return partHi - partLo;
    }

    @Override
    public NavigableHakunaGeometry getGeometryN(int n) {
        return new NavigableEWKB(this, partLo + n);
    }

    @Override
    public int getNumRings() {
        int p = partLo;
        return partRingStart[p + 1] - partRingStart[p];
    }

    private int ringIndex(int r) {
        return partRingStart[partLo] + r;
    }

    @Override
    public int getRingSize(int r) {
        return ringCount[ringIndex(r)];
    }

    @Override
    public void copyRingXY(int r, double[] dst, int dstOff, int from, int count) {
        int ri = ringIndex(r);
        reader.order(partOrder[partLo]);
        int pos = ringOffset[ri] + from * strideBytes;
        int o = dstOff;
        for (int i = 0; i < count; i++) {
            dst[o]     = reader.getDouble(pos);
            dst[o + 1] = reader.getDouble(pos + 8);
            o += 2;
            pos += strideBytes;
        }
    }


    @Override public byte[] toEWKB() { return geom.toEWKB(); }
    @Override public Geometry toJTSGeometry() { return geom.toJTSGeometry(); }
    @Override public void write(GeometryWriter writer) throws Exception { geom.write(writer); }
    @Override public int getWKBType() { return geom.getWKBType(); }
    @Override public int getDimension() { return geom.getDimension(); }
    @Override public int getSrid() { return geom.getSrid(); }
    @Override public Envelope boundedBy() { return geom.boundedBy(); }
}
