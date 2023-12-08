package fi.nls.hakunapi.gpkg;

import org.locationtech.jts.geom.Envelope;

public class GPKGFeaturesTable {

    public static final String DATA_TYPE = "features";

    private final String tableName;
    private final String identifier;
    private final String description;
    private final double minX;
    private final double minY;
    private final double maxX;
    private final double maxY;
    private final int srid;

    public GPKGFeaturesTable(String tableName, String identifier, String description, Envelope envelope, int srid) {
        this.tableName = tableName;
        this.identifier = identifier;
        this.description = description;
        this.minX = envelope.getMinX();
        this.minY = envelope.getMinY();
        this.maxX = envelope.getMaxX();
        this.maxY = envelope.getMaxY();
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

    public int getSrid() {
        return srid;
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

}
