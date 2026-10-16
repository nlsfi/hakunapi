package fi.nls.hakunapi.tiles.servlet.jakarta;

import fi.nls.hakunapi.core.operation.ApiOperation;
import fi.nls.hakunapi.core.operation.ApiTag;

public class GetDatasetTilesetsListOperation implements ApiOperation {
    @Override
    public String getSummary() {
        return "retrieve the list of dataset tilesets";
    }

    @Override
    public String getDescription() {
        return "Lists the tilesets of the dataset as a whole. A dataset tile carries one "
                + "layer per collection, unlike a geodata tileset of a single collection.";
    }

    @Override
    public String get200Description() {
        return "The list of dataset tilesets.";
    }

    @Override
    public String getOperationId() {
        return "getDatasetTilesetsList";
    }

    @Override
    public ApiTag getTag() {
        return ApiTag.Tiles;
    }
}
