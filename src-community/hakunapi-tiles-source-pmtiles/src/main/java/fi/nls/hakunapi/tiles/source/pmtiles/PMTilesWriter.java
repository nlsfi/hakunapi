package fi.nls.hakunapi.tiles.source.pmtiles;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PMTilesWriter implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(PMTilesWriter.class);

    /** Spec: header plus compressed root directory must fit the first 16384 bytes. */
    static final int ROOT_DIR_MAX = 16_384 - PMTilesHeader.LENGTH;

    private static final int FIRST_LEAF_SIZE = 4096;

    private final Path target;
    private final Path tempData;
    private final FileChannel data;
    private final byte tileCompression;
    private final byte tileType;

    private final List<Directory.Entry> entries = new ArrayList<>();

    private final Map<Long, List<long[]>> byHash = new HashMap<>();

    private long dataLength;
    private long addressedTiles;
    private long lastTileId = -1;
    private boolean closed;
    private int minZoom = Integer.MAX_VALUE;
    private int maxZoom = Integer.MIN_VALUE;
    private boolean finished;

    public PMTilesWriter(Path target, byte tileCompression, byte tileType) throws IOException {
        this.target = target;
        this.tileCompression = tileCompression;
        this.tileType = tileType;
        this.tempData = Files.createTempFile(
                target.toAbsolutePath().getParent(), ".pmtiles-data-", ".tmp");
        this.data = FileChannel.open(tempData, StandardOpenOption.WRITE, StandardOpenOption.READ);
    }

    /** Tile ids must ascend: runs only collapse consecutive ids. */
    public void add(int z, long x, long y, byte[] tile) throws IOException {
        if (finished) {
            throw new IllegalStateException("Archive already finished");
        }
        long tileId = TileId.fromZXY(z, x, y);
        if (tileId <= lastTileId) {
            throw new IllegalArgumentException("Tiles must be added in ascending tile id order, but "
                    + z + "/" + x + "/" + y + " is id " + tileId + " after " + lastTileId);
        }
        lastTileId = tileId;
        addressedTiles++;
        minZoom = Math.min(minZoom, z);
        maxZoom = Math.max(maxZoom, z);

        ByteBuffer body = ByteBuffer.wrap(tile);
        long hash = hash(tile);
        long offset = findDuplicate(body, hash);
        if (offset < 0) {
            offset = dataLength;
            writeFully(data, body);
            dataLength += tile.length;
            byHash.computeIfAbsent(hash, __ -> new ArrayList<>())
                    .add(new long[] { offset, tile.length });
        }

        if (!entries.isEmpty()) {
            Directory.Entry last = entries.get(entries.size() - 1);
            if (last.tileId() + last.runLength() == tileId
                    && last.offset() == offset && last.length() == tile.length) {
                entries.set(entries.size() - 1, new Directory.Entry(
                        last.tileId(), last.runLength() + 1, last.offset(), last.length()));
                return;
            }
        }
        entries.add(new Directory.Entry(tileId, 1, offset, tile.length));
    }

    private long findDuplicate(ByteBuffer tile, long hash) throws IOException {
        List<long[]> candidates = byHash.get(hash);
        if (candidates == null) {
            return -1;
        }
        ByteBuffer buf = null;
        for (int i = 0; i < candidates.size(); i++) {
            long[] candidate = candidates.get(i);
            if (candidate[1] != tile.remaining()) {
                continue;
            }
            if (buf == null) {
                buf = ByteBuffer.allocate(tile.remaining());
            }
            buf.clear();
            readFully(buf, candidate[0]);
            if (buf.flip().equals(tile)) {
                return candidate[0];
            }
        }
        return -1;
    }

    private static void writeFully(FileChannel ch, ByteBuffer buf) throws IOException {
        while (buf.hasRemaining()) {
            ch.write(buf);
        }
    }

    private void readFully(ByteBuffer buf, long position) throws IOException {
        long at = position;
        while (buf.hasRemaining()) {
            int n = data.read(buf, at);
            if (n < 0) {
                throw new IOException("Truncated tile data at " + at);
            }
            at += n;
        }
    }

    private static long hash(byte[] bytes) {
        long h = 0xcbf29ce484222325L;
        for (int i = 0; i < bytes.length; i++) {
            h ^= bytes[i] & 0xff;
            h *= 0x100000001b3L;
        }
        return h;
    }

    public long finish(String metadataJson, int[] bboxE7, int[] centerE7, int centerZoom)
            throws IOException {
        if (finished) {
            throw new IllegalStateException("Archive already finished");
        }
        finished = true;
        if (entries.isEmpty()) {
            throw new IllegalStateException("Refusing to write an archive with no tiles");
        }

        byte internal = Compression.GZIP;
        byte[] metadata = gzip(metadataJson.getBytes(StandardCharsets.UTF_8));
        Directories dirs = buildDirectories(internal);

        long rootDirOffset = PMTilesHeader.LENGTH;
        long metadataOffset = rootDirOffset + dirs.root.length;
        long leafDirsOffset = metadataOffset + metadata.length;
        long tileDataOffset = leafDirsOffset + dirs.leaves.length;

        byte[] header = HeaderWriter.build(
                rootDirOffset, dirs.root.length,
                metadataOffset, metadata.length,
                leafDirsOffset, dirs.leaves.length,
                tileDataOffset, dataLength,
                addressedTiles, entries.size(), countDistinct(),
                true, internal, tileCompression, tileType,
                minZoom, maxZoom, bboxE7, centerE7, centerZoom);

        Path tmp = target.resolveSibling(target.getFileName() + ".tmp");
        long total;
        try {
            total = writeArchive(tmp, header, dirs.root, metadata, dirs.leaves);
            Files.move(tmp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException | RuntimeException e) {
            try {
                Files.deleteIfExists(tmp);
            } catch (IOException suppressed) {
                e.addSuppressed(suppressed);
            }
            throw e;
        }

        LOG.info("Wrote {} ({} addressed tiles, {} entries, {} distinct bodies, {} leaf dirs, {} bytes)",
                target, addressedTiles, entries.size(), countDistinct(), dirs.leafCount, total);
        return total;
    }

    private long writeArchive(Path out, byte[] header, byte[] root, byte[] metadata, byte[] leaves)
            throws IOException {
        try (FileChannel ch = FileChannel.open(out, StandardOpenOption.CREATE,
                StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writeFully(ch, ByteBuffer.wrap(header));
            writeFully(ch, ByteBuffer.wrap(root));
            writeFully(ch, ByteBuffer.wrap(metadata));
            writeFully(ch, ByteBuffer.wrap(leaves));
            long moved = 0;
            while (moved < dataLength) {
                long n = data.transferTo(moved, dataLength - moved, ch);
                if (n <= 0) {
                    throw new IOException("Failed to copy tile data at " + moved);
                }
                moved += n;
            }
            return ch.position();
        }
    }

    private long countDistinct() {
        long n = 0;
        for (List<long[]> bucket : byHash.values()) {
            n += bucket.size();
        }
        return n;
    }

    private Directories buildDirectories(byte internal) throws IOException {
        byte[] flat = DirectoryWriter.encode(entries, internal);
        if (flat.length <= ROOT_DIR_MAX) {
            return new Directories(flat, new byte[0], 0);
        }
        for (int leafSize = FIRST_LEAF_SIZE;; leafSize *= 2) {
            List<Directory.Entry> root = new ArrayList<>();
            ByteArrayOutputStream leaves = new ByteArrayOutputStream();
            int leafCount = 0;
            for (int i = 0; i < entries.size(); i += leafSize) {
                List<Directory.Entry> chunk = entries.subList(i, Math.min(i + leafSize, entries.size()));
                byte[] encoded = DirectoryWriter.encode(chunk, internal);
                // runLength 0 marks a leaf pointer; offset/length address the leaf section.
                root.add(new Directory.Entry(chunk.get(0).tileId(), 0, leaves.size(), encoded.length));
                leaves.write(encoded, 0, encoded.length);
                leafCount++;
            }
            byte[] rootBytes = DirectoryWriter.encode(root, internal);
            if (rootBytes.length <= ROOT_DIR_MAX) {
                return new Directories(rootBytes, leaves.toByteArray(), leafCount);
            }
        }
    }

    private record Directories(byte[] root, byte[] leaves, int leafCount) {
    }

    private static byte[] gzip(byte[] bytes) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(bytes.length / 2 + 64);
        try (OutputStream gz = new GZIPOutputStream(out)) {
            gz.write(bytes);
        }
        return out.toByteArray();
    }

    public long getAddressedTiles() {
        return addressedTiles;
    }

    /** Idempotent and never throws, so cleanup cannot fail a finished archive. */
    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        try {
            data.close();
        } catch (IOException e) {
            LOG.warn("Failed to close the temporary tile data file {}", tempData, e);
        }
        try {
            Files.deleteIfExists(tempData);
        } catch (IOException e) {
            LOG.warn("Failed to delete the temporary tile data file {}", tempData, e);
        }
    }

}
