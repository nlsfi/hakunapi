package fi.nls.hakunapi.tiles.generator;

import fi.nls.hakunapi.tiles.mvt.Protobuf;

public final class MvtTile {

    private static final int TILE_LAYERS = 3;
    private static final int LAYER_FEATURES = 2;

    private MvtTile() {}

    public static boolean hasFeatures(byte[] tile) {
        if (tile == null) {
            return false;
        }
        int pos = 0;
        int end = tile.length;
        while (pos < end) {
            long t = Protobuf.varint(tile, pos);
            int tag = Protobuf.value(t);
            pos = Protobuf.end(t);
            int wire = Protobuf.wire(tag);
            if (Protobuf.field(tag) == TILE_LAYERS && wire == Protobuf.WIRE_LEN) {
                long l = Protobuf.varint(tile, pos);
                int len = Protobuf.value(l);
                pos = Protobuf.end(l);
                if (layerHasFeatures(tile, pos, pos + len)) {
                    return true;
                }
                pos += len;
            } else {
                pos = Protobuf.skip(tile, pos, wire);
            }
        }
        return false;
    }

    private static boolean layerHasFeatures(byte[] tile, int pos, int end) {
        while (pos < end) {
            long t = Protobuf.varint(tile, pos);
            int tag = Protobuf.value(t);
            pos = Protobuf.end(t);
            int wire = Protobuf.wire(tag);
            if (Protobuf.field(tag) == LAYER_FEATURES && wire == Protobuf.WIRE_LEN) {
                return true;
            }
            pos = Protobuf.skip(tile, pos, wire);
        }
        return false;
    }

}
