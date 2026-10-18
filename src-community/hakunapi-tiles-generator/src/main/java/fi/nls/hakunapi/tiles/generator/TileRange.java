package fi.nls.hakunapi.tiles.generator;

import fi.nls.hakunapi.tiles.TileMatrix;

/** Row and column bounds are inclusive. */
public record TileRange(int zoom, long minRow, long maxRow, long minCol, long maxCol) {

    /** Far edges are half-open: a boundary on a tile edge does not pull in the next tile. */
    public static TileRange of(TileMatrix tm, int zoom, double minX, double minY, double maxX, double maxY) {
        double spanX = tm.getTileSpanX();
        double spanY = tm.getTileSpanY();
        double originX = tm.getPointOfOriginX();
        double originY = tm.getPointOfOriginY();

        long minCol = (long) Math.floor((minX - originX) / spanX);
        long maxCol = (long) Math.ceil((maxX - originX) / spanX) - 1;
        // Rows grow downward from the origin, so the extent's maxY gives the first row.
        long minRow = (long) Math.floor((originY - maxY) / spanY);
        long maxRow = (long) Math.ceil((originY - minY) / spanY) - 1;

        minCol = Math.max(0, minCol);
        minRow = Math.max(0, minRow);
        maxCol = Math.min(tm.getMatrixWidth() - 1L, maxCol);
        maxRow = Math.min(tm.getMatrixHeight() - 1L, maxRow);

        return new TileRange(zoom, minRow, maxRow, minCol, maxCol);
    }

    public boolean isEmpty() {
        return minRow > maxRow || minCol > maxCol;
    }

    public long size() {
        if (isEmpty()) {
            return 0;
        }
        return (maxRow - minRow + 1) * (maxCol - minCol + 1);
    }

}
