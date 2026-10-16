package fi.nls.hakunapi.tiles.source.vectortile.mvt;

import java.io.IOException;
import java.io.OutputStream;

/** Protobuf writer that fills its buffer from the end. */
public final class BackwardProtoBuf {
    public static final int WIRE_VARINT = 0;
    public static final int WIRE_FIXED64 = 1;
    public static final int WIRE_LEN = 2;
    public static final int WIRE_FIXED32 = 5;

    private final int chunkSize;

    private static final class Chunk {
        final byte[] data;
        int pos;
        Chunk prev;

        Chunk(int size) {
            this.data = new byte[size];
            this.pos = size;
        }
    }

    private Chunk head;
    private static final int MAX_POOLED_CHUNKS = 32;

    private Chunk pool;
    private int length;

    public BackwardProtoBuf() {
        this(8192);
    }

    public BackwardProtoBuf(int chunkSize) {
        this.chunkSize = Math.max(64, chunkSize);
        this.head = new Chunk(this.chunkSize);
    }

    public void reset() {
        Chunk c = head;
        while (c.prev != null) {
            c = c.prev;
        }
        c.prev = pool;
        pool = head;
        head = newChunk();
        length = 0;
    }

    public void trim() {
        Chunk c = pool;
        int n = 1;
        while (c != null && n < MAX_POOLED_CHUNKS) {
            c = c.prev;
            n++;
        }
        if (c != null) {
            c.prev = null;
        }
    }

    private Chunk newChunk() {
        Chunk c = pool;
        if (c != null) {
            pool = c.prev;
            c.prev = null;
            c.pos = c.data.length;
            return c;
        }
        return new Chunk(chunkSize);
    }

    public int length() {
        return length;
    }

    private void ensurePrimitive(int need) {
        if (head.pos < need) {
            Chunk c = newChunk();
            c.prev = head;
            head = c;
        }
    }

    public void prependByte(int b) {
        ensurePrimitive(1);
        head.data[--head.pos] = (byte) b;
        length++;
    }

    public void prependBytes(byte[] src, int off, int len) {
        length += len;
        int remaining = len;
        while (remaining > 0) {
            if (head.pos == 0) {
                Chunk c = newChunk();
                c.prev = head;
                head = c;
            }
            int take = Math.min(remaining, head.pos);
            head.pos -= take;
            System.arraycopy(src, off + remaining - take, head.data, head.pos, take);
            remaining -= take;
        }
    }

    public void prependVarint(long value) {
        if ((value >>> 7) == 0) {
            ensurePrimitive(1);
            head.data[--head.pos] = (byte) value;
            length++;
            return;
        }
        prependLongVarint(value);
    }

    private void prependLongVarint(long value) {
        long v = value;
        int n = 1;
        long t = v >>> 7;
        while (t != 0) {
            n++;
            t >>>= 7;
        }
        ensurePrimitive(n);
        length += n;
        for (int i = n - 1; i >= 0; i--) {
            int group = (int) ((v >>> (7 * i)) & 0x7f);
            if (i != n - 1) {
                group |= 0x80;
            }
            head.data[--head.pos] = (byte) group;
        }
    }

    public void prependTag(int fieldNumber, int wireType) {
        prependVarint(((long) fieldNumber << 3) | wireType);
    }

    public void prependLengthDelimitedHeader(int fieldNumber, int bodyLen) {
        prependVarint(bodyLen);
        prependTag(fieldNumber, WIRE_LEN);
    }

    public void prependVarintField(int fieldNumber, long value) {
        prependVarint(value);
        prependTag(fieldNumber, WIRE_VARINT);
    }

    public void prependFixed64Field(int fieldNumber, long bits) {
        ensurePrimitive(8);
        for (int i = 7; i >= 0; i--) {
            head.data[--head.pos] = (byte) (bits >>> (8 * i));
        }
        length += 8;
        prependTag(fieldNumber, WIRE_FIXED64);
    }

    public void prependFixed32Field(int fieldNumber, int bits) {
        ensurePrimitive(4);
        for (int i = 3; i >= 0; i--) {
            head.data[--head.pos] = (byte) (bits >>> (8 * i));
        }
        length += 4;
        prependTag(fieldNumber, WIRE_FIXED32);
    }

    public void writeTo(OutputStream out) throws IOException {
        for (Chunk c = head; c != null; c = c.prev) {
            out.write(c.data, c.pos, c.data.length - c.pos);
        }
    }

    public byte[] toByteArray() {
        int len = length();
        byte[] out = new byte[len];
        int p = 0;
        for (Chunk c = head; c != null; c = c.prev) {
            int clen = c.data.length - c.pos;
            System.arraycopy(c.data, c.pos, out, p, clen);
            p += clen;
        }
        return out;
    }
}
