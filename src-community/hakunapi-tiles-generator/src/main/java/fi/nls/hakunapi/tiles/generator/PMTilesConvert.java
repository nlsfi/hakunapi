package fi.nls.hakunapi.tiles.generator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.nls.hakunapi.gpkg.GPKG;
import fi.nls.hakunapi.tiles.mvt.Protobuf;
import fi.nls.hakunapi.tiles.source.pmtiles.Compression;
import fi.nls.hakunapi.tiles.source.pmtiles.PMTilesWriter;
import fi.nls.hakunapi.tiles.source.pmtiles.TileId;

public class PMTilesConvert {

    private static final Logger LOG = LoggerFactory.getLogger(PMTilesConvert.class);

    private static final byte TILE_TYPE_MVT = 1;
    private static final int LAYER_SAMPLE_PER_ZOOM = 8;
    private static final int TILE_LAYERS = 3;
    private static final int LAYER_NAME = 1;

    public static void main(String[] args) {
        try {
            System.exit(run(args));
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            LOG.debug("Conversion failed", e);
            System.exit(1);
        }
    }

    private static int run(String[] args) throws Exception {
        Args parsed = Args.parse(args);
        if (parsed == null) {
            usage();
            return 2;
        }
        if (!Files.exists(parsed.pyramidPath)) {
            throw new IllegalArgumentException("No such pyramid: " + parsed.pyramidPath);
        }

        long started = System.currentTimeMillis();
        long written;
        try (Connection c = open(parsed.pyramidPath)) {
            checkSquareMatrices(c, parsed.table);
            List<long[]> addresses = readAddresses(c, parsed.table, parsed.flip);
            if (addresses.isEmpty()) {
                throw new IllegalArgumentException(
                        "Table " + parsed.table + " holds no tiles in " + parsed.pyramidPath);
            }
            LOG.info("Converting {} tiles from {} table {}", addresses.size(),
                    parsed.pyramidPath, parsed.table);
            if (parsed.centerZoom < 0) {
                parsed.centerZoom = (int) addresses.get(0)[1];
            }
            written = convert(c, parsed, addresses);
        }
        long seconds = (System.currentTimeMillis() - started) / 1000;
        System.out.printf("Wrote %s (%,d bytes) in %d s%n",
                parsed.outputPath, written, seconds);
        return 0;
    }

