package fi.nls.hakunapi.tiles.servlet.jakarta;

import fi.nls.hakunapi.core.operation.ApiOperation;
import fi.nls.hakunapi.core.operation.ApiTag;

public class GetMapTileOperation implements ApiOperation {
    @Override
    public String getSummary() {
        return "retrieve a map tile of a collection";
    }

    @Override
    public String getDescription() {
        return "Retrieves a single map (raster) tile of the collection at the requested tile matrix set, "
                + "tile matrix (zoom level), row and column.";
    }

    @Override
    public String get200Description() {
        return "The map tile at the requested address.";
    }

    @Override
    public String getOperationId() {
        return "getCollectionMapTile";
    }

    @Override
    public ApiTag getTag() {
        return ApiTag.Tiles;
    }
}
