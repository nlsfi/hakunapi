package fi.nls.hakunapi.tiles;

import java.io.IOException;
import java.io.OutputStream;

public class BytesTile implements Tile {

    private final String tileMatrixSetId;
    private final String tileMatrix;
    private final long tileRow;
    private final long tileCol;
    private final String mediaType;
    private final String contentEncoding;
    private final byte[] bytes;

    public BytesTile(String tileMatrixSetId, String tileMatrix, long tileRow, long tileCol,
            String mediaType, byte[] bytes) {
        this(tileMatrixSetId, tileMatrix, tileRow, tileCol, mediaType, null, bytes);
    }

    public BytesTile(String tileMatrixSetId, String tileMatrix, long tileRow, long tileCol,
            String mediaType, String contentEncoding, byte[] bytes) {
        this.tileMatrixSetId = tileMatrixSetId;
        this.tileMatrix = tileMatrix;
        this.tileRow = tileRow;
        this.tileCol = tileCol;
        this.mediaType = mediaType;
        this.contentEncoding = contentEncoding;
        this.bytes = bytes;
    }

    @Override
    public int getLength() {
        return bytes.length;
    }

    @Override
    public String getTileMatrixSetId() {
        return tileMatrixSetId;
    }

    @Override
    public String getTileMatrix() {
        return tileMatrix;
    }

    @Override
    public long getTileRow() {
        return tileRow;
    }

    @Override
    public long getTileCol() {
        return tileCol;
    }

    @Override
    public String getMediaType() {
        return mediaType;
    }

    @Override
    public String getContentEncoding() {
        return contentEncoding;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        out.write(bytes);
    }

    @Override
    public byte[] getBytes() {
        return bytes;
    }

}
