package fi.nls.hakunapi.core.tiles;

import fi.nls.hakunapi.core.operation.ApiOperation;
import fi.nls.hakunapi.core.operation.ApiTag;

public class GetTileOperation implements ApiOperation {

    @Override
    public String getSummary() {
        return "Retrieve a tile of the dataset";
    }

    @Override
    public String getDescription() {
        return "The tile in the requested tiling scheme, on the requested zoom level in the tiling scheme, with the requested grid coordinates (row, column) is returned. Each collection of the dataset is returned as a separate layer. The collections and the feature properties to include in the tile representation can be limited using query parameters";
    }

    @Override
    public String getOperationId() {
        return "getTilesDataset";
    }

    @Override
    public String get200Description() {
        return "A tile of the dataset";
    }

    @Override
    public ApiTag getTag() {
        return ApiTag.Tiles;
    }

}
