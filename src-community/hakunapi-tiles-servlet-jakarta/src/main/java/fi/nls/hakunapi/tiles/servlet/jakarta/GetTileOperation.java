package fi.nls.hakunapi.tiles.servlet.jakarta;

import fi.nls.hakunapi.core.operation.ApiOperation;
import fi.nls.hakunapi.core.operation.ApiTag;

public class GetTileOperation implements ApiOperation {
    @Override
    public String getSummary() {
        return "retrieve a tile of a collection";
    }

    @Override
    public String getDescription() {
        return "Retrieves a single tile of the collection at the requested tile matrix set, "
                + "tile matrix (zoom level), row and column.";
    }

    @Override
    public String get200Description() {
        return "The tile at the requested address.";
    }

    @Override
    public String getOperationId() {
        return "getCollectionTile";
    }

    @Override
    public ApiTag getTag() {
        return ApiTag.Tiles;
    }
}
