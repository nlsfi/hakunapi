package fi.nls.hakunapi.simple.webapp.javax;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.FilterParser;
import fi.nls.hakunapi.core.OutputFormat;
import fi.nls.hakunapi.core.FeatureServiceConfig;

public class SimpleFeatureServiceConfig extends FeatureServiceConfig {

    private final Map<String, FeatureType> collections;
    private final Map<String, OutputFormat> outputFormats;
    private final Map<String, FilterParser> filterParsers;

    public SimpleFeatureServiceConfig(Map<String, FeatureType> collections,
            List<OutputFormat> outputFormats,
            List<FilterParser> filterParsers) {
        this.collections = collections;
        this.outputFormats = toMap(outputFormats, OutputFormat::getId);
        this.filterParsers = toMap(filterParsers, FilterParser::getCode);
    }

    private static <T> Map<String, T> toMap(List<T> list, Function<? super T, String> keyMapper) {
        return list.stream()
                .collect(Collectors.toMap(
                        keyMapper,
                        Function.identity(),
                        (u, v) -> { throw new IllegalStateException("Duplicate key " + u); },
                        LinkedHashMap::new));
    }

    @Override
    public Collection<FeatureType> getCollections() {
        List<FeatureType> collections = new ArrayList<>();
        for (FeatureType sft : this.collections.values()) {
            collections.add(sft);
        }
        return collections;
    }

    @Override
    public FeatureType getCollection(String name) {
        return collections.get(name);
    }

    @Override
    public Collection<OutputFormat> getOutputFormats() {
        return outputFormats.values();
    }

    @Override
    public OutputFormat getOutputFormat(String f) {
        return outputFormats.get(f);
    }

    @Override
    public Collection<FilterParser> getFilterParsers() {
        return filterParsers.values();
    }

    @Override
    public FilterParser getFilterParser(String filterLang) {
        return filterParsers.get(filterLang);
    }

}
