package fi.nls.hakunapi.tiles.servlet.jakarta;

import fi.nls.hakunapi.core.operation.ApiOperation;
import fi.nls.hakunapi.core.operation.ApiTag;

public class GetMapTilesetsListOperation implements ApiOperation {
    @Override
    public String getSummary() {
        return "retrieve the list of map tilesets for a collection";
    }

    @Override
    public String getDescription() {
        return "Lists the map (raster) tilesets (one per tile matrix set) available for the collection.";
    }

    @Override
    public String get200Description() {
        return "The list of map tilesets available for the collection.";
    }

    @Override
    public String getOperationId() {
        return "getCollectionMapTilesetsList";
    }

    @Override
    public ApiTag getTag() {
        return ApiTag.Tiles;
    }
}
