package fi.nls.hakunapi.tiles.source.pmtiles;

import java.io.IOException;

public final class Directory {
    private final long[] tileIds;
    private final long[] runLengths;
    private final long[] offsets;
    private final long[] lengths;

    private Directory(long[] tileIds, long[] runLengths, long[] offsets, long[] lengths) {
        this.tileIds = tileIds;
        this.runLengths = runLengths;
        this.offsets = offsets;
        this.lengths = lengths;
    }

    public static Directory parse(byte[] bytes) throws IOException {
        Varint v = new Varint(bytes);
        long count = v.next();
        if (count < 0 || count > bytes.length / 4) {
            throw new IOException("PMTiles directory entry count out of range: " + count);
        }
        int n = (int) count;
        long[] tileIds = new long[n];
        long[] runLengths = new long[n];
        long[] offsets = new long[n];
        long[] lengths = new long[n];

        long lastId = 0;
        for (int i = 0; i < n; i++) {
            lastId += v.next();
            tileIds[i] = lastId;
        }
        for (int i = 0; i < n; i++) {
            runLengths[i] = v.next();
        }
        for (int i = 0; i < n; i++) {
            lengths[i] = v.next();
        }
        for (int i = 0; i < n; i++) {
            long o = v.next();
            if (o == 0) {
                offsets[i] = i > 0 ? offsets[i - 1] + lengths[i - 1] : 0;
            } else {
                offsets[i] = o - 1;
            }
        }
        return new Directory(tileIds, runLengths, offsets, lengths);
    }

    public Entry find(long tileId) {
        int lo = 0;
        int hi = tileIds.length - 1;
        int found = -1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            if (tileIds[mid] <= tileId) {
                found = mid;
                lo = mid + 1;
            } else {
                hi = mid - 1;
            }
        }
        if (found < 0) {
            return null;
        }
        long rl = runLengths[found];
        if (rl == 0) {
            return new Entry(tileIds[found], 0, offsets[found], lengths[found]);
        }
        if (tileId < tileIds[found] + rl) {
            return new Entry(tileIds[found], rl, offsets[found], lengths[found]);
        }
        return null;
    }

    public int size() {
        return tileIds.length;
    }

    public Entry get(int i) {
        return new Entry(tileIds[i], runLengths[i], offsets[i], lengths[i]);
    }

    public record Entry(long tileId, long runLength, long offset, long length) {
        public boolean isLeaf() {
            return runLength == 0;
        }
    }

    private static final class Varint {
        private final byte[] buf;
        private int pos;

        Varint(byte[] buf) {
            this.buf = buf;
        }

        long next() throws IOException {
            long result = 0;
            int shift = 0;
            while (true) {
                if (pos >= buf.length) {
                    throw new IOException("Truncated PMTiles directory varint");
                }
                int b = buf[pos++] & 0xff;
                result |= (long) (b & 0x7f) << shift;
                if ((b & 0x80) == 0) {
                    return result;
                }
                shift += 7;
            }
        }
    }
}
