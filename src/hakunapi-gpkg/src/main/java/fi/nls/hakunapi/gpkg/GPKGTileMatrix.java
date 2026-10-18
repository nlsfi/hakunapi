package fi.nls.hakunapi.gpkg;

/** pixelXSize and pixelYSize are ground units per pixel. */
public class GPKGTileMatrix {

    public static final String GPKG_TABLE_TILE_MATRIX = "gpkg_tile_matrix";

    private final String tableName;
    private final int zoomLevel;
    private final int matrixWidth;
    private final int matrixHeight;
    private final int tileWidth;
    private final int tileHeight;
    private final double pixelXSize;
    private final double pixelYSize;

    public GPKGTileMatrix(String tableName, int zoomLevel, int matrixWidth, int matrixHeight,
            int tileWidth, int tileHeight, double pixelXSize, double pixelYSize) {
        this.tableName = tableName;
        this.zoomLevel = zoomLevel;
        this.matrixWidth = matrixWidth;
        this.matrixHeight = matrixHeight;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.pixelXSize = pixelXSize;
        this.pixelYSize = pixelYSize;
    }

    public String getTableName() {
        return tableName;
    }

    public int getZoomLevel() {
        return zoomLevel;
    }

    public int getMatrixWidth() {
        return matrixWidth;
    }

    public int getMatrixHeight() {
        return matrixHeight;
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    public double getPixelXSize() {
        return pixelXSize;
    }

    public double getPixelYSize() {
        return pixelYSize;
    }

}
