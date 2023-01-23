package fi.nls.hakunapi.core.projection;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class EWKBTransformer {

    private static final int XY_LEN = 128;

    private final ProjectionTransformer t;
    private final double[] xy = new double[XY_LEN];

    public EWKBTransformer(ProjectionTransformer transformer) {
        this.t = transformer;
    }

    public void transformEWKB(byte[] ewkb) throws Exception {
        ByteBuffer bb = ByteBuffer.wrap(ewkb);
        if (bb.get() != 0) {
            bb.order(ByteOrder.LITTLE_ENDIAN);
        }
        int extType = bb.getInt();
        int dimension = (extType & 0x80000000) == 0 ? 2 : 3;
        if (dimension != 2) {
            throw new IllegalArgumentException("Expected 2D geometry");
        }
        boolean hasSRID = (extType & 0x20000000) != 0;
        if (hasSRID) {
            // Overwrite the SRID information
            bb.putInt(t.getToSRID());
        }
        int type = extType & 0x7;
        reprojectEWKBGeometry2d(bb, type);
    }

    private void reprojectEWKBGeometry2d(ByteBuffer bb, int type) throws Exception {
        int numPoints, numRings, numGeoms;
        switch (type) {
        case 1:
            reprojectRing(t, bb, 1);
            break;
        case 2:
            numPoints = bb.getInt();
            reprojectRing(t, bb, numPoints);
            break;
        case 3:
            numRings = bb.getInt();
            for (int ring = 0; ring < numRings; ring++) {
                numPoints = bb.getInt();
                reprojectRing(t, bb, numPoints);
            }
            break;
        case 4:
            numGeoms = bb.getInt();
            for (int i = 0; i < numGeoms; i++) {
                if (bb.get() != 0) {
                    bb.order(ByteOrder.LITTLE_ENDIAN);
                }
                bb.getInt(); // skip type Point
                reprojectRing(t, bb, 1);
            }
            break;
        case 5:
            numGeoms = bb.getInt();
            for (int i = 0; i < numGeoms; i++) {
                if (bb.get() != 0) {
                    bb.order(ByteOrder.LITTLE_ENDIAN);
                }
                bb.getInt(); // skip type LineString
                numPoints = bb.getInt();
                reprojectRing(t, bb, numPoints);
            }
            break;
        case 6:
            numGeoms = bb.getInt();
            for (int i = 0; i < numGeoms; i++) {
                if (bb.get() != 0) {
                    bb.order(ByteOrder.LITTLE_ENDIAN);
                }
                bb.getInt(); // skip type Polygon
                numRings = bb.getInt();
                for (int j = 0; j < numRings; j++) {
                    numPoints = bb.getInt();
                    reprojectRing(t, bb, numPoints);
                }
            }
            break;
        }
    }

    private void reprojectRing(ProjectionTransformer t, ByteBuffer bb, int n) throws Exception {
        int len = 2 * n;
        double[] xy = len > XY_LEN ? new double[len] : this.xy;
        bb.asDoubleBuffer().get(xy, 0, len);
        t.transformInPlace(xy, 0, n);
        bb.asDoubleBuffer().put(xy, 0, len);
        bb.position(bb.position() + Double.BYTES * len);
    }

}
