package fi.nls.hakunapi.gpkg.extension;

import java.sql.Connection;
import java.sql.SQLException;

import fi.nls.hakunapi.gpkg.GPKG;

public class MvtTilesExtension extends GPKGExtension {

    public static final String EXTENSION_NAME = "hakunapi_mvt";
    public static final String COLUMN_NAME = "tile_data";
    public static final String SCOPE = "read-write";
    public static final String DEFINITION = "tile_data holds Mapbox Vector Tiles, not raster images";

    public MvtTilesExtension(String tableName) {
        super(tableName, COLUMN_NAME, EXTENSION_NAME, DEFINITION, SCOPE);
    }

    @Override
    public void createExtension(Connection c) throws SQLException {
        GPKG.insertExtension(c, this);
    }

}
