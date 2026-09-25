package fi.nls.hakunapi.bytes.sozip;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import fi.nls.hakunapi.bytes.ByteReader;
import fi.nls.hakunapi.bytes.ByteSource;

/**
 * Opens one Deflate entry of a ZIP archive, read through any ByteSource, as a
 * ByteSource over its uncompressed bytes. The entry needs a SOZip index
 * (https://github.com/sozip/sozip-spec) unless it is small enough to inflate
 * whole.
 *
 * ZIP structures are APPNOTE 6.3.9: end of central directory (4.3.16), Zip64
 * end of central directory locator and record (4.3.15, 4.3.14), central
 * directory header (4.3.12), local file header (4.3.7) and the Zip64 extra
 * field (4.5.3).
 */
public final class SOZip {

    private static final int EOCD_SIG = 0x06054b50;
    private static final int EOCD_LEN = 22;
    private static final int ZIP64_LOCATOR_SIG = 0x07064b50;
    private static final int ZIP64_LOCATOR_LEN = 20;
    private static final int ZIP64_EOCD_SIG = 0x06064b50;
    private static final int ZIP64_EOCD_LEN = 56;
    private static final int CD_SIG = 0x02014b50;
    private static final int CD_LEN = 46;
    private static final int LOCAL_SIG = 0x04034b50;
    private static final int LOCAL_LEN = 30;
    private static final int ZIP64_EXTRA_ID = 0x0001;
    private static final int MAX_COMMENT = 0xFFFF;
    private static final long U32_MAX = 0xFFFFFFFFL;
    private static final int U16_MAX = 0xFFFF;

    private static final int FLAG_ENCRYPTED = 1;
    private static final int FLAG_DATA_DESCRIPTOR = 8;
    private static final int METHOD_STORED = 0;
    private static final int METHOD_DEFLATE = 8;

    private static final int INDEX_HEADER_LEN = 32;
    private static final int INDEX_VERSION = 1;
    private static final int INDEX_OFFSET_SIZE = 8;
    private static final String INDEX_SUFFIX = ".sozip.idx";
    private static final int MAX_CHUNK_SIZE = 100 << 20;
    /**
     * Largest Deflate entry inflated whole when it has no index
     */
    private static final long MAX_UNINDEXED = 1 << 20;

    private SOZip() {
        // Use static methods
    }

    /**
     * Opens entry of archive. The returned source owns the archive: closing it
     * closes the archive, and if opening fails the archive is closed here.
     *
     * @param entry the entry's name, or null for the only entry
     */
    public static SOZipEntry open(ByteSource archive, String entry) throws IOException {
        try (ByteReader h = archive.open()) {
            Entry e = find(h, archive.size(), entry);
            long dataStart = dataStart(h, archive.size(), e);
            if (e.method != METHOD_DEFLATE) {
                throw new IOException("Entry " + e.name + " uses compression method " + e.method
                        + ", only Deflate (8) is supported");
            }
            return index(h, archive, archive.size(), e, dataStart);
        } catch (IOException | RuntimeException | Error t) {
            try {
                archive.close();
            } catch (IOException ignore) {
                // The failure to open is the one worth reporting
            }
            throw t;
        }
    }

    private record Entry(String name, int method, long compressedSize, long uncompressedSize,
            long localHeader) {}

