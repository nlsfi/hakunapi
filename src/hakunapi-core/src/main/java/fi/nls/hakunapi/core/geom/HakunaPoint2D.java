package fi.nls.hakunapi.core.geom;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBConstants;

import fi.nls.hakunapi.core.GeometryWriter;
import fi.nls.hakunapi.core.util.GeoPackageWKBWriter;

/**
 * Use HakunaGeometryJTS instead
 */
@Deprecated
public class HakunaPoint2D implements HakunaGeometry {

    private final double x;
    private final double y;
    private final int srid;

    public HakunaPoint2D(double x, double y, int srid) {
        this.x = x;
        this.y = y;
        this.srid = srid;
    }

    @Override
    public void write(GeometryWriter writer) throws Exception {
        writer.init(HakunaGeometryType.POINT, srid, 2);
        writer.writeCoordinate(x, y);
        writer.end();
    }

    @Override
    public Geometry toJTSGeometry() {
        Geometry g = HakunaGeometryFactory.GF.createPoint(new Coordinate(x, y));
        g.setSRID(srid);
        return g;
    }

    @Override
    public int getWKBLength() {
        return 1 + 4 + 16;
    }

    @Override
    public void toWKB(byte[] buf, int off) {
        ByteBuffer bb = ByteBuffer.wrap(buf, off, buf.length - off);
        bb.order(ByteOrder.nativeOrder());
        bb.put(ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN ? GeoPackageWKBWriter.WKB_LITTLE_ENDIAN : GeoPackageWKBWriter.WKB_BIG_ENDIAN);
        bb.putInt(getWKBType());
        bb.putDouble(x);
        bb.putDouble(y);
    }

    @Override
    public byte[] toEWKB() {
        byte[] ewkb = new byte[1 + 4 + 4 + 16];
        ByteBuffer bb = ByteBuffer.wrap(ewkb);
        bb.put((byte) 0);
        int hasSrid = 0x20000000;
        int type = hasSrid + 1;
        bb.putInt(type);
        bb.putInt(srid);
        bb.putDouble(x);
        bb.putDouble(y);
        return ewkb;
    }

    @Override
    public Envelope boundedBy() {
        return new Envelope(x, x, y, y); 
    }

    @Override
    public int getSrid() {
        return srid;
    }

    @Override
    public int getWKBType() {
        return WKBConstants.wkbPoint;
    }

    @Override
    public int getDimension() {
        return 2;
    }

}
