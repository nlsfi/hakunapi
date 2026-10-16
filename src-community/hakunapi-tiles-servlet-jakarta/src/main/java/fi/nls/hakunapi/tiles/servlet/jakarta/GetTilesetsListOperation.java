package fi.nls.hakunapi.tiles.servlet.jakarta;

import fi.nls.hakunapi.core.operation.ApiOperation;
import fi.nls.hakunapi.core.operation.ApiTag;

public class GetTilesetsListOperation implements ApiOperation {
    @Override
    public String getSummary() {
        return "retrieve the list of tilesets for a collection";
    }

    @Override
    public String getDescription() {
        return "Lists the tilesets (one per tile matrix set) available for the collection.";
    }

    @Override
    public String get200Description() {
        return "The list of tilesets available for the collection.";
    }

    @Override
    public String getOperationId() {
        return "getCollectionTilesetsList";
    }

    @Override
    public ApiTag getTag() {
        return ApiTag.Tiles;
    }
}
