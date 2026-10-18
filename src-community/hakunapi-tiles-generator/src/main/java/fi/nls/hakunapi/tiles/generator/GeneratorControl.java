package fi.nls.hakunapi.tiles.generator;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.FilterParser;
import fi.nls.hakunapi.core.SRIDCode;
import fi.nls.hakunapi.core.config.HakunaConfigParser;
import fi.nls.hakunapi.core.filter.Filter;

public class GeneratorControl {

    public static final String PROP_TILE_MATRIX_SET = "tileMatrixSet";
    public static final String PROP_RANGES = "ranges";

    private final String tileMatrixSetId;
    private final List<ZoomRange> ranges;

    public GeneratorControl(String tileMatrixSetId, List<ZoomRange> ranges) {
        this.tileMatrixSetId = tileMatrixSetId;
        this.ranges = List.copyOf(ranges);
    }

    public static HakunaConfigParser load(Path path) throws Exception {
        Properties properties = new Properties();
        try (BufferedReader r = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            properties.load(r);
        }
        return new HakunaConfigParser(properties);
    }

    public static String readTileMatrixSetId(HakunaConfigParser cfg) {
        return cfg.getRequired(PROP_TILE_MATRIX_SET);
    }

    public static GeneratorControl readRanges(HakunaConfigParser cfg, FeatureServiceConfig service,
            List<FilterParser> filters, SRIDCode srid) {
        String tileMatrixSetId = readTileMatrixSetId(cfg);

        String[] rangeNames = cfg.getMultiple(PROP_RANGES);
        if (rangeNames.length == 0) {
            throw new IllegalArgumentException("control file: " + PROP_RANGES + " must name at least one zoom range");
        }

        List<ZoomRange> ranges = new ArrayList<>(rangeNames.length);
        for (String name : rangeNames) {
            ranges.add(readRange(cfg, name, service, filters, srid));
        }
        ranges.sort((a, b) -> Integer.compare(a.minZoom(), b.minZoom()));
        validateContiguous(ranges);

        return new GeneratorControl(tileMatrixSetId, ranges);
    }

    private static ZoomRange readRange(HakunaConfigParser cfg, String name, FeatureServiceConfig service,
            List<FilterParser> filters, SRIDCode srid) {
        String p = name + ".";
        int[] zoom = parseZoomRange(name, cfg.getRequired(p + "zoom"));

        String[] collectionIds = cfg.getMultiple(p + "collections");
        if (collectionIds.length == 0) {
            throw new IllegalArgumentException("control file: range " + name
                    + " must name at least one collection in " + p + "collections");
        }

        Map<String, Filter> rangeFilters = new LinkedHashMap<>();
        for (String collectionId : collectionIds) {
            FeatureType ft = service.getCollection(collectionId);
            if (ft == null) {
                throw new IllegalArgumentException("control file: range " + name
                        + " names unknown collection '" + collectionId + "'");
            }
            String cql2 = cfg.get(p + collectionId + ".filter");
            if (cql2 != null && !cql2.isBlank()) {
                rangeFilters.put(collectionId, parseFilter(name, collectionId, cql2, ft, filters, srid));
            }
        }

        return new ZoomRange(name, zoom[0], zoom[1], List.of(collectionIds), rangeFilters);
    }

    private static Filter parseFilter(String rangeName, String collectionId, String cql2, FeatureType ft,
            List<FilterParser> filters, SRIDCode srid) {
        if (filters.isEmpty()) {
            throw new IllegalStateException("No filter parser available to parse range filters");
        }
        FilterParser parser = filters.get(0);
        try {
            return parser.parse(ft, cql2, srid);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("control file: range " + rangeName + " filter for collection '"
                    + collectionId + "' is not valid " + parser.getCode() + ": " + e.getMessage());
        }
    }

    private static int[] parseZoomRange(String name, String value) {
        String s = value.trim();
        int dash = s.indexOf('-');
        try {
            if (dash < 0) {
                int z = Integer.parseInt(s);
                return new int[] { z, z };
            }
            int min = Integer.parseInt(s.substring(0, dash).trim());
            int max = Integer.parseInt(s.substring(dash + 1).trim());
            if (min > max) {
                throw new IllegalArgumentException("control file: range " + name + " zoom '" + value
                        + "' is inverted");
            }
            return new int[] { min, max };
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("control file: range " + name + " zoom '" + value
                    + "' is not a zoom level or an n-m range");
        }
    }

    private static void validateContiguous(List<ZoomRange> ranges) {
        for (int i = 1; i < ranges.size(); i++) {
            ZoomRange prev = ranges.get(i - 1);
            ZoomRange next = ranges.get(i);
            if (next.minZoom() <= prev.maxZoom()) {
                throw new IllegalArgumentException("control file: ranges " + prev.name() + " (" + prev.minZoom()
                        + "-" + prev.maxZoom() + ") and " + next.name() + " (" + next.minZoom() + "-"
                        + next.maxZoom() + ") overlap");
            }
            if (next.minZoom() != prev.maxZoom() + 1) {
                throw new IllegalArgumentException("control file: no range covers zoom "
                        + (prev.maxZoom() + 1) + " between ranges " + prev.name() + " and " + next.name());
            }
        }
    }

    public String getTileMatrixSetId() {
        return tileMatrixSetId;
    }

    public List<ZoomRange> getRanges() {
        return ranges;
    }

    public List<String> getAllCollectionIds() {
        List<String> all = new ArrayList<>();
        for (int i = 0; i < ranges.size(); i++) {
            for (String id : ranges.get(i).collectionIds()) {
                if (!all.contains(id)) {
                    all.add(id);
                }
            }
        }
        return all;
    }

    public int getMinZoom() {
        return ranges.get(0).minZoom();
    }

    public int getMaxZoom() {
        return ranges.get(ranges.size() - 1).maxZoom();
    }

    public ZoomRange getRange(int zoom) {
        for (int i = 0; i < ranges.size(); i++) {
            ZoomRange range = ranges.get(i);
            if (range.covers(zoom)) {
                return range;
            }
        }
        throw new IllegalArgumentException("No zoom range covers zoom level " + zoom);
    }

}
