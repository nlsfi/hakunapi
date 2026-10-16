package fi.nls.hakunapi.tiles;

import java.io.OutputStream;

@FunctionalInterface
public interface TileBody {

    void writeTo(OutputStream out) throws Exception;

}
