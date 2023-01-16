package fi.nls.hakunapi.core.tiles;

import fi.nls.hakunapi.core.operation.WFS3Operation;
import fi.nls.hakunapi.core.operation.WFS3Tag;

public class GetTilingSchemesOperation implements WFS3Operation {

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
    public WFS3Tag getTag() {
        return WFS3Tag.Tiles;
    }

}
