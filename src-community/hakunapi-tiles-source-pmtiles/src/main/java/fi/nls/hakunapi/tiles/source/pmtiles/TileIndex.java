package fi.nls.hakunapi.tiles.source.pmtiles;

import java.io.IOException;
import java.util.Arrays;

import fi.nls.hakunapi.tiles.TileLocator;

public class TileIndex implements TileLocator {
    private final long[] ids;
    private final long[] locations;

    private final long tileDataOffset;

    private final byte tileCompression;
    private final String mediaType;

    private TileIndex(long[] ids, long[] locations, long tileDataOffset,
            byte tileCompression, String mediaType) {
        this.ids = ids;
        this.locations = locations;
        this.tileDataOffset = tileDataOffset;
        this.tileCompression = tileCompression;
        this.mediaType = mediaType;
    }

    public static TileIndex build(PMTilesArchive archive) throws Exception {
        Builder b = new Builder();
        archive.forEachEntry((tileId, runLength, offset, length) -> {
            long packed = TileLocator.pack(offset, length);

            for (long i = 0; i < runLength; i++) {
                b.add(tileId + i, packed);
            }
        });

        PMTilesHeader header = archive.getHeader();
        return new TileIndex(b.ids(), b.locations(), header.tileDataOffset(),
                header.tileCompression(), header.tileMediaType());
    }

    private static final class Builder {
        private long[] ids = new long[1024];
        private long[] locations = new long[1024];
        private int n;

        void add(long id, long packed) {
            if (n > 0 && id <= ids[n - 1]) {
                throw new IllegalStateException("PMTiles entries are not in ascending tile id"
                        + " order: " + id + " after " + ids[n - 1]);
            }
            if (n == ids.length) {
                ids = Arrays.copyOf(ids, n * 2);
                locations = Arrays.copyOf(locations, n * 2);
            }
            ids[n] = id;
            locations[n] = packed;
            n++;
        }

        long[] ids() {
            return Arrays.copyOf(ids, n);
        }

        long[] locations() {
            return Arrays.copyOf(locations, n);
        }
    }

    @Override
    public long find(int z, long x, long y) {
        int i = Arrays.binarySearch(ids, TileId.fromZXY(z, x, y));
        if (i < 0) {
            return NOT_FOUND;
        }
        long packed = locations[i];

        return TileLocator.pack(tileDataOffset + TileLocator.offset(packed),
                TileLocator.length(packed));
    }

    @Override
    public String getContentEncoding() {
        return Compression.contentEncoding(tileCompression);
    }

    @Override
    public String getMediaType() {
        return mediaType;
    }

    public int size() {
        return ids.length;
    }

    public long heapBytes() {
        return 2L * ids.length * Long.BYTES;
    }

    void forEachEntry(PackedVisitor visitor) {
        for (int i = 0; i < ids.length; i++) {
            visitor.accept(ids[i], locations[i]);
        }
    }

    long tileDataOffset() {
        return tileDataOffset;
    }

    byte tileCompression() {
        return tileCompression;
    }

    public interface EntryVisitor {
        void accept(long tileId, long runLength, long offset, long length) throws IOException;
    }

    public interface PackedVisitor {
        void accept(long tileId, long packed);
    }
}
