package fi.nls.hakunapi.simple.sdo;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.nls.hakunapi.core.FeatureProducer;
import fi.nls.hakunapi.core.FeatureStream;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.SimpleFeatureType;
import fi.nls.hakunapi.core.SimpleSource;
import fi.nls.hakunapi.core.ValueProvider;
import fi.nls.hakunapi.core.config.HakunaConfigParser;
import fi.nls.hakunapi.core.projection.NOPProjectionTransformer;
import fi.nls.hakunapi.core.projection.ProjectionTransformer;
import fi.nls.hakunapi.core.projection.ProjectionTransformerFactory;
import fi.nls.hakunapi.core.request.GetFeatureCollection;
import fi.nls.hakunapi.core.request.GetFeatureRequest;

public class SdoQueryMain {
	private static final Logger LOG = LoggerFactory.getLogger(SdoQueryMain.class);

	private Properties load(Path path, Charset cs) {
		try (Reader r = Files.newBufferedReader(path, cs)) {
			Properties properties = new Properties();
			properties.load(r);
			return properties;
		} catch (IOException e) {
			LOG.warn(e.getMessage(), e);
			return null;
		}
	}
	

	public static void main(String[] args) throws Exception {
		SdoQueryMain m = new SdoQueryMain();
		Map<String, FeatureType> collections = m.init(args);

		String collectionId = "kiinteistonluovutus";
		FeatureType ft = collections.get(collectionId);
		((SimpleFeatureType)ft).setProjectionTransformerFactory(NOPFactory);
        
		

		int limit = 10;

		GetFeatureCollection c = new GetFeatureCollection(ft);

		GetFeatureRequest request = new GetFeatureRequest();

		request.addPathParam("collectionId", collectionId);
		request.addCollection(c);
		request.setQueryHeaders(Map.of());
		request.setLimit(limit);

		FeatureProducer producer = ft.getFeatureProducer();

		try (FeatureStream features = producer.getFeatures(request, c);) {

			int numberReturned = 0;
			for (; numberReturned < limit; numberReturned++) {
				if (!features.hasNext()) {
					break;
				}

				ValueProvider f = features.next();

				System.out.println(f);
			}

			features.close();

		}

	}

	public Map<String, FeatureType> init(String[] args) throws Exception {
		Path configPath = Paths.get(args[0]);
		Properties config = load(configPath, StandardCharsets.UTF_8);
		if (config == null) {
			config = load(configPath, StandardCharsets.ISO_8859_1);
		}
		if (config == null) {
			throw new RuntimeException("Could not load configuration from " + configPath.toAbsolutePath().toString());
		}

		HakunaConfigParser parser = new HakunaConfigParser(config);
		List<SimpleSource> mySources = getSources(parser);

		Map<String, SimpleSource> sourcesByType = new HashMap<>();
		for (SimpleSource source : mySources) {
			sourcesByType.put(source.getType(), source);
		}

		Map<String, FeatureType> collections = new HashMap<>();
		for (String collectionId : parser.readCollectionIds()) {
			FeatureType ft = parser.readCollection(configPath, sourcesByType, collectionId, Collections.emptyList());
			collections.put(collectionId, ft);
		}
	
		LOG.info("Starting OGC API Features service with collections: {}", collections);

		return collections;

	}

	/*
	 * / may be overridden with property f.ex
	 * db.classes=fi.nls.hakunapi.source.HakunaTestSource
	 */
	protected static String[] DEFAULT_SOURCE_CLASSES = new String[] {
	        //
	        "fi.nls.hakunapi.simple.postgis.PostGISSimpleSource" };

	protected List<SimpleSource> getSources(HakunaConfigParser config) {

		String sourceClassesStr = config.get("db.classes");
		if (sourceClassesStr == null) {
			LOG.info("Source: class list null - using defaults");
		} else {
			LOG.info("Source: class list " + sourceClassesStr);
		}

		String[] sourceClasses = config.getMultiple("db.classes", DEFAULT_SOURCE_CLASSES);

		final List<SimpleSource> sources = Stream.of(sourceClasses).map(clsName -> {
			try {
				return Class.forName(clsName);
			} catch (ClassNotFoundException e1) {
				LOG.error("Source: class not found " + clsName);
				return null;
			}
		}).filter(Objects::nonNull).map(cls -> {
			try {
				SimpleSource source = (SimpleSource) cls.getDeclaredConstructor().newInstance();
				LOG.info("Source: " + source.getType() + " instance " + source);
				return source;
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
			        | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				LOG.error("Source: instantiation exception for " + cls);
				return null;
			}
		}).filter(Objects::nonNull).collect(Collectors.toList());

		return sources;
	}
	
	static ProjectionTransformerFactory NOPFactory = new ProjectionTransformerFactory() {
		
		@Override
		public ProjectionTransformer toCRS84(int fromSRID) throws Exception {			
			return NOPProjectionTransformer.INSTANCE;
		}
		
		@Override
		public ProjectionTransformer getTransformer(int fromSRID, int toSRID) throws Exception {
			return NOPProjectionTransformer.INSTANCE;
		}
		
		@Override
		public ProjectionTransformer fromCRS84(int toSRID) throws Exception {
			return NOPProjectionTransformer.INSTANCE;
		}
	};
	
}
