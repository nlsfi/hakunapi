package fi.nls.hakunapi.simple.webapp.javax;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.nls.hakunapi.core.ConformanceClass;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.FilterParser;
import fi.nls.hakunapi.core.MetadataFormat;
import fi.nls.hakunapi.core.OutputFormat;
import fi.nls.hakunapi.core.OutputFormatProvider;
import fi.nls.hakunapi.core.SRIDCode;
import fi.nls.hakunapi.core.SimpleSource;
import fi.nls.hakunapi.core.config.HakunaApplicationJson;
import fi.nls.hakunapi.core.config.HakunaConfigParser;
import fi.nls.hakunapi.core.schemas.FunctionsContent;
import fi.nls.hakunapi.core.telemetry.ServiceTelemetry;
import fi.nls.hakunapi.core.telemetry.TelemetryConfigParser;
import fi.nls.hakunapi.core.telemetry.TelemetryProvider;
import fi.nls.hakunapi.core.util.PropertyUtil;
import fi.nls.hakunapi.cql2.function.CQL2Functions;
import fi.nls.hakunapi.cql2.text.CQL2Text;
import fi.nls.hakunapi.geojson.hakuna.OutputFormatGeoJSON;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@WebListener
public class HakunaContextListener implements ServletContextListener {

    private static final Logger LOG = LoggerFactory.getLogger(HakunaContextListener.class);

    private List<Closeable> toClose;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        toClose = new ArrayList<>();

        try {
            Path configPath = getConfigPath(sce)
                    .orElseThrow(() -> new RuntimeException("Could not find configuration file"));
            Properties config = load(configPath, StandardCharsets.UTF_8);
            if (config == null) {
                config = load(configPath, StandardCharsets.ISO_8859_1);
            }
            if (config == null) {
                throw new RuntimeException(
                        "Could not load configuration from " + configPath.toAbsolutePath().toString());
            }

            String contextName = getContextName(sce);
            applyAppJson(contextName, config, StandardCharsets.UTF_8);

            HakunaConfigParser parser = new HakunaConfigParser(config);
            List<SimpleSource> mySources =  getSources(parser);

            Map<String, SimpleSource> sourcesByType = new HashMap<>();
            for (SimpleSource source : mySources) {
                sourcesByType.put(source.getType(), source);
            }

            Info info = parser.readInfo();
            List<Server> servers = parser.readServers();
            Optional<Map<String, SecurityScheme>> optionalSecuritySchemes = parser.readSecuritySchemes();
            Map<String, SecurityScheme> securitySchemes = null;
            List<SecurityRequirement> securityRequirements = null;
            if (optionalSecuritySchemes.isPresent()) {
                securitySchemes = optionalSecuritySchemes.get();
                securityRequirements = securitySchemes.keySet().stream()
                        .map(name -> new SecurityRequirement().addList(name)).collect(Collectors.toList());
            }
            List<SRIDCode> knownSrids = parser.getKnownSrids();

            Map<String, FeatureType> collections = new HashMap<>();
            for (String collectionId : parser.readCollectionIds()) {
                FeatureType ft = parser.readCollection(configPath, sourcesByType, collectionId);
                collections.put(collectionId, ft);
            }

            List<ConformanceClass> conformsTo = new ArrayList<>();
            conformsTo.add(ConformanceClass.Core);
            conformsTo.add(ConformanceClass.OpenAPI30);
            conformsTo.add(ConformanceClass.GeoJSON);
            conformsTo.add(ConformanceClass.HTML);
            conformsTo.add(ConformanceClass.CRS);
            conformsTo.add(ConformanceClass.GPKG);

            conformsTo.add(ConformanceClass.QUERYABLES);
            conformsTo.add(ConformanceClass.QUERYABLES_QUERY_PARAMETERS);
            conformsTo.add(ConformanceClass.FILTER);
            conformsTo.add(ConformanceClass.FEATURES_FILTER);
            conformsTo.add(ConformanceClass.BASIC_CQL);
            conformsTo.add(ConformanceClass.ADVANCED_COMPARISON_OPERATORS);
            conformsTo.add(ConformanceClass.BASIC_SPATIAL_OPERATORS);
            conformsTo.add(ConformanceClass.SPATIAL_OPERATORS);
            conformsTo.add(ConformanceClass.CQL2_TEXT_ENCODING);
            // conformsTo.add(ConformanceClass.CQL2_JSON_ENCODING);

            List<OutputFormat> outputFormats = getOutputFormats(parser);

            List<FilterParser> filterParsers = Arrays.asList(CQL2Text.INSTANCE);
            CQL2Functions.INSTANCE.register();
            FunctionsContent functionsMetadata = CQL2Functions.INSTANCE.toFunctionsMetadata();

            SimpleFeatureServiceConfig service = new SimpleFeatureServiceConfig(collections, outputFormats, filterParsers);
            service.setInfo(info);
            service.setServers(servers);
            service.setLimitDefault(PropertyUtil.getInt(config, "getfeatures.limit.default", 1000));
            service.setLimitMaximum(PropertyUtil.getInt(config, "getfeatures.limit.max", 10000));
            service.setConformanceClasses(conformsTo);
            service.setSecuritySchemes(securitySchemes);
            service.setSecurityRequirements(securityRequirements);
            service.setFunctions(functionsMetadata);
            service.setMetadataFormats(List.of(MetadataFormat.JSON, MetadataFormat.HTML));
            service.setKnownSrids(knownSrids);
            
            ServiceTelemetry telemetry = TelemetryConfigParser.parse(service, parser);
            service.setTelemetry(telemetry);
            toClose.add(telemetry);

            telemetry.start();
            

            LOG.info("Starting OGC API Features service with collections: {}", collections);
            sce.getServletContext().setAttribute("hakunaService", service);
            sce.getServletContext().setAttribute("hakunaConfig", parser);
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
            close();
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
    }

