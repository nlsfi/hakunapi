package fi.nls.hakunapi.core.tiles;

import fi.nls.hakunapi.core.operation.WFS3Operation;
import fi.nls.hakunapi.core.operation.WFS3Tag;

public class GetTileOperation implements WFS3Operation {

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
    public WFS3Tag getTag() {
        return WFS3Tag.Tiles;
    }

}
