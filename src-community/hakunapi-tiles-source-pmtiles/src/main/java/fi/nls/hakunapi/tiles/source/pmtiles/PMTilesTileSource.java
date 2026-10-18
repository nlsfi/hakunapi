package fi.nls.hakunapi.tiles.source.pmtiles;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.nls.hakunapi.core.config.HakunaConfigParser;
import fi.nls.hakunapi.tiles.BytesTile;
import fi.nls.hakunapi.tiles.Tile;
import fi.nls.hakunapi.tiles.MapTileLayer;
import fi.nls.hakunapi.tiles.TileMatrixSetRegistry;
import fi.nls.hakunapi.tiles.MapTileSource;
import fi.nls.hakunapi.tiles.config.TilesConfigParser;

public class PMTilesTileSource implements MapTileSource {
    private static final Logger LOG = LoggerFactory.getLogger(PMTilesTileSource.class);

    public static final String TYPE = "pmtiles";

    static final String MVT = "application/vnd.mapbox-vector-tile";

    private final List<PMTilesArchive> archives = new ArrayList<>();

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public MapTileLayer parse(HakunaConfigParser cfg, Path path, TileMatrixSetRegistry tms, String layerId)
            throws Exception {
        Parsed parsed = parse(cfg, path, layerId, archives, false);
        return new PMTilesTileLayer(layerId, parsed.title(), parsed.description(), this,
                parsed.tmsIds(), List.of(parsed.config().mediaType()), null, parsed.config());
    }

    record Parsed(String title, String description, List<String> tmsIds, PMTilesConfig config) {}

    static Parsed parse(HakunaConfigParser cfg, Path path, String layerId,
            List<PMTilesArchive> archives, boolean vector) throws Exception {
        TilesConfigParser tiles = new TilesConfigParser(cfg);
        String p = "tiles.layers." + layerId + ".";
        String pm = p + "pmtiles.";

        String title = cfg.get(p + "title", layerId);
        String description = cfg.get(p + "description", title);
        List<String> tmsIds = tiles.readTileMatrixSetIds(layerId);

        String filePath = cfg.get(pm + "path");
        String url = cfg.get(pm + "url");
        if ((filePath == null) == (url == null)) {
            throw new IllegalArgumentException(
                    "tile layer " + layerId + ": exactly one of pmtiles.path or pmtiles.url is required");
        }

        RangeReader reader;
        String location;
        if (filePath != null) {
            Path archivePath = path != null ? path.resolveSibling(filePath) : Path.of(filePath);
            reader = new FileRangeReader(archivePath);
            location = archivePath.toString();
        } else {
            reader = new HttpRangeReader(URI.create(url));
            location = url;
        }

        PMTilesArchive archive;
        try {
            archive = PMTilesArchive.open(reader);
        } catch (Exception e) {
            reader.close();
            throw e;
        }
        String mediaType = cfg.get(pm + "format", archive.getHeader().tileMediaType());

        boolean mvt = MVT.equals(archive.getHeader().tileMediaType());
        if (mvt != vector) {
            archive.close();
            throw new IllegalArgumentException("tile layer " + layerId + ": " + location
                    + " holds " + archive.getHeader().tileMediaType() + ", so its type is "
                    + (mvt ? PMTilesVectorTileSource.TYPE : TYPE) + ", not "
                    + (vector ? PMTilesVectorTileSource.TYPE : TYPE));
        }
        archives.add(archive);

        boolean sendfile = Boolean.parseBoolean(cfg.get(pm + "sendfile", "false"));
        if (sendfile && filePath == null) {
            throw new IllegalArgumentException("tile layer " + layerId
                    + ": pmtiles.sendfile needs pmtiles.path; the container sends a range of a"
                    + " file on disk, and a remote archive is not one");
        }
        if (!vector && sendfile) {
            throw new IllegalArgumentException("tile layer " + layerId
                    + ": pmtiles.sendfile needs type " + PMTilesVectorTileSource.TYPE);
        }
        fi.nls.hakunapi.tiles.TileLocator index = null;
        if (sendfile) {
            long started = System.currentTimeMillis();
            TileIndex sorted = TileIndex.build(archive);
            BoxedTileIndex boxed = BoxedTileIndex.of(sorted);
            index = boxed != null ? boxed : sorted;
            LOG.info("pmtiles tile layer {} indexed ({}) for sendfile -> {} tiles, {} kB{}, in {} ms",
                    layerId, boxed != null ? "per-zoom arrays" : "sorted",
                    sorted.size(),
                    (boxed != null ? boxed.heapBytes() : sorted.heapBytes()) / 1024,
                    boxed != null ? " [" + boxed.describe() + "]" : "",
                    System.currentTimeMillis() - started);
        }

        PMTilesConfig config = new PMTilesConfig(archive, mediaType, java.util.Set.copyOf(tmsIds),
                sendfile, index, filePath != null ? Path.of(location) : null);
        LOG.info("pmtiles tile layer {} -> {} type={}", layerId, location, mediaType);
        return new Parsed(title, description, tmsIds, config);
    }

    @Override
    public Optional<Tile> getTile(MapTileLayer layer, String tileMatrixSetId, String tileMatrix,
            long tileRow, long tileCol, String mediaType, Map<String, String> requestParams) throws Exception {
        return readTile(((PMTilesTileLayer) layer).getPMTilesConfig(), tileMatrixSetId, tileMatrix,
                tileRow, tileCol);
    }

    static Optional<Tile> readTile(PMTilesConfig cfg, String tileMatrixSetId, String tileMatrix,
            long tileRow, long tileCol) throws Exception {
        if (!cfg.tmsIds().contains(tileMatrixSetId)) {
            return Optional.empty();
        }

        int z;
        try {
            z = Integer.parseInt(tileMatrix);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
        if (!TileId.inRange(z, tileCol, tileRow)) {
            return Optional.empty();
        }

        Optional<byte[]> bytes = cfg.archive().getTile(z, tileCol, tileRow);
        if (bytes.isEmpty()) {
            return Optional.empty();
        }
        String contentEncoding = Compression.contentEncoding(cfg.archive().getHeader().tileCompression());
        return Optional.of(new BytesTile(tileMatrixSetId, tileMatrix, tileRow, tileCol,
                cfg.mediaType(), contentEncoding, bytes.get()));
    }

    @Override
    public void close() throws Exception {
        closeAll(archives);
    }

    static void closeAll(List<PMTilesArchive> archives) throws Exception {
        Exception first = null;
        for (PMTilesArchive a : archives) {
            try {
                a.close();
            } catch (Exception e) {
                if (first == null) {
                    first = e;
                }
            }
        }
        archives.clear();
        if (first != null) {
            throw first;
        }
    }
}
