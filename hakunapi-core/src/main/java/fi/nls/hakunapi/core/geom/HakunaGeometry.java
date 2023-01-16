package fi.nls.hakunapi.core.geom;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import fi.nls.hakunapi.core.GeometryWriter;

public interface HakunaGeometry {

    public byte[] toEWKB();
    public Geometry toJTSGeometry();
    public void write(GeometryWriter writer) throws Exception;

    public default byte[] toWKB() {
        byte[] buf = new byte[getWKBLength()];
        toWKB(buf, 0);
        return buf;
    }

    public default void toWKB(byte[] buf, int off) {
        new HakunaGeometryEWKB(toEWKB()).toWKB(buf, off);
    }
    
    public default int getWKBLength() {
        return new HakunaGeometryEWKB(toEWKB()).getWKBLength();
    }

    public default int getWKBType() {
        return new HakunaGeometryEWKB(toEWKB()).getWKBType();
    }

    public default int getDimension() {
        return new HakunaGeometryEWKB(toEWKB()).getDimension();
    }

    public default int getSrid() {
        return new HakunaGeometryEWKB(toEWKB()).getSrid();
    }

    public default Envelope boundedBy() {
        return new HakunaGeometryEWKB(toEWKB()).boundedBy();
    }
    
}