    /**
     * The entry called name in the central directory, or the only one when name is null.
     */
    private static Entry find(ByteReader h, long size, String name) throws IOException {
        // The end of central directory record is at the end, before a comment of up to 64 KiB
        int tailLen = (int) Math.min(size, EOCD_LEN + MAX_COMMENT);
        long tailStart = size - tailLen;
        ByteBuffer tail = read(h, tailStart, tailLen);
        int eocd = -1;
        for (int i = tailLen - EOCD_LEN; i >= 0; i--) {
            if (tail.getInt(i) == EOCD_SIG) {
                eocd = i;
                break;
            }
        }
        if (eocd < 0) {
            throw new IOException("Not a ZIP archive: no end of central directory record");
        }
        long eocdPos = tailStart + eocd;
        int disk = tail.getShort(eocd + 4) & U16_MAX;
        int cdDisk = tail.getShort(eocd + 6) & U16_MAX;
        long count = tail.getShort(eocd + 10) & U16_MAX;
        long cdSize = tail.getInt(eocd + 12) & U32_MAX;
        long cdOffset = tail.getInt(eocd + 16) & U32_MAX;
        long cdLimit = eocdPos;

        long locatorPos = eocdPos - ZIP64_LOCATOR_LEN;
        if (locatorPos >= 0 && read(h, tail, tailStart, locatorPos, 4).getInt(0) == ZIP64_LOCATOR_SIG) {
            ByteBuffer locator = read(h, tail, tailStart, locatorPos, ZIP64_LOCATOR_LEN);
            long zip64Pos = locator.getLong(8);
            if (zip64Pos < 0 || zip64Pos > locatorPos - ZIP64_EOCD_LEN) {
                throw new IOException("Zip64 end of central directory at " + zip64Pos + " is outside the archive");
            }
            ByteBuffer zip64 = read(h, tail, tailStart, zip64Pos, ZIP64_EOCD_LEN);
            if (zip64.getInt(0) != ZIP64_EOCD_SIG) {
                throw new IOException("No Zip64 end of central directory record at " + zip64Pos);
            }
            disk = zip64.getInt(16);
            cdDisk = zip64.getInt(20);
            count = zip64.getLong(32);
            cdSize = zip64.getLong(40);
            cdOffset = zip64.getLong(48);
            cdLimit = zip64Pos;
        }
        if (disk != 0 || cdDisk != 0) {
            throw new IOException("Multi-disk ZIP archives are not supported");
        }
        if (cdOffset < 0 || cdSize < 0 || cdOffset + cdSize > cdLimit || cdSize > Integer.MAX_VALUE) {
            throw new IOException("Central directory of " + cdSize + " bytes at " + cdOffset
                    + " is outside the archive");
        }

        ByteBuffer cd = read(h, tail, tailStart, cdOffset, (int) cdSize);
        Entry only = null;
        int visible = 0;
        for (long i = 0; i < count; i++) {
            Entry e = entry(cd);
            if (name != null) {
                if (e.name.equals(name)) {
                    return e;
                }
            } else if (!e.name.endsWith("/") && !isIndexName(e.name)) {
                only = e;
                visible++;
            }
        }
        if (name != null) {
            throw new IOException("No entry " + name + " in the ZIP archive");
        }
        if (visible != 1) {
            throw new IOException("No entry named and the ZIP archive has " + visible
                    + " entries, name the one to read");
        }
        return only;
    }

    /**
     * The central directory header at cd's position, which it moves past.
     */
    private static Entry entry(ByteBuffer cd) throws IOException {
        int at = cd.position();
        if (cd.remaining() < CD_LEN || cd.getInt(at) != CD_SIG) {
            throw new IOException("Corrupt central directory at entry offset " + at);
        }
        int flags = cd.getShort(at + 8) & U16_MAX;
        int method = cd.getShort(at + 10) & U16_MAX;
        long compressedSize = cd.getInt(at + 20) & U32_MAX;
        long uncompressedSize = cd.getInt(at + 24) & U32_MAX;
        int nameLen = cd.getShort(at + 28) & U16_MAX;
        int extraLen = cd.getShort(at + 30) & U16_MAX;
        int commentLen = cd.getShort(at + 32) & U16_MAX;
        long localHeader = cd.getInt(at + 42) & U32_MAX;
        if (cd.remaining() < CD_LEN + nameLen + extraLen + commentLen) {
            throw new IOException("Corrupt central directory at entry offset " + at);
        }
        // UTF-8 whether or not flag bit 11 says so, as java.util.zip.ZipFile does by default
        String name = new String(cd.array(), cd.arrayOffset() + at + CD_LEN, nameLen, StandardCharsets.UTF_8);

        // Zip64 extra field: only the values whose header field is saturated, in this order
        if (uncompressedSize == U32_MAX || compressedSize == U32_MAX || localHeader == U32_MAX) {
            int zip64 = findExtra(cd, at + CD_LEN + nameLen, extraLen, ZIP64_EXTRA_ID);
            if (zip64 < 0) {
                throw new IOException("Entry " + name + " needs a Zip64 extra field and has none");
            }
            int dataLen = cd.getShort(zip64 + 2) & U16_MAX;
            int p = zip64 + 4;
            int end = p + dataLen;
            if (uncompressedSize == U32_MAX) {
                uncompressedSize = zip64Value(cd, p, end, name);
                p += 8;
            }
            if (compressedSize == U32_MAX) {
                compressedSize = zip64Value(cd, p, end, name);
                p += 8;
            }
            if (localHeader == U32_MAX) {
                localHeader = zip64Value(cd, p, end, name);
            }
        }
        cd.position(at + CD_LEN + nameLen + extraLen + commentLen);
        if ((flags & FLAG_ENCRYPTED) != 0) {
            throw new IOException("Entry " + name + " is encrypted");
        }
        return new Entry(name, method, compressedSize, uncompressedSize, localHeader);
    }

