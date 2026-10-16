package fi.nls.hakunapi.tiles.source.vectortile.mvt;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import fi.nls.hakunapi.core.util.LocalDateOutput;

/** Builds one MVT layer backwards so name, keys and values can precede the features. */
public final class MvtLayerEncoder {
    private static final int F_NAME = 1;
    private static final int F_FEATURES = 2;
    private static final int F_KEYS = 3;
    private static final int F_VALUES = 4;
    private static final int F_EXTENT = 5;
    private static final int F_VERSION = 15;

    private static final int FE_ID = 1;
    private static final int FE_TAGS = 2;
    private static final int FE_TYPE = 3;
    private static final int FE_GEOMETRY = 4;

    public static final int GEOM_POINT = 1;
    public static final int GEOM_LINE = 2;
    public static final int GEOM_POLYGON = 3;

    private static final int MVT_VERSION = 2;

    private static final int RETAINED_VALUES = 16384;

    private static final int SLOT_DATE = -1;

    private static final int MAX_VARINT32_BYTES = 5;

    private static final int RETAINED_PACK_SCRATCH = 64 * 1024;

    private final BackwardProtoBuf buf = new BackwardProtoBuf();

    private final byte[] dateScratch = new byte[LocalDateOutput.MAX_BYTE_LEN];

    private byte[] packScratch = new byte[RETAINED_PACK_SCRATCH];

    private String name;
    private int extent = 4096;

    private final Map<String, Integer> keyIndex = new HashMap<>();
    private byte[][] keyUtf8 = new byte[16][];
    private int keyCount;

    private int[] valueType = new int[16];
    private long[] valueBits = new long[16];
    private byte[][] valueUtf8 = new byte[16][];
    private int valueCount;

    private int[] valueHashSlots = new int[32];
    private int valueHashMask = valueHashSlots.length - 1;

    private int[] tags = new int[32];
    private int tagCount;

    private int featureCount;

    public void reset(String layerName) {
        buf.reset();
        this.name = layerName;
        keyIndex.clear();
        keyCount = 0;
        valueCount = 0;
        java.util.Arrays.fill(valueHashSlots, 0);
        tagCount = 0;
        featureCount = 0;
    }

    public void trim() {
        buf.trim();
        if (valueType.length > RETAINED_VALUES) {
            valueType = new int[RETAINED_VALUES];
            valueBits = new long[RETAINED_VALUES];
            valueUtf8 = new byte[RETAINED_VALUES][];
        } else {
            java.util.Arrays.fill(valueUtf8, 0, valueCount, null);
        }
        if (valueHashSlots.length > RETAINED_VALUES * 2) {
            valueHashSlots = new int[RETAINED_VALUES * 2];
            valueHashMask = valueHashSlots.length - 1;
        }
        if (packScratch.length > RETAINED_PACK_SCRATCH) {
            packScratch = new byte[RETAINED_PACK_SCRATCH];
        }
    }

    public void setExtent(int extent) {
        this.extent = extent;
    }

    public int length() {
        return buf.length();
    }

    public int getFeatureCount() {
        return featureCount;
    }

    public int keyIndex(String key) {
        Integer idx = keyIndex.get(key);
        if (idx != null) {
            return idx;
        }
        int i = keyCount;
        if (i == keyUtf8.length) {
            byte[][] g = new byte[i << 1][];
            System.arraycopy(keyUtf8, 0, g, 0, i);
            keyUtf8 = g;
        }
        keyUtf8[i] = key.getBytes(StandardCharsets.UTF_8);
        keyIndex.put(key, i);
        keyCount = i + 1;
        return i;
    }

    private int internNumeric(int type, long bits) {
        int h = hashNumeric(type, bits) & valueHashMask;
        while (true) {
            int slot = valueHashSlots[h];
            if (slot == 0) {
                int i = appendNumeric(type, bits);
                putSlot(h, i);
                return i;
            }
            int vi = slot - 1;
            if (valueType[vi] == type && valueUtf8[vi] == null && valueBits[vi] == bits) {
                return vi;
            }
            h = (h + 1) & valueHashMask;
        }
    }

