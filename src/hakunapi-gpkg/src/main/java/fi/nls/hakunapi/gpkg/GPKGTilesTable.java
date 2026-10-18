package fi.nls.hakunapi.gpkg;

/** The extent is the tiling scheme's, not that of the tiles written. */
public class GPKGTilesTable {

    public static final String DATA_TYPE = "tiles";

    private final String tableName;
    private final String identifier;
    private final String description;
    private final double minX;
    private final double minY;
    private final double maxX;
    private final double maxY;
    private final int srid;

    public GPKGTilesTable(String tableName, String identifier, String description,
            double minX, double minY, double maxX, double maxY, int srid) {
        this.tableName = tableName;
        this.identifier = identifier;
        this.description = description;
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
        this.srid = srid;
    }

    public String getTableName() {
        return tableName;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getDescription() {
        return description;
    }

    public double getMinX() {
        return minX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMaxY() {
        return maxY;
    }

    public int getSrid() {
        return srid;
    }

}
