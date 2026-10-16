package fi.nls.hakunapi.tiles.source.pmtiles;

import java.nio.file.Path;
import java.util.Set;

import fi.nls.hakunapi.tiles.TileLocator;

public record PMTilesConfig(
        PMTilesArchive archive,
        String mediaType,
        Set<String> tmsIds,
        boolean sendfile,
        TileLocator index,
        Path archiveFile) {
    public Path sendfileArchive() {
        return sendfile ? archiveFile : null;
    }
}
