package fi.nls.hakunapi.tiles.source.pmtiles;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class FileRangeReader implements RangeReader {
    private final FileChannel channel;

    public FileRangeReader(Path path) throws IOException {
        this.channel = FileChannel.open(path, StandardOpenOption.READ);
    }

    @Override
    public byte[] readRange(long offset, int length) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(length);
        while (buf.hasRemaining()) {
            if (channel.read(buf, offset + buf.position()) < 0) {
                throw new EOFException("PMTiles range past end of file: offset=" + offset + " length=" + length);
            }
        }
        return buf.array();
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }
}
