package fi.nls.hakunapi.tiles.servlet.jakarta;

import fi.nls.hakunapi.core.operation.ApiOperation;
import fi.nls.hakunapi.core.operation.ApiTag;

public class GetDatasetTileOperation implements ApiOperation {
    @Override
    public String getSummary() {
        return "retrieve a tile of the dataset";
    }

    @Override
    public String getDescription() {
        return "Retrieves a single tile of the dataset at the requested tile matrix set, "
                + "tile matrix (zoom level), row and column, carrying one layer per collection. "
                + "The optional collections parameter narrows the tile to the named collections, "
                + "which a tileset serving stored tiles does not support.";
    }

    @Override
    public String get200Description() {
        return "The tile at the requested address.";
    }

    @Override
    public String getOperationId() {
        return "getDatasetTile";
    }

    @Override
    public ApiTag getTag() {
        return ApiTag.Tiles;
    }
}
