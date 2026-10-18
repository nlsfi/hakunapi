package fi.nls.hakunapi.tiles.source.pmtiles;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.GZIPOutputStream;

public final class DirectoryWriter {

    private DirectoryWriter() {
    }

    public static byte[] encode(List<Directory.Entry> entries, byte compression) throws IOException {
        byte[] plain = encode(entries);
        if (compression == Compression.NONE || compression == Compression.UNKNOWN) {
            return plain;
        }
        if (compression != Compression.GZIP) {
            throw new IOException("Unsupported PMTiles internal compression: " + compression);
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream(plain.length / 2 + 64);
        try (GZIPOutputStream gz = new GZIPOutputStream(out)) {
            gz.write(plain);
        }
        return out.toByteArray();
    }

    public static byte[] encode(List<Directory.Entry> entries) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(entries.size() * 5 + 16);
        writeVarint(out, entries.size());

        long lastId = 0;
        for (int i = 0; i < entries.size(); i++) {
            long id = entries.get(i).tileId();
            if (id < lastId) {
                throw new IOException("PMTiles directory entries must ascend by tile id, but "
                        + id + " follows " + lastId);
            }
            writeVarint(out, id - lastId);
            lastId = id;
        }
        for (int i = 0; i < entries.size(); i++) {
            writeVarint(out, entries.get(i).runLength());
        }
        for (int i = 0; i < entries.size(); i++) {
            writeVarint(out, entries.get(i).length());
        }
        for (int i = 0; i < entries.size(); i++) {
            Directory.Entry e = entries.get(i);
            if (i > 0) {
                Directory.Entry prev = entries.get(i - 1);
                if (e.offset() == prev.offset() + prev.length()) {
                    // Offset 0 means contiguous with the previous entry (spec).
                    writeVarint(out, 0);
                    continue;
                }
            }
            writeVarint(out, e.offset() + 1);
        }
        return out.toByteArray();
    }

    private static void writeVarint(OutputStream out, long value) throws IOException {
        if (value < 0) {
            throw new IOException("Cannot encode a negative varint: " + value);
        }
        long v = value;
        while (v >= 0x80) {
            out.write((int) ((v & 0x7f) | 0x80));
            v >>>= 7;
        }
        out.write((int) v);
    }

}