    private int internString(byte[] utf8) {
        int h = hashBytes(utf8) & valueHashMask;
        while (true) {
            int slot = valueHashSlots[h];
            if (slot == 0) {
                int i = appendString(utf8);
                putSlot(h, i);
                return i;
            }
            int vi = slot - 1;
            if (valueType[vi] == MvtValue.STRING
                    && java.util.Arrays.equals(valueUtf8[vi], utf8)) {
                return vi;
            }
            h = (h + 1) & valueHashMask;
        }
    }

    private int appendNumeric(int type, long bits) {
        int i = ensureValueSlot();
        valueType[i] = type;
        valueBits[i] = bits;
        valueUtf8[i] = null;
        valueCount = i + 1;
        return i;
    }

    private int appendString(byte[] utf8) {
        int i = ensureValueSlot();
        valueType[i] = MvtValue.STRING;
        valueUtf8[i] = utf8;
        valueCount = i + 1;
        return i;
    }

    private int ensureValueSlot() {
        int i = valueCount;
        if (i == valueType.length) {
            int cap = i << 1;
            int[] gt = new int[cap];
            long[] gb = new long[cap];
            byte[][] gu = new byte[cap][];
            System.arraycopy(valueType, 0, gt, 0, i);
            System.arraycopy(valueBits, 0, gb, 0, i);
            System.arraycopy(valueUtf8, 0, gu, 0, i);
            valueType = gt;
            valueBits = gb;
            valueUtf8 = gu;
        }
        return i;
    }

    private void putSlot(int h, int i) {
        valueHashSlots[h] = i + 1;
        if ((valueCount << 2) >= (valueHashSlots.length * 3)) {
            rehash();
        }
    }

    private void rehash() {
        int[] old = valueHashSlots;
        int cap = old.length << 1;
        valueHashSlots = new int[cap];
        valueHashMask = cap - 1;
        for (int s = 0; s < valueCount; s++) {
            int h = (valueType[s] == MvtValue.STRING ? hashBytes(valueUtf8[s])
                    : hashNumeric(valueType[s], valueBits[s])) & valueHashMask;
            while (valueHashSlots[h] != 0) {
                h = (h + 1) & valueHashMask;
            }
            valueHashSlots[h] = s + 1;
        }
    }

    private static int hashNumeric(int type, long bits) {
        long h = bits * 0x9E3779B97F4A7C15L + type;
        return (int) (h ^ (h >>> 32));
    }

    private static int hashBytes(byte[] a) {
        int h = 1;
        for (int i = 0; i < a.length; i++) {
            h = 31 * h + a[i];
        }
        return h;
    }

    private void addTag(int keyIdx, int valueIdx) {
        if (tagCount + 2 > tags.length) {
            int[] g = new int[tags.length << 1];
            System.arraycopy(tags, 0, g, 0, tagCount);
            tags = g;
        }
        tags[tagCount++] = keyIdx;
        tags[tagCount++] = valueIdx;
    }

    public void tagString(int keyIdx, String value) {
        tagStringUtf8(keyIdx, value.getBytes(StandardCharsets.UTF_8));
    }

    public void tagStringUtf8(int keyIdx, byte[] utf8) {
        addTag(keyIdx, internString(utf8));
    }

    public void tagDouble(int keyIdx, double value) {
        addTag(keyIdx, internNumeric(MvtValue.DOUBLE, Double.doubleToLongBits(value)));
    }

    public void tagFloat(int keyIdx, float value) {
        addTag(keyIdx, internNumeric(MvtValue.FLOAT, Float.floatToIntBits(value) & 0xffffffffL));
    }

    public void tagLong(int keyIdx, long value) {
        addTag(keyIdx, internNumeric(MvtValue.INT, value));
    }

