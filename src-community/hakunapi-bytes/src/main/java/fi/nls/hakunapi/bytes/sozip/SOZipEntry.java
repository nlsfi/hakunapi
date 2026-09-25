package fi.nls.hakunapi.bytes.sozip;

import java.io.IOException;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import fi.nls.hakunapi.bytes.ByteReader;
import fi.nls.hakunapi.bytes.ByteSource;

/**
 * A Deflate entry of a SOZip archive, read in its uncompressed address space.
 * A read inflates only the chunks it covers, from one read of the archive.
 * Whole chunks are inflated straight into the destination, and a chunk covered
 * in part into the reader's scratch, which is kept for the next read.
 */
public class SOZipEntry implements ByteSource {

    /**
     * Most compressed bytes staged for one read. A large read is inflated in
     * batches of this, so the staging array stays bounded; a single chunk larger
     * than this is still read whole.
     */
    private static final int MAX_STAGING = 16 << 20;

    /**
     * The empty stored block Z_FULL_FLUSH ends a chunk with.
     */
    private static final byte[] FULL_FLUSH = { 0, 0, 0, (byte) 0xFF, (byte) 0xFF };

    private final ByteSource archive;
    private final long dataStart;
    private final long size;
    private final int chunkSize;
    private final int lastChunk;
    private final long[] chunkStarts;

    /**
     * @param dataStart where the compressed data starts in the archive
     * @param size uncompressed size
     * @param chunkStarts the compressed offset of every chunk relative to
     *        dataStart, plus the compressed size as the end of the last one;
     *        validated by {@link SOZip}. A single chunk ({0, compressed size})
     *        is a Deflate stream read whole.
     */
    public SOZipEntry(ByteSource archive, long dataStart, long size, int chunkSize, long[] chunkStarts) {
        this.archive = archive;
        this.dataStart = dataStart;
        this.size = size;
        this.chunkSize = chunkSize;
        this.lastChunk = chunkStarts.length - 2;
        this.chunkStarts = chunkStarts;
    }

    @Override
    public long size() {
        return size;
    }

    /**
     * Uncompressed bytes per chunk; the last one may be shorter
     */
    public int chunkSize() {
        return chunkSize;
    }

    @Override
    public ByteReader open() throws IOException {
        return new Reader(archive.open());
    }

    @Override
    public void close() throws IOException {
        archive.close();
    }

    private class Reader implements ByteReader {

        private final ByteReader archive;
        private final Inflater inflater = new Inflater(true);
        /**
         * Compressed bytes of the chunks being read, reused across reads
         */
        private byte[] staging;
        /**
         * The last chunk a read covered only in part, inflated
         */
        private byte[] scratch;
        private int scratchChunk = -1;
        /**
         * Takes the one byte a corrupt chunk would still produce past its end
         */
        private final byte[] overrun = new byte[1];
        private MemorySegment lastSegment;
        private ByteBuffer segmentBuffer;
        private boolean closed;

        private Reader(ByteReader archive) {
            this.archive = archive;
        }

        @Override
        public int read(long off, MemorySegment dst, long dstOff, int len) throws IOException {
            if (len <= 0 || off >= size) {
                return 0;
            }
            if (closed) {
                throw new ClosedChannelException();
            }
            // A segment over 2 GB has no ByteBuffer view, so only the part read is viewed
            if (dst.byteSize() > Integer.MAX_VALUE) {
                lastSegment = null;
                segmentBuffer = dst.asSlice(dstOff, Math.min(len, size - off)).asByteBuffer();
                return read(off, len, null, dstOff, dst, 0);
            }
            if (dst != lastSegment) {
                lastSegment = dst;
                segmentBuffer = dst.asByteBuffer();
            }
            return read(off, len, null, dstOff, dst, (int) dstOff);
        }

        @Override
        public int read(long off, byte[] dst, int dstOff, int len) throws IOException {
            if (len <= 0 || off >= size) {
                return 0;
            }
            if (closed) {
                throw new ClosedChannelException();
            }
            return read(off, len, dst, dstOff, null, 0);
        }

        /**
         * Into dstArray or dstSegment at dstOff; inflating into dstSegment goes
         * through segmentBuffer, where dstOff is at bufferOff.
         */
        private int read(long off, int len, byte[] dstArray, long dstOff, MemorySegment dstSegment, int bufferOff)
                throws IOException {
            int want = (int) Math.min(len, size - off);
            long end = off + want;
            int from = (int) (off / chunkSize);
            int to = (int) ((end - 1) / chunkSize);
            if (from == scratchChunk && !whole(from, off, end)) {
                copyFromScratch(off, end, dstArray, dstOff, dstSegment);
                from++;
            } else if (to == scratchChunk && !whole(to, off, end)) {
                copyFromScratch(off, end, dstArray, dstOff, dstSegment);
                to--;
            }
            for (int k = from; k <= to;) {
                // Stage as many chunks as fit, at least one
                int e = k + 1;
                while (e <= to && chunkStarts[e + 1] - chunkStarts[k] <= MAX_STAGING) {
                    e++;
                }
                stage(chunkStarts[k], (int) (chunkStarts[e] - chunkStarts[k]));
                for (int j = k; j < e; j++) {
                    int inOff = (int) (chunkStarts[j] - chunkStarts[k]);
                    if (whole(j, off, end)) {
                        int at = (int) ((long) j * chunkSize - off);
                        if (dstArray != null) {
                            inflate(j, inOff, dstArray, (int) dstOff + at, null, 0);
                        } else {
                            inflate(j, inOff, null, 0, segmentBuffer, bufferOff + at);
                        }
                    } else {
                        if (scratch == null) {
                            scratch = new byte[chunkSize];
                        }
                        // Invalid until the inflate succeeds
                        scratchChunk = -1;
                        inflate(j, inOff, scratch, 0, null, 0);
                        scratchChunk = j;
                        copyFromScratch(off, end, dstArray, dstOff, dstSegment);
                    }
                }
                k = e;
            }
            return want;
        }

