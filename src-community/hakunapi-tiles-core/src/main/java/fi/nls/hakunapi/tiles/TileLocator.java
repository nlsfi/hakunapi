package fi.nls.hakunapi.tiles;

/** Where a tile lies in a file, answered from memory; only stores where a tile is one contiguous run can implement this. */
public interface TileLocator {

    long NOT_FOUND = -1;

    /** Bits of the packed long given to the length; the rest hold the offset. */
    int LENGTH_BITS = 24;

    long LENGTH_MASK = (1L << LENGTH_BITS) - 1;

    /** The largest offset a packed location can carry: a 1 TB file. */
    long MAX_OFFSET = (1L << 40) - 1;

    /** Packed offset and length, offset absolute within the file, or NOT_FOUND. */
    long find(int z, long x, long y);

    String getMediaType();

    /** HTTP Content-Encoding of all stored tiles, or null when uncompressed. */
    String getContentEncoding();

    static long offset(long packed) {
        return packed >>> LENGTH_BITS;
    }

    static int length(long packed) {
        return (int) (packed & LENGTH_MASK);
    }

    /** Checked rather than masked: a truncated offset would serve the wrong bytes. */
    static long pack(long offset, long length) {
        if (offset > MAX_OFFSET) {
            throw new IllegalArgumentException("Tile offset " + offset + " exceeds " + MAX_OFFSET);
        }
        if (length > LENGTH_MASK) {
            throw new IllegalArgumentException("Tile length " + length + " exceeds " + LENGTH_MASK);
        }
        return (offset << LENGTH_BITS) | length;
    }

}
