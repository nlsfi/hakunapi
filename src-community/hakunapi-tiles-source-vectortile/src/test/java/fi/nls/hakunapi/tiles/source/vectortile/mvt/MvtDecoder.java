package fi.nls.hakunapi.tiles.source.vectortile.mvt;

import java.util.ArrayList;
import java.util.List;

public final class MvtDecoder {
    public static final class Layer {
        public String name;
        public int version;
        public int extent = 4096;
        public final List<String> keys = new ArrayList<>();
        public final List<Object> values = new ArrayList<>();
        public final List<Feature> features = new ArrayList<>();
    }

    public static final class Feature {
        public long id;
        public int type;
        int[] tags = new int[0];
        int[] geometry = new int[0];
    }

    private final byte[] b;
    private int pos;
    private final int end;

    private MvtDecoder(byte[] b, int off, int len) {
        this.b = b;
        this.pos = off;
        this.end = off + len;
    }

    public static List<Layer> decodeTile(byte[] tile) {
        List<Layer> layers = new ArrayList<>();
        MvtDecoder d = new MvtDecoder(tile, 0, tile.length);
        while (d.pos < d.end) {
            int tag = (int) d.varint();
            int field = tag >>> 3;
            int wire = tag & 0x7;
            if (field == 3 && wire == 2) {
                int len = (int) d.varint();
                layers.add(decodeLayer(tile, d.pos, len));
                d.pos += len;
            } else {
                d.skip(wire);
            }
        }
        return layers;
    }

    private static Layer decodeLayer(byte[] b, int off, int len) {
        Layer layer = new Layer();
        MvtDecoder d = new MvtDecoder(b, off, len);
        while (d.pos < d.end) {
            int tag = (int) d.varint();
            int field = tag >>> 3;
            int wire = tag & 0x7;
            switch (field) {
            case 1: layer.name = d.str(); break;
            case 15: layer.version = (int) d.varint(); break;
            case 5: layer.extent = (int) d.varint(); break;
            case 3: layer.keys.add(d.str()); break;
            case 4: {
                int vlen = (int) d.varint();
                layer.values.add(decodeValue(b, d.pos, vlen));
                d.pos += vlen;
                break;
            }
            case 2: {
                int flen = (int) d.varint();
                layer.features.add(decodeFeature(b, d.pos, flen));
                d.pos += flen;
                break;
            }
            default: d.skip(wire);
            }
        }
        return layer;
    }

    private static Feature decodeFeature(byte[] b, int off, int len) {
        Feature f = new Feature();
        MvtDecoder d = new MvtDecoder(b, off, len);
        while (d.pos < d.end) {
            int tag = (int) d.varint();
            int field = tag >>> 3;
            int wire = tag & 0x7;
            switch (field) {
            case 1: f.id = d.varint(); break;
            case 3: f.type = (int) d.varint(); break;
            case 2: f.tags = d.packedUint32(); break;
            case 4: f.geometry = d.packedUint32(); break;
            default: d.skip(wire);
            }
        }
        return f;
    }

    private static Object decodeValue(byte[] b, int off, int len) {
        MvtDecoder d = new MvtDecoder(b, off, len);
        int tag = (int) d.varint();
        int field = tag >>> 3;
        switch (field) {
        case 1: return d.str();
        case 2: return Float.intBitsToFloat(d.fixed32());
        case 3: return Double.longBitsToDouble(d.fixed64());
        case 4: return d.varint();
        case 5: return d.varint();
        case 6: { long z = d.varint(); return (z >>> 1) ^ -(z & 1); }
        case 7: return d.varint() != 0;
        default: throw new IllegalStateException("value field " + field);
        }
    }

    private long varint() {
        long result = 0;
        int shift = 0;
        while (true) {
            int x = b[pos++] & 0xff;
            result |= (long) (x & 0x7f) << shift;
            if ((x & 0x80) == 0) return result;
            shift += 7;
        }
    }

    private String str() {
        int len = (int) varint();
        String s = new String(b, pos, len, java.nio.charset.StandardCharsets.UTF_8);
        pos += len;
        return s;
    }

    private int[] packedUint32() {
        int len = (int) varint();
        int stop = pos + len;
        List<Integer> out = new ArrayList<>();
        while (pos < stop) {
            out.add((int) varint());
        }
        int[] a = new int[out.size()];
        for (int i = 0; i < a.length; i++) a[i] = out.get(i);
        return a;
    }

    private int fixed32() {
        int v = (b[pos] & 0xff) | (b[pos + 1] & 0xff) << 8
              | (b[pos + 2] & 0xff) << 16 | (b[pos + 3] & 0xff) << 24;
        pos += 4;
        return v;
    }

    private long fixed64() {
        long v = 0;
        for (int i = 0; i < 8; i++) {
            v |= (long) (b[pos + i] & 0xff) << (8 * i);
        }
        pos += 8;
        return v;
    }

    private void skip(int wire) {
        switch (wire) {
        case 0: varint(); break;
        case 1: pos += 8; break;
        case 2: { int len = (int) varint(); pos += len; break; }
        case 5: pos += 4; break;
        default: throw new IllegalStateException("wire " + wire);
        }
    }
}