        /**
         * Whether [off, end) covers chunk j entirely.
         */
        private boolean whole(int j, long off, long end) {
            long chunkStart = (long) j * chunkSize;
            return off <= chunkStart && Math.min(chunkStart + chunkSize, size) <= end;
        }

        /**
         * The part of the scratch's chunk inside [off, end), to where it goes in dst.
         */
        private void copyFromScratch(long off, long end, byte[] dstArray, long dstOff, MemorySegment dstSegment) {
            long chunkStart = (long) scratchChunk * chunkSize;
            long a = Math.max(off, chunkStart);
            int n = (int) (Math.min(end, chunkStart + chunkSize) - a);
            if (dstArray != null) {
                System.arraycopy(scratch, (int) (a - chunkStart), dstArray, (int) (dstOff + a - off), n);
            } else {
                MemorySegment.copy(scratch, (int) (a - chunkStart), dstSegment, ValueLayout.JAVA_BYTE, dstOff + a - off, n);
            }
        }

        private void stage(long from, int len) throws IOException {
            if (staging == null || staging.length < len) {
                staging = new byte[len];
            }
            long pos = dataStart + from;
            int read = 0;
            while (read < len) {
                int n = archive.read(pos + read, staging, read, len - read);
                if (n <= 0) {
                    throw new IOException("Short read of compressed data at " + (pos + read)
                            + ": expected " + len + " bytes, got " + read);
                }
                read += n;
            }
        }

        /**
         * Inflates chunk j, staged at inOff, into dstArray at dstOff or into
         * dstBuffer at bufferOff. Success is what SOZip's Annex F says it is:
         * the stream ends, every input byte is used, and the output is exactly
         * the chunk.
         */
        private void inflate(int j, int inOff, byte[] dstArray, int dstOff, ByteBuffer dstBuffer, int bufferOff)
                throws IOException {
            int inLen = (int) (chunkStarts[j + 1] - chunkStarts[j]);
            int outLen = (int) Math.min(chunkSize, size - (long) j * chunkSize);
            if (j != lastChunk) {
                // Every chunk but the last ends in the empty stored block of
                // Z_FULL_FLUSH. Marking that block final makes the chunk a
                // complete Deflate stream, so "finished" can be checked.
                int flush = inOff + inLen - FULL_FLUSH.length;
                if (inLen < FULL_FLUSH.length || !endsWithFullFlush(flush)) {
                    throw new IOException("SOZip chunk " + j + " does not end with a full flush");
                }
                staging[flush] = 1;
            }
            inflater.reset();
            inflater.setInput(staging, inOff, inLen);
            if (dstBuffer != null) {
                dstBuffer.limit(bufferOff + outLen).position(bufferOff);
            }
            try {
                int n = 0;
                while (n < outLen) {
                    int remaining = inflater.getRemaining();
                    int r = dstArray != null
                            ? inflater.inflate(dstArray, dstOff + n, outLen - n)
                            : inflater.inflate(dstBuffer);
                    if (r == 0 && (inflater.finished() || inflater.needsInput() || inflater.needsDictionary()
                            || inflater.getRemaining() == remaining)) {
                        // Ended, starved or stuck: the check below reports it
                        break;
                    }
                    n += r;
                }
                // With the output full, the final empty block may still be
                // unread; this consumes it, and catches a chunk that is longer
                // than it should be
                if (n == outLen && !inflater.finished() && inflater.inflate(overrun, 0, 1) != 0) {
                    n++;
                }
                if (n != outLen || !inflater.finished() || inflater.getRemaining() != 0) {
                    throw new IOException("Corrupt SOZip chunk " + j + ": inflated " + n
                            + " of " + outLen + " bytes, " + inflater.getRemaining() + " input bytes left");
                }
            } catch (DataFormatException e) {
                throw new IOException("Corrupt SOZip chunk " + j, e);
            }
        }

        private boolean endsWithFullFlush(int at) {
            for (int i = 0; i < FULL_FLUSH.length; i++) {
                if (staging[at + i] != FULL_FLUSH[i]) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public MemorySegment fetch(long off, int len) {
            return null;
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;
            inflater.end();
            staging = null;
            scratch = null;
            archive.close();
        }

    }

}