    public void tagBool(int keyIdx, boolean value) {
        addTag(keyIdx, internNumeric(MvtValue.BOOL, value ? 1L : 0L));
    }

    public void tagDate(int keyIdx, LocalDate date) {
        addTag(keyIdx, internNumeric(SLOT_DATE, packDate(date)));
    }

    private static long packDate(LocalDate date) {
        return ((long) date.getYear() << 9) | (date.getMonthValue() << 5) | date.getDayOfMonth();
    }

    public void beginFeature() {
        tagCount = 0;
    }

    public void endFeature(long id, int geomType, int[] geometry, int geomLen) {
        featureCount++;
        int before = buf.length();

        int geomBytes = prependPackedVarints(geometry, geomLen);
        buf.prependLengthDelimitedHeader(FE_GEOMETRY, geomBytes);

        buf.prependVarintField(FE_TYPE, geomType);

        int tagBytes = prependPackedVarints(tags, tagCount);
        buf.prependLengthDelimitedHeader(FE_TAGS, tagBytes);

        if (id != 0) {
            buf.prependVarintField(FE_ID, id);
        }

        int featureLen = buf.length() - before;
        buf.prependLengthDelimitedHeader(F_FEATURES, featureLen);
    }

    public void finish() {
        for (int i = valueCount - 1; i >= 0; i--) {
            int before = buf.length();
            prependValueBody(i);
            int len = buf.length() - before;
            buf.prependLengthDelimitedHeader(F_VALUES, len);
        }
        for (int i = keyCount - 1; i >= 0; i--) {
            byte[] k = keyUtf8[i];
            buf.prependBytes(k, 0, k.length);
            buf.prependLengthDelimitedHeader(F_KEYS, k.length);
        }
        buf.prependVarintField(F_EXTENT, extent);
        byte[] nm = name.getBytes(StandardCharsets.UTF_8);
        buf.prependBytes(nm, 0, nm.length);
        buf.prependLengthDelimitedHeader(F_NAME, nm.length);
        buf.prependVarintField(F_VERSION, MVT_VERSION);
    }

    private void prependValueBody(int i) {
        int type = valueType[i];
        switch (type) {
        case SLOT_DATE:
            long packed = valueBits[i];
            int len = LocalDateOutput.outputLocalDate(
                    (int) (packed >> 9), (int) (packed >> 5) & 0xf, (int) (packed & 0x1f),
                    dateScratch, 0);
            buf.prependBytes(dateScratch, 0, len);
            buf.prependLengthDelimitedHeader(MvtValue.STRING, len);
            break;
        case MvtValue.STRING:
            byte[] s = valueUtf8[i];
            MvtValue.prependString(buf, s);
            break;
        case MvtValue.DOUBLE:
            MvtValue.prependDouble(buf, Double.longBitsToDouble(valueBits[i]));
            break;
        case MvtValue.FLOAT:
            MvtValue.prependFloat(buf, Float.intBitsToFloat((int) valueBits[i]));
            break;
        case MvtValue.INT:
            MvtValue.prependInt(buf, valueBits[i]);
            break;
        case MvtValue.BOOL:
            MvtValue.prependBool(buf, valueBits[i] != 0);
            break;
        default:
            throw new IllegalStateException("value type " + type);
        }
    }

    public void writeTo(OutputStream out) throws IOException {
        buf.writeTo(out);
    }

    private int prependPackedVarints(int[] a, int len) {
        int need = len * MAX_VARINT32_BYTES;
        if (packScratch.length < need) {
            packScratch = new byte[Math.max(64, Integer.highestOneBit(need - 1) << 1)];
        }
        byte[] out = packScratch;
        int p = 0;
        for (int i = 0; i < len; i++) {
            int v = a[i];
            while ((v & ~0x7f) != 0) {
                out[p++] = (byte) (v | 0x80);
                v >>>= 7;
            }
            out[p++] = (byte) v;
        }
        buf.prependBytes(out, 0, p);
        return p;
    }
}
