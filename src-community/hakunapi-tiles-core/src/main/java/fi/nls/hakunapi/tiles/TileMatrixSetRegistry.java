package fi.nls.hakunapi.tiles;

import java.util.Collection;
import java.util.Optional;

public interface TileMatrixSetRegistry {

    Collection<TileMatrixSet> list();

    Optional<TileMatrixSet> get(String tileMatrixSetId);

}
