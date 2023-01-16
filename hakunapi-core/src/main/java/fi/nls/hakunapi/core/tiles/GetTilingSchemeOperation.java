package fi.nls.hakunapi.core.tiles;

import fi.nls.hakunapi.core.operation.WFS3Operation;
import fi.nls.hakunapi.core.operation.WFS3Tag;

public class GetTilingSchemeOperation implements WFS3Operation {

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
    public WFS3Tag getTag() {
        return WFS3Tag.Tiles;
    }

}
