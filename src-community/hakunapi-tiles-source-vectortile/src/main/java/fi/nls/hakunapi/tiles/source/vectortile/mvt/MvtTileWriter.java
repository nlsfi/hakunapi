package fi.nls.hakunapi.tiles.source.vectortile.mvt;

import java.io.IOException;
import java.io.OutputStream;

public final class MvtTileWriter {
    private static final int F_LAYERS = 3;

    private MvtTileWriter() {}

    public static void write(OutputStream out, MvtLayerEncoder... layers) throws IOException {
        for (MvtLayerEncoder layer : layers) {
            writeLayer(out, layer);
        }
    }

    public static void writeLayer(OutputStream out, MvtLayerEncoder layer) throws IOException {
        int len = layer.length();
        writeTag(out, F_LAYERS, BackwardProtoBuf.WIRE_LEN);
        writeVarint(out, len);
        layer.writeTo(out);
    }

    private static void writeTag(OutputStream out, int field, int wire) throws IOException {
        writeVarint(out, ((long) field << 3) | wire);
    }

    private static void writeVarint(OutputStream out, long value) throws IOException {
        long v = value;
        while ((v & ~0x7fL) != 0) {
            out.write((int) ((v & 0x7f) | 0x80));
            v >>>= 7;
        }
        out.write((int) v);
    }
}
