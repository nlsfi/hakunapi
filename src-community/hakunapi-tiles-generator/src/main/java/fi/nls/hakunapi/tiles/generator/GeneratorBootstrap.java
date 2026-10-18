package fi.nls.hakunapi.tiles.generator;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.SRIDCode;
import fi.nls.hakunapi.core.SimpleFeatureType;
import fi.nls.hakunapi.core.SimpleSource;
import fi.nls.hakunapi.core.config.HakunaConfigParser;
import fi.nls.hakunapi.core.CRSRegistryProvider;
import fi.nls.hakunapi.tiles.TileMatrixSetRegistry;
import fi.nls.hakunapi.tiles.config.TilesConfigParser;

public class GeneratorBootstrap implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(GeneratorBootstrap.class);

    private static final String[] DEFAULT_SOURCE_CLASSES = {
            "fi.nls.hakunapi.simple.postgis.PostGISSimpleSource"
    };

    private final GeneratorServiceConfig service;
    private final TileMatrixSetRegistry tileMatrixSets;
    private final List<SimpleSource> sources;
    private final HakunaConfigParser parser;
    private final Path configPath;
    private final Map<String, SimpleSource> sourcesByType;

    private GeneratorBootstrap(GeneratorServiceConfig service, TileMatrixSetRegistry tileMatrixSets,
            List<SimpleSource> sources,
            HakunaConfigParser parser, Path configPath, Map<String, SimpleSource> sourcesByType) {
        this.service = service;
        this.tileMatrixSets = tileMatrixSets;
        this.sources = sources;
        this.parser = parser;
        this.configPath = configPath;
        this.sourcesByType = sourcesByType;
    }

    public static GeneratorBootstrap read(Path configPath) throws Exception {
        Properties properties = new Properties();
        try (BufferedReader r = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
            properties.load(r);
        }
        HakunaConfigParser parser = new HakunaConfigParser(properties);

        List<SimpleSource> sources = createSources(parser);
        Map<String, SimpleSource> sourcesByType = new HashMap<>();
        for (int i = 0; i < sources.size(); i++) {
            SimpleSource source = sources.get(i);
            sourcesByType.put(source.getType(), source);
        }

        List<SRIDCode> knownSrids = CRSRegistryProvider.getCRSRegistry()
                .map(reg -> reg.detect(properties, parser.getKnownSrids()))
                .orElseThrow(() -> new IllegalStateException("Could not find any CRS registry!"));

        Map<String, FeatureType> collections = new LinkedHashMap<>();
        try {
            for (String collectionId : parser.readCollectionIds()) {
                collections.put(collectionId, parser.readCollection(configPath, sourcesByType, collectionId));
            }
        } catch (Exception e) {
            closeSources(sources);
            throw e;
        }
        if (collections.isEmpty()) {
            closeSources(sources);
            throw new IllegalArgumentException("Missing required config property 'collections'");
        }
        LOG.info("Read {} collections from {}", collections.size(), configPath);

        GeneratorServiceConfig service = new GeneratorServiceConfig(collections);
        service.setKnownSrids(knownSrids);

        TileMatrixSetRegistry tileMatrixSets = new TilesConfigParser(parser).readTileMatrixSets();

        return new GeneratorBootstrap(service, tileMatrixSets, sources,
                parser, configPath, sourcesByType);
    }

    private static List<SimpleSource> createSources(HakunaConfigParser cfg) {
        String[] classNames = cfg.getMultiple("db.classes", DEFAULT_SOURCE_CLASSES);
        List<SimpleSource> sources = new ArrayList<>(classNames.length);
        for (String className : classNames) {
            try {
                sources.add((SimpleSource) Class.forName(className).getDeclaredConstructor().newInstance());
            } catch (ReflectiveOperationException e) {
                throw new IllegalArgumentException("db.classes names source '" + className
                        + "', which could not be instantiated: " + e);
            }
        }
        if (sources.isEmpty()) {
            throw new IllegalArgumentException("db.classes names no feature sources");
        }
        LOG.info("Sources: {}", classNames.length == 0 ? "(none)" : String.join(", ", classNames));
        return sources;
    }

    /** Re-reads the config: FeatureType has no copy operation and sources cast it to their concrete type. */
    public SimpleFeatureType readCollection(String collectionId) throws Exception {
        FeatureType ft = parser.readCollection(configPath, sourcesByType, collectionId);
        if (!(ft instanceof SimpleFeatureType sft)) {
            throw new IllegalStateException("Collection " + collectionId + " is a "
                    + ft.getClass().getName() + ", which cannot carry a zoom range filter");
        }
        return sft;
    }

    public GeneratorServiceConfig getService() {
        return service;
    }

    public TileMatrixSetRegistry getTileMatrixSets() {
        return tileMatrixSets;
    }

    public SRIDCode getSridCode(int srid) {
        return service.getSridCode(srid)
                .orElseThrow(() -> new IllegalArgumentException("Unknown SRID " + srid
                        + "; add it to the service configuration's srid list"));
    }

    @Override
    public void close() {
        closeSources(sources);
    }

    private static void closeSources(List<SimpleSource> sources) {
        for (int i = 0; i < sources.size(); i++) {
            SimpleSource source = sources.get(i);
            try {
                source.close();
            } catch (Exception e) {
                LOG.warn("Failed to close source {}", source.getType(), e);
            }
        }
    }

}
