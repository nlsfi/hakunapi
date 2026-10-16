package fi.nls.hakunapi.tiles.source.pmtiles;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class PMTilesArchive implements AutoCloseable {
    private static final int MAX_LEAF_DEPTH = 4;
    private static final long MAX_RANGE_LENGTH = 64L << 20;
    private static final int LEAF_CACHE_SIZE = 64;

    private final RangeReader reader;
    private final PMTilesHeader header;
    private final Directory rootDir;

    private final Map<Long, Directory> leafCache = new LinkedHashMap<>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, Directory> eldest) {
            return size() > LEAF_CACHE_SIZE;
        }
    };

    private PMTilesArchive(RangeReader reader, PMTilesHeader header, Directory rootDir) {
        this.reader = reader;
        this.header = header;
        this.rootDir = rootDir;
    }

    public static PMTilesArchive open(RangeReader reader) throws Exception {
        byte[] headerBytes = reader.readRange(0, PMTilesHeader.LENGTH);
        PMTilesHeader header = PMTilesHeader.parse(headerBytes);
        byte[] rootRaw = reader.readRange(header.rootDirOffset(), toLength(header.rootDirLength()));
        Directory rootDir = Directory.parse(Compression.decode(header.internalCompression(), rootRaw));
        return new PMTilesArchive(reader, header, rootDir);
    }

    public PMTilesHeader getHeader() {
        return header;
    }

    public Optional<byte[]> getTile(int z, long x, long y) throws Exception {
        TileMeta meta = getTileMeta(z, x, y);
        if (meta == null) {
            return Optional.empty();
        }
        return Optional.of(reader.readRange(meta.offset(), meta.length()));
    }

    public TileMeta getTileMeta(int z, long x, long y) throws Exception {
        long tileId = TileId.fromZXY(z, x, y);
        Directory dir = rootDir;
        for (int depth = 0; depth <= MAX_LEAF_DEPTH; depth++) {
            Directory.Entry e = dir.find(tileId);
            if (e == null) {
                return null;
            }
            if (!e.isLeaf()) {
                return new TileMeta(header.tileDataOffset() + e.offset(), toLength(e.length()),
                        header.tileCompression(), header.tileMediaType());
            }
            dir = leafDirectory(e.offset(), toLength(e.length()));
        }
        throw new IllegalStateException("PMTiles leaf directory chain exceeded depth " + MAX_LEAF_DEPTH);
    }

    private Directory leafDirectory(long offset, int length) throws Exception {
        synchronized (leafCache) {
            Directory cached = leafCache.get(offset);
            if (cached != null) {
                return cached;
            }
        }
        byte[] leafRaw = reader.readRange(header.leafDirsOffset() + offset, length);
        Directory dir = Directory.parse(Compression.decode(header.internalCompression(), leafRaw));
        synchronized (leafCache) {
            leafCache.put(offset, dir);
        }
        return dir;
    }

    static int toLength(long v) throws IOException {
        if (v < 0 || v > MAX_RANGE_LENGTH) {
            throw new IOException("PMTiles range length out of range: " + v);
        }
        return (int) v;
    }

    public record TileMeta(long offset, int length, byte compression, String mediaType) {}

    public void forEachEntry(TileIndex.EntryVisitor visitor) throws Exception {
        visit(rootDir, visitor, 0);
    }

    private void visit(Directory dir, TileIndex.EntryVisitor visitor, int depth) throws Exception {
        if (depth > MAX_LEAF_DEPTH) {
            throw new IllegalStateException("PMTiles leaf directory chain exceeded depth " + MAX_LEAF_DEPTH);
        }
        for (int i = 0; i < dir.size(); i++) {
            Directory.Entry e = dir.get(i);
            if (e.isLeaf()) {
                byte[] leafRaw = reader.readRange(header.leafDirsOffset() + e.offset(), toLength(e.length()));
                visit(Directory.parse(Compression.decode(header.internalCompression(), leafRaw)),
                        visitor, depth + 1);
            } else {
                visitor.accept(e.tileId(), e.runLength(), e.offset(), e.length());
            }
        }
    }

    @Override
    public void close() throws Exception {
        reader.close();
    }
}
