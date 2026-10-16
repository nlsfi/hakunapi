package fi.nls.hakunapi.tiles.mvt;

import java.nio.charset.StandardCharsets;

/** Copies one layer out of an encoded tile as bytes without decoding; Tile.layers = 3, Layer.name = 1 per the MVT spec. */
public final class MvtLayerExtract {

    private static final int TILE_LAYERS = 3;
    private static final int LAYER_NAME = 1;

    private MvtLayerExtract() {}

    /** Returns null when the tile has no such layer. */
    public static byte[] extract(byte[] tile, String layerName) {
        if (tile == null || layerName == null) {
            return null;
        }
        byte[] wanted = layerName.getBytes(StandardCharsets.UTF_8);

        int pos = 0;
        int end = tile.length;
        while (pos < end) {
            long t = Protobuf.varint(tile, pos);
            int tag = Protobuf.value(t);
            int afterTag = Protobuf.end(t);
            int wire = Protobuf.wire(tag);
            if (Protobuf.field(tag) == TILE_LAYERS && wire == Protobuf.WIRE_LEN) {
                long l = Protobuf.varint(tile, afterTag);
                int len = Protobuf.value(l);
                int body = Protobuf.end(l);
                if (nameMatches(tile, body, body + len, wanted)) {
                    // Copy the whole Tile.layers entry: tag, length, body.
                    int entryLen = body + len - pos;
                    byte[] out = new byte[entryLen];
                    System.arraycopy(tile, pos, out, 0, entryLen);
                    return out;
                }
                pos = body + len;
            } else {
                pos = Protobuf.skip(tile, afterTag, wire);
            }
        }
        return null;
    }

    private static boolean nameMatches(byte[] tile, int pos, int end, byte[] wanted) {
        while (pos < end) {
            long t = Protobuf.varint(tile, pos);
            int tag = Protobuf.value(t);
            int afterTag = Protobuf.end(t);
            int wire = Protobuf.wire(tag);
            if (Protobuf.field(tag) == LAYER_NAME && wire == Protobuf.WIRE_LEN) {
                long l = Protobuf.varint(tile, afterTag);
                int len = Protobuf.value(l);
                int body = Protobuf.end(l);
                if (len != wanted.length) {
                    return false;
                }
                for (int i = 0; i < len; i++) {
                    if (tile[body + i] != wanted[i]) {
                        return false;
                    }
                }
                return true;
            }
            pos = Protobuf.skip(tile, afterTag, wire);
        }
        return false;
    }

}