    private static long zip64Value(ByteBuffer bb, int p, int end, String name) throws IOException {
        if (p + 8 > end) {
            throw new IOException("Zip64 extra field of entry " + name + " is too short");
        }
        long v = bb.getLong(p);
        if (v < 0) {
            throw new IOException("Size or offset of entry " + name + " does not fit in a long");
        }
        return v;
    }

    /**
     * The position of the extra field with id in [from, from + len), or -1.
     */
    private static int findExtra(ByteBuffer bb, int from, int len, int id) {
        int p = from;
        int end = from + len;
        while (p + 4 <= end) {
            int dataLen = bb.getShort(p + 2) & U16_MAX;
            if ((bb.getShort(p) & U16_MAX) == id) {
                return p + 4 + dataLen <= end ? p : -1;
            }
            p += 4 + dataLen;
        }
        return -1;
    }

    /**
     * Where e's data starts: after its local header, whose name and extra lengths may differ from the central directory's.
     */
    private static long dataStart(ByteReader h, long size, Entry e) throws IOException {
        if (e.localHeader > size - LOCAL_LEN) {
            throw new IOException("Local header of entry " + e.name + " is outside the archive");
        }
        ByteBuffer local = read(h, e.localHeader, LOCAL_LEN);
        if (local.getInt(0) != LOCAL_SIG) {
            throw new IOException("No local header for entry " + e.name + " at " + e.localHeader);
        }
        long dataStart = e.localHeader + LOCAL_LEN + (local.getShort(26) & U16_MAX) + (local.getShort(28) & U16_MAX);
        if (e.compressedSize > size - dataStart) {
            throw new IOException("Data of entry " + e.name + " runs past the end of the archive");
        }
        return dataStart;
    }