	/*/ may be overridden with property f.ex
	 db.classes=fi.nls.hakunapi.source.HakunaTestSource
	 */
	protected static String[] DEFAULT_SOURCE_CLASSES = new String[] { 
			//
			"fi.nls.hakunapi.simple.postgis.PostGISSimpleSource"
	};

	protected List<SimpleSource> getSources(HakunaConfigParser config) {

		String sourceClassesStr = config.get("db.classes");
		if(sourceClassesStr==null) {
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
    
    
    private List<OutputFormat> getOutputFormats(HakunaConfigParser config) {
        List<OutputFormat> outputFormats = new ArrayList<>();
        for (String format : config.getMultiple("formats")) {
            Map<String, String> formatConfig = config.getAllStartingWith("formats." + format + ".");
            OutputFormat f = OutputFormatProvider.getOutputFormat(formatConfig);
            if (f == null) {
                // LOG?
            } else {
                outputFormats.add(f);
            }
        }
        if (outputFormats.isEmpty()) {
            return Collections.singletonList(OutputFormatGeoJSON.INSTANCE);
        }
        return outputFormats;
    }

    private Optional<Path> getConfigPath(ServletContextEvent sce) {
        String contextPath = sce.getServletContext().getContextPath();
        if (contextPath.length() > 0) {
            contextPath = contextPath.substring(1);
        }
        return getConfigPath(contextPath);
    }

    private String getContextName(ServletContextEvent sce) {
        String contextName = sce.getServletContext().getContextPath();
        if (contextName.length() > 0) {
            contextName = contextName.substring(1);
        }
        return contextName;
    }

    protected Optional<Path> getConfigPath(String contextPath) {
        return Stream.of(
                contextPath + ".hakunapi.config.path",
                contextPath + ".hakuna.config.path",
                "hakunapi.config.path")
                .map(this::getConfigPathByProperty)
                .filter(Optional::isPresent)
                .findAny()
                .orElseGet(() -> getLegacyHakunaConfigPath(contextPath));
    }

    private Optional<Path> getConfigPathByProperty(String property) {
        String configPath = getProperty(property);
        if (configPath != null && !configPath.isBlank()) {
            LOG.info("{} = {}", property, configPath);
            Path path = Paths.get(configPath);
            if (Files.exists(path)) {
                return Optional.of(path);
            }
            LOG.info("{} file doesn't exist!", configPath);
        } else {
            LOG.debug("Property {} not set or set to empty!", property);
        }
        return Optional.empty();
    }

    private Optional<Path> getLegacyHakunaConfigPath(String contextPath) {
        String property = "hakuna.config.path";
        String configPath = getProperty(property);
        if (configPath != null && !configPath.isEmpty()) {
            LOG.info("{} = {}", property, configPath);
            Path path = Paths.get(configPath, contextPath + ".properties");
            if (Files.exists(path)) {
                return Optional.of(path);
            }
            LOG.info("{} file doesn't exist!", path.toString());
        } else {
            LOG.info("Property {} not set or set to empty!", property);
        }
        return Optional.empty();
    }

    private String getProperty(final String property) {
        // first check environment variables
        final String envValue = getEnv(getEnvVariableName(property));
        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }
        // fallback to system properties
        return System.getProperty(property);
    }

    String getEnv(String envVariable) {
        return System.getenv(envVariable);
    }

    private String getEnvVariableName(String property) {
        return property.toUpperCase().replace('.', '_');
    }

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

    protected void applyAppJson(String contextName, Properties config, Charset utf8) {

        String json = System.getenv(HakunaApplicationJson.HAKUNA_APPLICATION_JSON_PROP);
        if (json == null) {
            json = System.getProperty(HakunaApplicationJson.HAKUNA_APPLICATION_JSON_PROP);
        }
        if (json == null || json.isEmpty()) {
            return;
        }
        HakunaApplicationJson appJson = HakunaApplicationJson.readFromString(json);

        appJson.setContextProperties(contextName, config);

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        close();
    }

    private void close() {
        for (Closeable c : toClose) {
            try {
                c.close();
            } catch (Exception e) {
                // Ignore
            }
        }
    }

}
