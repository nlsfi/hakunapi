package fi.nls.hakunapi.core.tiles;

import fi.nls.hakunapi.core.operation.ApiOperation;
import fi.nls.hakunapi.core.operation.ApiTag;

public class GetTilingSchemeOperation implements ApiOperation {

    @Override
    public String getSummary() {
        return "Retrieve a tiling scheme by id";
    }

    @Override
    public String getDescription() {
        return "Retrieve a tiling scheme by id";
    }

    @Override
    public String getOperationId() {
        return "getTilingScheme";
    }

    @Override
    public String get200Description() {
        return "A tiling scheme";
    }

    @Override
    public ApiTag getTag() {
        return ApiTag.Tiles;
    }

}