    /**
     * Reads and checks the SOZip index of a Deflate entry. It is the hidden
     * entry whose local header immediately follows e's data, and it is read
     * whole: 8 bytes per chunk, so 2 MiB for an 8 GiB file.
     */
    private static SOZipEntry index(ByteReader h, ByteSource archive, long size, Entry e, long dataStart)
            throws IOException {
        long localPos = dataStart + e.compressedSize;
        if (localPos > size - LOCAL_LEN) {
            return noIndex(archive, e, dataStart, "nothing follows its data");
        }
        ByteBuffer local = read(h, localPos, LOCAL_LEN);
        if (local.getInt(0) != LOCAL_SIG) {
            return noIndex(archive, e, dataStart, "no local header follows its data");
        }
        int flags = local.getShort(6) & U16_MAX;
        int method = local.getShort(8) & U16_MAX;
        long indexLen = local.getInt(22) & U32_MAX;
        int nameLen = local.getShort(26) & U16_MAX;
        int extraLen = local.getShort(28) & U16_MAX;
        if (localPos + LOCAL_LEN + nameLen + extraLen > size) {
            return noIndex(archive, e, dataStart, "the local header after its data is truncated");
        }
        ByteBuffer nameExtra = read(h, localPos + LOCAL_LEN, nameLen + extraLen);
        String name = new String(nameExtra.array(), 0, nameLen, StandardCharsets.UTF_8);
        if (!isIndexOf(name, e.name)) {
            return noIndex(archive, e, dataStart, "the entry after it is " + name + ", not " + indexName(e.name));
        }
        if (method != METHOD_STORED) {
            throw invalidIndex(e, "the index is compressed with method " + method);
        }
        if (indexLen == U32_MAX) {
            // In a local header the Zip64 field holds both sizes, uncompressed first
            int zip64 = findExtra(nameExtra, nameLen, extraLen, ZIP64_EXTRA_ID);
            if (zip64 < 0) {
                throw invalidIndex(e, "the index needs a Zip64 extra field and has none");
            }
            indexLen = zip64Value(nameExtra, zip64 + 4, zip64 + 4 + (nameExtra.getShort(zip64 + 2) & U16_MAX), name);
        }
        long indexStart = localPos + LOCAL_LEN + nameLen + extraLen;
        if (indexLen < INDEX_HEADER_LEN || indexLen > size - indexStart) {
            throw invalidIndex(e, "the index is " + indexLen + " bytes"
                    + ((flags & FLAG_DATA_DESCRIPTOR) != 0 ? " in its local header, which defers sizes to a data descriptor" : ""));
        }

        if (indexLen > Integer.MAX_VALUE) {
            throw invalidIndex(e, "the index is " + indexLen + " bytes");
        }
        // Header and offsets in one read: over HTTP every read is a round trip
        ByteBuffer header = read(h, indexStart, (int) indexLen);
        int version = header.getInt(0);
        long skip = header.getInt(4) & U32_MAX;
        long chunkSize = header.getInt(8) & U32_MAX;
        int offsetSize = header.getInt(12);
        long uncompressedSize = header.getLong(16);
        long compressedSize = header.getLong(24);
        if (version != INDEX_VERSION) {
            throw invalidIndex(e, "version " + version + ", expected " + INDEX_VERSION);
        }
        if (offsetSize != INDEX_OFFSET_SIZE) {
            throw invalidIndex(e, "offset size " + offsetSize + ", expected " + INDEX_OFFSET_SIZE);
        }
        if (uncompressedSize != e.uncompressedSize || compressedSize != e.compressedSize) {
            throw invalidIndex(e, "it is for " + compressedSize + " -> " + uncompressedSize + " bytes, the entry is "
                    + e.compressedSize + " -> " + e.uncompressedSize);
        }
        if (chunkSize == 0 || uncompressedSize <= chunkSize) {
            throw invalidIndex(e, "chunk size " + chunkSize + " for " + uncompressedSize + " bytes");
        }
        if (chunkSize >= MAX_CHUNK_SIZE) {
            throw invalidIndex(e, "chunk size " + chunkSize + ", less than " + MAX_CHUNK_SIZE + " is supported");
        }
        long offsets = (uncompressedSize - 1) / chunkSize;
        if (offsets >= Integer.MAX_VALUE / INDEX_OFFSET_SIZE) {
            throw invalidIndex(e, offsets + " chunks is more than supported");
        }
        if (indexLen != INDEX_HEADER_LEN + skip + offsets * INDEX_OFFSET_SIZE) {
            throw invalidIndex(e, "the index is " + indexLen + " bytes, " + offsets + " chunk offsets need "
                    + (INDEX_HEADER_LEN + skip + offsets * INDEX_OFFSET_SIZE));
        }

        // Chunk 0 starts at 0 and has no entry; the last one ends at the compressed size
        int chunks = (int) offsets + 1;
        long[] chunkStarts = new long[chunks + 1];
        int table = INDEX_HEADER_LEN + (int) skip;
        for (int i = 1; i < chunks; i++) {
            chunkStarts[i] = header.getLong(table + (i - 1) * INDEX_OFFSET_SIZE);
        }
        chunkStarts[chunks] = compressedSize;
        // Strictly ascending and below the compressed size (spec), and no chunk
        // longer than Deflate can make one of chunkSize bytes
        long maxCompressed = 13 + 2 * chunkSize;
        for (int i = 0; i < chunks; i++) {
            long len = chunkStarts[i + 1] - chunkStarts[i];
            if (len <= 0 || len > maxCompressed) {
                throw invalidIndex(e, "chunk " + i + " spans " + chunkStarts[i] + ".." + chunkStarts[i + 1]);
            }
        }
        return new SOZipEntry(archive, dataStart, uncompressedSize, (int) chunkSize, chunkStarts);
    }

