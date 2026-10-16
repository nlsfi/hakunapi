package fi.nls.hakunapi.tiles.source.pmtiles;

import fi.nls.hakunapi.tiles.TileLocator;

public class BoxedTileIndex implements TileLocator {
    private static final long EMPTY = 0L;

    public static final double MIN_DENSITY = 0.5;

    private final Zoom[] zooms;

    private final long tileDataOffset;
    private final byte tileCompression;
    private final String mediaType;

    private static final class Zoom {
        final long xMin;
        final long yMin;
        final long width;
        final long height;
        final long[] locations;

        Zoom(long xMin, long yMin, long width, long height) {
            this.xMin = xMin;
            this.yMin = yMin;
            this.width = width;
            this.height = height;
            this.locations = new long[Math.toIntExact(width * height)];
        }
    }

    private BoxedTileIndex(Zoom[] zooms, long tileDataOffset, byte tileCompression,
            String mediaType) {
        this.zooms = zooms;
        this.tileDataOffset = tileDataOffset;
        this.tileCompression = tileCompression;
        this.mediaType = mediaType;
    }

    public static BoxedTileIndex of(TileIndex sorted) {
        long[] xMin = new long[32];
        long[] xMax = new long[32];
        long[] yMin = new long[32];
        long[] yMax = new long[32];
        long[] counts = new long[32];
        java.util.Arrays.fill(xMin, Long.MAX_VALUE);
        java.util.Arrays.fill(yMin, Long.MAX_VALUE);
        java.util.Arrays.fill(xMax, Long.MIN_VALUE);
        java.util.Arrays.fill(yMax, Long.MIN_VALUE);

        sorted.forEachEntry((tileId, packed) -> {
            long[] zxy = TileId.toZXY(tileId);
            int z = (int) zxy[0];
            long x = zxy[1];
            long y = zxy[2];
            counts[z]++;
            if (x < xMin[z]) xMin[z] = x;
            if (x > xMax[z]) xMax[z] = x;
            if (y < yMin[z]) yMin[z] = y;
            if (y > yMax[z]) yMax[z] = y;
        });

        Zoom[] zooms = new Zoom[32];
        for (int z = 0; z < 32; z++) {
            if (counts[z] == 0) {
                continue;
            }
            long w = xMax[z] - xMin[z] + 1;
            long h = yMax[z] - yMin[z] + 1;
            long slots = w * h;

            if (slots > Integer.MAX_VALUE || (double) counts[z] / slots < MIN_DENSITY) {
                return null;
            }
            zooms[z] = new Zoom(xMin[z], yMin[z], w, h);
        }

        sorted.forEachEntry((tileId, packed) -> {
            long[] zxy = TileId.toZXY(tileId);
            Zoom zoom = zooms[(int) zxy[0]];
            zoom.locations[slot(zoom, zxy[1], zxy[2])] = packed;
        });

        return new BoxedTileIndex(zooms, sorted.tileDataOffset(), sorted.tileCompression(),
                sorted.getMediaType());
    }

    private static int slot(Zoom zoom, long x, long y) {
        return Math.toIntExact((y - zoom.yMin) * zoom.width + (x - zoom.xMin));
    }

    @Override
    public long find(int z, long x, long y) {
        if (z < 0 || z >= zooms.length) {
            return NOT_FOUND;
        }
        Zoom zoom = zooms[z];
        if (zoom == null) {
            return NOT_FOUND;
        }
        long dx = x - zoom.xMin;
        long dy = y - zoom.yMin;

        if (Long.compareUnsigned(dx, zoom.width) >= 0
                || Long.compareUnsigned(dy, zoom.height) >= 0) {
            return NOT_FOUND;
        }
        long packed = zoom.locations[(int) (dy * zoom.width + dx)];
        if (packed == EMPTY) {
            return NOT_FOUND;
        }
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

    public long heapBytes() {
        long bytes = 0;
        for (Zoom zoom : zooms) {
            if (zoom != null) {
                bytes += (long) zoom.locations.length * Long.BYTES;
            }
        }
        return bytes;
    }

    public String describe() {
        StringBuilder sb = new StringBuilder();
        for (int z = 0; z < zooms.length; z++) {
            if (zooms[z] == null) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append('z').append(z).append(' ')
              .append(zooms[z].width).append('x').append(zooms[z].height);
        }
        return sb.toString();
    }
}
