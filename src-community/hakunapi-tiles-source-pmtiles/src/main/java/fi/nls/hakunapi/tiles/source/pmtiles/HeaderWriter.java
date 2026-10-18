package fi.nls.hakunapi.tiles.source.pmtiles;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public final class HeaderWriter {

    public static final byte CLUSTERED = 1;
    public static final byte NOT_CLUSTERED = 0;

    private HeaderWriter() {
    }

    public static byte[] build(
            long rootDirOffset, long rootDirLength,
            long metadataOffset, long metadataLength,
            long leafDirsOffset, long leafDirsLength,
            long tileDataOffset, long tileDataLength,
            long addressedTiles, long tileEntries, long tileContents,
            boolean clustered,
            byte internalCompression, byte tileCompression, byte tileType,
            int minZoom, int maxZoom,
            int[] bboxE7, int[] centerE7, int centerZoom) {

        byte[] header = new byte[PMTilesHeader.LENGTH];
        ByteBuffer b = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);

        byte[] magic = "PMTiles".getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(magic, 0, header, 0, magic.length);
        header[7] = 3;

        b.putLong(8, rootDirOffset);
        b.putLong(16, rootDirLength);
        b.putLong(24, metadataOffset);
        b.putLong(32, metadataLength);
        b.putLong(40, leafDirsOffset);
        b.putLong(48, leafDirsLength);
        b.putLong(56, tileDataOffset);
        b.putLong(64, tileDataLength);
        b.putLong(72, addressedTiles);
        b.putLong(80, tileEntries);
        b.putLong(88, tileContents);

        header[96] = clustered ? CLUSTERED : NOT_CLUSTERED;
        header[97] = internalCompression;
        header[98] = tileCompression;
        header[99] = tileType;
        header[100] = (byte) minZoom;
        header[101] = (byte) maxZoom;

        b.putInt(102, bboxE7[0]);
        b.putInt(106, bboxE7[1]);
        b.putInt(110, bboxE7[2]);
        b.putInt(114, bboxE7[3]);
        header[118] = (byte) centerZoom;
        b.putInt(119, centerE7[0]);
        b.putInt(123, centerE7[1]);

        return header;
    }

}
