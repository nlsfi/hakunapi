package fi.nls.hakunapi.tiles.source.pmtiles;

public final class TileId {
    private TileId() {
    }

    public static boolean inRange(int z, long x, long y) {
        if (z < 0 || z > 31) {
            return false;
        }
        long dim = 1L << z;
        return x >= 0 && y >= 0 && x < dim && y < dim;
    }

    public static long fromZXY(int z, long x, long y) {
        if (z > 31) {
            throw new IllegalArgumentException("PMTiles zoom out of range: " + z);
        }
        long dim = 1L << z;
        if (x < 0 || y < 0 || x >= dim || y >= dim) {
            throw new IllegalArgumentException("Tile out of range for zoom " + z + ": x=" + x + " y=" + y);
        }

        long acc = (dim * dim - 1) / 3;
        long tx = x;
        long ty = y;
        long d = 0;
        for (long s = dim / 2; s > 0; s /= 2) {
            long rx = (tx & s) > 0 ? 1 : 0;
            long ry = (ty & s) > 0 ? 1 : 0;
            d += s * s * ((3 * rx) ^ ry);

            if (ry == 0) {
                if (rx == 1) {
                    tx = s - 1 - tx;
                    ty = s - 1 - ty;
                }
                long t = tx;
                tx = ty;
                ty = t;
            }
        }
        return acc + d;
    }

    public static long[] toZXY(long tileId) {
        if (tileId < 0) {
            throw new IllegalArgumentException("Negative PMTiles tile id: " + tileId);
        }
        int z = 0;
        long acc = 0;
        while (z <= 31) {
            long dim = 1L << z;
            long levelTiles = dim * dim;
            if (tileId < acc + levelTiles) {
                break;
            }
            acc += levelTiles;
            z++;
        }
        if (z > 31) {
            throw new IllegalArgumentException("PMTiles tile id out of range: " + tileId);
        }

        long d = tileId - acc;
        long dim = 1L << z;
        long tx = 0;
        long ty = 0;
        for (long s = 1; s < dim; s *= 2) {
            long rx = 1 & (d / 2);
            long ry = 1 & (d ^ rx);

            if (ry == 0) {
                if (rx == 1) {
                    tx = s - 1 - tx;
                    ty = s - 1 - ty;
                }
                long t = tx;
                tx = ty;
                ty = t;
            }
            tx += s * rx;
            ty += s * ry;
            d /= 4;
        }
        return new long[] { z, tx, ty };
    }
}