    /** The PMTiles tile id is a Hilbert index over a 2^z square grid. */
    private static void checkSquareMatrices(Connection c, String table) throws SQLException {
        String sql = "SELECT m.zoom_level, m.matrix_width, m.matrix_height FROM gpkg_tile_matrix m"
                + " WHERE m.table_name = ? AND EXISTS (SELECT 1 FROM " + GPKG.quote(table)
                + " t WHERE t.zoom_level = m.zoom_level)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, table);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int zoom = rs.getInt(1);
                    long width = rs.getLong(2);
                    long height = rs.getLong(3);
                    long dim = 1L << zoom;
                    if (width != dim || height != dim) {
                        throw new IllegalArgumentException("Zoom " + zoom + " of table " + table
                                + " has a " + width + "x" + height + " tile matrix, but PMTiles needs "
                                + dim + "x" + dim + " (2^zoom)");
                    }
                }
            }
        }
    }

    /** Sorted by Hilbert tile id, which the PMTiles writer requires and SQLite cannot order by. */
    private static List<long[]> readAddresses(Connection c, String table, boolean flip)
            throws SQLException {
        List<long[]> addresses = new ArrayList<>();
        String sql = "SELECT zoom_level, tile_column, tile_row FROM " + GPKG.quote(table);
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int zoom = rs.getInt(1);
                long col = rs.getLong(2);
                long row = rs.getLong(3);
                long y = flip ? (1L << zoom) - 1 - row : row;
                addresses.add(new long[] { TileId.fromZXY(zoom, col, y), zoom, col, row });
            }
        }
        addresses.sort((a, b) -> Long.compare(a[0], b[0]));
        return addresses;
    }

    private static long convert(Connection c, Args parsed, List<long[]> addresses) throws Exception {
        String sql = "SELECT tile_data FROM " + GPKG.quote(parsed.table)
                + " WHERE zoom_level = ? AND tile_column = ? AND tile_row = ?";
        try (PMTilesWriter writer = new PMTilesWriter(parsed.outputPath,
                        parsed.tileCompression, TILE_TYPE_MVT);
                PreparedStatement ps = c.prepareStatement(sql)) {
            Set<String> layers = new LinkedHashSet<>();
            int[] sampled = new int[32];
            for (int i = 0; i < addresses.size(); i++) {
                long[] a = addresses.get(i);
                int zoom = (int) a[1];
                long col = a[2];
                long row = a[3];
                ps.setInt(1, zoom);
                ps.setLong(2, col);
                ps.setLong(3, row);
                byte[] tile;
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalStateException("Tile vanished: " + zoom + "/" + row + "/" + col);
                    }
                    tile = rs.getBytes(1);
                }
                long y = parsed.flip ? (1L << zoom) - 1 - row : row;
                if (tile != null && sampled[zoom]++ < LAYER_SAMPLE_PER_ZOOM) {
                    collectLayerNames(parsed.tileCompression == Compression.GZIP ? gunzip(tile) : tile, layers);
                }
                writer.add(zoom, col, y, tile);
                if ((i + 1) % 100_000 == 0) {
                    LOG.info("{} / {} tiles", i + 1, addresses.size());
                }
            }
            return writer.finish(metadata(parsed.table, layers), parsed.bboxE7, parsed.centerE7, parsed.centerZoom);
        }
    }

    private static byte[] gunzip(byte[] bytes) throws IOException {
        try (GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
            return in.readAllBytes();
        }
    }

    private static void collectLayerNames(byte[] tile, Set<String> names) {
        int pos = 0;
        while (pos < tile.length) {
            long t = Protobuf.varint(tile, pos);
            int tag = Protobuf.value(t);
            int afterTag = Protobuf.end(t);
            int wire = Protobuf.wire(tag);
            if (Protobuf.field(tag) == TILE_LAYERS && wire == Protobuf.WIRE_LEN) {
                long l = Protobuf.varint(tile, afterTag);
                int body = Protobuf.end(l);
                int end = body + Protobuf.value(l);
                for (int p = body; p < end;) {
                    long lt = Protobuf.varint(tile, p);
                    int ltag = Protobuf.value(lt);
                    int after = Protobuf.end(lt);
                    int lwire = Protobuf.wire(ltag);
                    if (Protobuf.field(ltag) == LAYER_NAME && lwire == Protobuf.WIRE_LEN) {
                        long nl = Protobuf.varint(tile, after);
                        names.add(new String(tile, Protobuf.end(nl), Protobuf.value(nl), StandardCharsets.UTF_8));
                        break;
                    }
                    p = Protobuf.skip(tile, after, lwire);
                }
                pos = end;
            } else {
                pos = Protobuf.skip(tile, afterTag, wire);
            }
        }
    }

    private static String jsonString(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '"' || ch == '\\') {
                sb.append('\\').append(ch);
            } else if (ch < 0x20) {
                sb.append(String.format("\\u%04x", (int) ch));
            } else {
                sb.append(ch);
            }
        }
        return sb.append('"').toString();
    }

    /** Spec: MVT archives MUST carry vector_layers. Third-party viewers assume WebMercatorQuad; the note names the actual tiling scheme. */
    private static String metadata(String table, Set<String> layers) {
        if (layers.isEmpty()) {
            layers.add(table);
        }
        StringBuilder vectorLayers = new StringBuilder("[");
        for (String layer : layers) {
            vectorLayers.append(vectorLayers.length() > 1 ? "," : "")
                    .append("{\"id\":").append(jsonString(layer)).append('}');
        }
        vectorLayers.append(']');
        return "{\"name\":" + jsonString(table) + ","
                + "\"format\":\"pbf\","
                + "\"vector_layers\":" + vectorLayers + ","
                + "\"generator\":\"hakunapi-tiles-generator\","
                + "\"hakunapi:note\":\"Tiles are addressed on the hakunapi tile matrix set they "
                + "were generated for, which need not be WebMercatorQuad. The PMTiles tile id is a "
                + "Hilbert index over a square 2^z grid and carries no projection; a viewer that "
                + "assumes WebMercatorQuad will place these tiles incorrectly.\"}";
    }

    private static Connection open(Path path) throws Exception {
        return DriverManager.getConnection("jdbc:sqlite:" + path.toAbsolutePath() + "?open_mode=1");
    }

    private static void usage() {
        System.err.println("Usage: PMTilesConvert [options] <pyramid.gpkg> <table> <output.pmtiles>");
        System.err.println();
        System.err.println("  --raw            the pyramid's tiles are uncompressed");
        System.err.println("                   (default: gzip, which is what the generator stores)");
        System.err.println("  --flip           the pyramid counts tile_row from the bottom (TMS)");
        System.err.println("  --bbox <minLon,minLat,maxLon,maxLat>");
        System.err.println("                   WGS84 extent recorded in the header (default: Finland, 19,59.4,31.8,70.1)");
        System.err.println("  --center-zoom <n>  zoom a viewer should open at (default: the lowest zoom in the pyramid)");
    }

    private static class Args {

        Path pyramidPath;
        String table;
        Path outputPath;
        byte tileCompression = Compression.GZIP;
        boolean flip;
        int[] bboxE7 = { 190000000, 594000000, 318000000, 701000000 };
        int[] centerE7 = { 250000000, 645000000 };
        int centerZoom = -1;

        static Args parse(String[] args) {
            Args a = new Args();
            List<String> positional = new ArrayList<>(3);
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (("--bbox".equals(arg) || "--center-zoom".equals(arg)) && i + 1 >= args.length) {
                    System.err.println("Option " + arg + " needs a value");
                    return null;
                }
                switch (arg) {
                case "--raw":
                    a.tileCompression = Compression.NONE;
                    break;
                case "--flip":
                    a.flip = true;
                    break;
                case "--bbox":
                    a.bboxE7 = parseBboxE7(args[++i]);
                    a.centerE7 = new int[] {
                            (int) (((long) a.bboxE7[0] + a.bboxE7[2]) / 2),
                            (int) (((long) a.bboxE7[1] + a.bboxE7[3]) / 2) };
                    break;
                case "--center-zoom":
                    a.centerZoom = Integer.parseInt(args[++i]);
                    break;
                default:
                    if (arg.startsWith("--")) {
                        System.err.println("Unknown option: " + arg);
                        return null;
                    }
                    positional.add(arg);
                }
            }
            if (positional.size() != 3) {
                return null;
            }
            a.pyramidPath = Paths.get(positional.get(0)).toAbsolutePath();
            a.table = positional.get(1);
            a.outputPath = Paths.get(positional.get(2)).toAbsolutePath();
            return a;
        }

        private static int[] parseBboxE7(String csv) {
            String[] parts = csv.split(",");
            if (parts.length != 4) {
                throw new IllegalArgumentException("--bbox must be minLon,minLat,maxLon,maxLat");
            }
            int[] bbox = new int[4];
            for (int i = 0; i < 4; i++) {
                bbox[i] = (int) Math.round(Double.parseDouble(parts[i].trim()) * 1e7);
            }
            return bbox;
        }

    }

}
