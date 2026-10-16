package fi.nls.hakunapi.tiles.source.pmtiles;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public record PMTilesHeader(
        long rootDirOffset,
        long rootDirLength,
        long leafDirsOffset,
        long tileDataOffset,
        byte internalCompression,
        byte tileCompression,
        byte tileType) {
    /** Header size in bytes. */
    public static final int LENGTH = 127;

    private static final byte[] MAGIC = "PMTiles".getBytes(StandardCharsets.US_ASCII);

    public static PMTilesHeader parse(byte[] bytes) throws IOException {
        if (bytes.length < LENGTH) {
            throw new IOException("PMTiles header too short: " + bytes.length);
        }
        for (int i = 0; i < MAGIC.length; i++) {
            if (bytes[i] != MAGIC[i]) {
                throw new IOException("Not a PMTiles archive: bad magic");
            }
        }
        int version = bytes[7] & 0xff;
        if (version != 3) {
            throw new IOException("Unsupported PMTiles version: " + version + " (only v3)");
        }
        ByteBuffer b = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        long rootDirOffset = b.getLong(8);
        long rootDirLength = b.getLong(16);
        long leafDirsOffset = b.getLong(40);
        long tileDataOffset = b.getLong(56);
        byte internalCompression = bytes[97];
        byte tileCompression = bytes[98];
        byte tileType = bytes[99];
        return new PMTilesHeader(rootDirOffset, rootDirLength, leafDirsOffset,
                tileDataOffset, internalCompression, tileCompression, tileType);
    }

    public String tileMediaType() {
        switch (tileType) {
            case 1: return "application/vnd.mapbox-vector-tile";
            case 2: return "image/png";
            case 3: return "image/jpeg";
            case 4: return "image/webp";
            case 5: return "image/avif";
            default: return "application/octet-stream";
        }
    }
}
