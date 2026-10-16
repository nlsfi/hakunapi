package fi.nls.hakunapi.tiles.servlet.jakarta;

import fi.nls.hakunapi.core.operation.ApiOperation;
import fi.nls.hakunapi.core.operation.ApiTag;

public class GetTileMatrixSetsOperation implements ApiOperation {
    @Override
    public String getSummary() {
        return "retrieve the list of available tile matrix sets";
    }

    @Override
    public String getDescription() {
        return "Lists the tile matrix sets (tiling schemes) supported by this API.";
    }

    @Override
    public String get200Description() {
        return "The list of supported tile matrix sets.";
    }

    @Override
    public String getOperationId() {
        return "getTileMatrixSets";
    }

    @Override
    public ApiTag getTag() {
        return ApiTag.Tiles;
    }
}
