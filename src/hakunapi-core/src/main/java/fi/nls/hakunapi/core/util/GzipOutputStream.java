package fi.nls.hakunapi.core.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * Like GZIPOutputStream but you can provide the Deflater
 * (and set the compression level).
 *
 * Also there's no synchronization compared to GZIPOutputStream
 * so don't try writing from multiple threads at the same time
 * (but why would you)
 */
public class GzipOutputStream extends DeflaterOutputStream {

    private CRC32 crc;

    public GzipOutputStream(OutputStream out, Deflater def, int size) throws IOException {
        super(out, def, size);
        crc = new CRC32();
        byte[] header = new byte[] {
                (byte) (0x8b1f >> 0), // ID1
                (byte) (0x8b1f >> 8), // ID2
                Deflater.DEFLATED, // CM
                0, // FLG
                0, // MTIME1
                0, // MTIME2
                0, // MTIME3
                0, // MTIME4
                0, // XFL
                (byte) 255 // OS, changed from 0 to 255 in Java 16, see https://bugs.openjdk.org/browse/JDK-8244706
        };
        out.write(header, 0, header.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        super.write(b, off, len);
        crc.update(b, off, len);
    }

    @Override
    public void finish() throws IOException {
        super.finish();
        ByteBuffer bb = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt((int) crc.getValue());
        bb.putInt(def.getTotalIn());
        out.write(bb.array(), 0, 8);
    }

    @Override
    public void close() throws IOException {
        super.close();
        def.end();
    }

}
