package fi.nls.hakunapi.tiles;

import java.nio.file.Path;

/** A layer whose tiles lie as contiguous runs in a file and can be sent as a byte range without being read into the heap. */
public interface AcceleratedTileLayer {

    /** Where the tiles lie, in memory; null when the layer was not configured for acceleration. */
    TileLocator getTileLocator();

    /** The internal location the front end serves the tile file at, e.g. /_tiles/mtk; null when the container sends the tiles itself. */
    String getInternalLocation();

    /** The file the tiles lie in, which the front end must be able to read. */
    Path getTileFile();

}