    /**
     * A small Deflate entry as one chunk: the whole stream, inflated at once.
     */
    private static SOZipEntry whole(ByteSource archive, Entry e, long dataStart) throws IOException {
        if (e.compressedSize > 13 + 2 * e.uncompressedSize) {
            throw new IOException("Deflate entry " + e.name + " is " + e.compressedSize
                    + " bytes compressed for " + e.uncompressedSize + " bytes, which Deflate cannot produce");
        }
        int chunk = (int) Math.max(1, e.uncompressedSize);
        return new SOZipEntry(archive, dataStart, e.uncompressedSize, chunk, new long[] { 0, e.compressedSize });
    }

    /**
     * dir/.name.sozip.idx for dir/name
     */
    private static String indexName(String name) {
        int slash = name.lastIndexOf('/');
        return name.substring(0, slash + 1) + '.' + name.substring(slash + 1) + INDEX_SUFFIX;
    }

    /**
     * The spec's dir/.name.sozip.idx, or GDAL's, which puts the dot after the
     * first slash (cpl_minizip_zip.cpp): a/.b/c.sozip.idx for a/b/c.
     */
    private static boolean isIndexOf(String candidate, String name) {
        if (candidate.equals(indexName(name))) {
            return true;
        }
        int slash = name.indexOf('/');
        return slash >= 0 && candidate.equals(name.substring(0, slash + 1) + '.' + name.substring(slash + 1) + INDEX_SUFFIX);
    }

    /**
     * An index some writer listed in the central directory, which the spec forbids
     */
    private static boolean isIndexName(String name) {
        int slash = name.lastIndexOf('/');
        return name.endsWith(INDEX_SUFFIX) && name.startsWith(".", slash + 1);
    }

    private static SOZipEntry noIndex(ByteSource archive, Entry e, long dataStart, String why) throws IOException {
        if (e.uncompressedSize <= MAX_UNINDEXED) {
            return whole(archive, e, dataStart);
        }
        throw new IOException("Deflate entry " + e.name + " has no SOZip index (" + why
                + "), and at " + e.uncompressedSize + " bytes it is too large to inflate whole."
                + " sozip --enable-sozip=yes writes one");
    }

    private static IOException invalidIndex(Entry e, String why) {
        return new IOException("SOZip index of entry " + e.name + " is invalid: " + why);
    }

    /**
     * [off, off + len) out of tail when it lies inside it, else read
     */
    private static ByteBuffer read(ByteReader h, ByteBuffer tail, long tailStart, long off, int len)
            throws IOException {
        if (off >= tailStart && off + len <= tailStart + tail.capacity()) {
            return ByteBuffer.wrap(tail.array(), (int) (off - tailStart), len).slice().order(ByteOrder.LITTLE_ENDIAN);
        }
        return read(h, off, len);
    }

    private static ByteBuffer read(ByteReader h, long off, int len) throws IOException {
        byte[] b = new byte[len];
        int read = 0;
        while (read < len) {
            int n = h.read(off + read, b, read, len - read);
            if (n <= 0) {
                throw new IOException("Short read of the ZIP archive at " + (off + read)
                        + ": expected " + len + " bytes, got " + read);
            }
            read += n;
        }
        return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN);
    }

}
