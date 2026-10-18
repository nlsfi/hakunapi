package fi.nls.hakunapi.tiles.generator;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.OutputFormat;

public class GeneratorServiceConfig extends FeatureServiceConfig {

    private final Map<String, FeatureType> collections;

    public GeneratorServiceConfig(Map<String, FeatureType> collections) {
        this.collections = collections;
    }

    @Override
    public Collection<FeatureType> getCollections() {
        return collections.values();
    }

    @Override
    public FeatureType getCollection(String name) {
        return collections.get(name);
    }

    @Override
    public Collection<OutputFormat> getOutputFormats() {
        return Collections.emptyList();
    }

    @Override
    public OutputFormat getOutputFormat(String f) {
        return null;
    }

}
