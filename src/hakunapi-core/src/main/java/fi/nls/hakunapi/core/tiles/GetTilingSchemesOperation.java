package fi.nls.hakunapi.core.tiles;

import fi.nls.hakunapi.core.operation.ApiOperation;
import fi.nls.hakunapi.core.operation.ApiTag;

public class GetTilingSchemesOperation implements ApiOperation {

    @Override
    public String getSummary() {
        return "Retrieve all available tiling schemes";
    }

    @Override
    public String getDescription() {
        return "Retrieve all available tiling schemes";
    }

    @Override
    public String getOperationId() {
        return "getTilingSchemes";
    }

    @Override
    public String get200Description() {
        return "A list of tiling schemes";
    }

    @Override
    public ApiTag getTag() {
        return ApiTag.Tiles;
    }

}
