package fi.nls.hakunapi.core.geom;

import java.nio.ByteBuffer;

public interface WKBBacked extends HakunaGeometry {

    /** Shared buffer; position and byte order belong to the owner, so duplicate before reading. */
    public ByteBuffer getBuffer();

    /** Offset of the geometry body, past the byte order flag and type. */
    public int getDataStart();

    /** Raw WKB geometry type 1-7, without SRID or dimension flags. */
    public int getGeometryType();

    @Override
    public int getDimension();

    public void setSrid(int srid);

    @Override
    public default NavigableHakunaGeometry toNavigable() {
        return new NavigableEWKB(this);
    }

}
