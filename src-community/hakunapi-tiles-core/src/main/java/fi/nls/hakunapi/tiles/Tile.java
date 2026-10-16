package fi.nls.hakunapi.tiles;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

public interface Tile {

    String getTileMatrixSetId();

    String getTileMatrix();

    long getTileRow();

    long getTileCol();

    String getMediaType();

    /** HTTP Content-Encoding of the body; null when uncompressed. */
    String getContentEncoding();

    /** May be called once. */
    void writeTo(OutputStream out) throws Exception;

    /** Length in bytes, or -1 when not known until the tile has been written. */
    default int getLength() {
        return -1;
    }

    default byte[] getBytes() throws Exception {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        writeTo(b);
        return b.toByteArray();
    }

}
