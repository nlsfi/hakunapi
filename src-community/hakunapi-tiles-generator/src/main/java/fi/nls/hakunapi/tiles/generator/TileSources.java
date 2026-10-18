package fi.nls.hakunapi.tiles.generator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fi.nls.hakunapi.core.config.HakunaConfigParser;

public class TileSources {

    public static final String PROP_SOURCES = "sources";

    private final Map<String, String> sourceByCollection;

    private final List<String> sourceIds;

    private TileSources(Map<String, String> sourceByCollection, List<String> sourceIds) {
        this.sourceByCollection = Map.copyOf(sourceByCollection);
        this.sourceIds = List.copyOf(sourceIds);
    }

    public static TileSources read(HakunaConfigParser cfg, GeneratorControl control) {
        List<String> rendered = control.getAllCollectionIds();

        String[] ids = cfg.getMultiple(PROP_SOURCES);
        if (ids.length == 0) {
            Map<String, String> identity = new LinkedHashMap<>();
            for (String id : rendered) {
                identity.put(id, id);
            }
            return new TileSources(identity, rendered);
        }

        Map<String, String> byCollection = new LinkedHashMap<>();
        List<String> sourceIds = new ArrayList<>(ids.length);
        for (String sourceId : ids) {
            if (sourceIds.contains(sourceId)) {
                throw new IllegalArgumentException("control file: source '" + sourceId + "' is declared twice");
            }
            for (ZoomRange range : control.getRanges()) {
                if (range.name().equals(sourceId)) {
                    throw new IllegalArgumentException("control file: source '" + sourceId
                            + "' has the same id as a zoom range; both are configured by " + sourceId
                            + ".collections, so rename one");
                }
            }
            sourceIds.add(sourceId);
            String[] collectionIds = cfg.getMultiple(sourceId + ".collections");
            if (collectionIds.length == 0) {
                throw new IllegalArgumentException("control file: source " + sourceId
                        + " must name at least one collection in " + sourceId + ".collections");
            }
            for (String collectionId : collectionIds) {
                String previous = byCollection.putIfAbsent(collectionId, sourceId);
                if (previous != null) {
                    throw new IllegalArgumentException("control file: collection '" + collectionId
                            + "' is in both source " + previous + " and source " + sourceId
                            + "; a collection belongs to exactly one source");
                }
                if (!rendered.contains(collectionId)) {
                    throw new IllegalArgumentException("control file: source " + sourceId
                            + " names collection '" + collectionId + "', which no zoom range renders");
                }
            }
        }

        Set<String> unplaced = new LinkedHashSet<>(rendered);
        unplaced.removeAll(byCollection.keySet());
        if (!unplaced.isEmpty()) {
            throw new IllegalArgumentException("control file: collection(s) " + unplaced
                    + " are rendered by a zoom range but are in no source");
        }

        return new TileSources(byCollection, sourceIds);
    }

    public List<String> getSourceIds() {
        return sourceIds;
    }

    public String getSource(String collectionId) {
        String sourceId = sourceByCollection.get(collectionId);
        if (sourceId == null) {
            throw new IllegalArgumentException("No source configured for collection '" + collectionId + "'");
        }
        return sourceId;
    }

}
