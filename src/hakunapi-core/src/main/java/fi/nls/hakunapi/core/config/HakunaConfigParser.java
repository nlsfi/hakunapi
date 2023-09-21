package fi.nls.hakunapi.core.config;

import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.nls.hakunapi.core.CacheSettings;
import fi.nls.hakunapi.core.DatetimeProperty;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.SRIDCode;
import fi.nls.hakunapi.core.SimpleFeatureType;
import fi.nls.hakunapi.core.SimpleSource;
import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.geom.HakunaGeometryDimension;
import fi.nls.hakunapi.core.param.GetFeatureParam;
import fi.nls.hakunapi.core.projection.ProjectionTransformerFactory;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyArray;
import fi.nls.hakunapi.core.property.HakunaPropertyJSON;
import fi.nls.hakunapi.core.property.HakunaPropertyNumberEnum;
import fi.nls.hakunapi.core.property.HakunaPropertyStringEnum;
import fi.nls.hakunapi.core.property.HakunaPropertyType;
import fi.nls.hakunapi.core.property.HakunaPropertyWrapper;
import fi.nls.hakunapi.core.property.HakunaPropertyWriter;
import fi.nls.hakunapi.core.property.HakunaPropertyWriters;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyBoolean;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyDate;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyDouble;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyFloat;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyInt;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyLong;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyString;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyTimestamp;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyTimestamptz;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyUUID;
import fi.nls.hakunapi.core.transformer.ValueTransformer;
import fi.nls.hakunapi.simple.servlet.operation.param.ConfigurableSimpleGetFeatureParam;
import fi.nls.hakunapi.simple.servlet.operation.param.CustomizableGetFeatureParam;
import fi.nls.hakunapi.simple.servlet.operation.param.DefaultFilterMapper;
import fi.nls.hakunapi.simple.servlet.operation.param.FilterMapper;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import io.swagger.v3.oas.models.servers.Server;

public class HakunaConfigParser {
    
    private static final Logger LOG = LoggerFactory.getLogger(HakunaConfigParser.class);
    
    public static final String REF_ID_PROP = "$id";
    public static final String REF_GEOMETRY_PROP = "$geometry";
    public static final String REF_GEOMETRY_PROP_ALIAS = "$geom";

    protected final Properties cfg;

    public HakunaConfigParser(Properties cfg) {
        this.cfg = cfg;
    }
    
    public Info readInfo() {
        Info info = new Info();
        info.setTitle(cfg.getProperty("api.title", "hakunapi OGC API Features Server"));
        info.setVersion(cfg.getProperty("api.version", "0.0.1"));
        info.setDescription(cfg.getProperty("api.description", "hakunapi OGC API Features Server"));

        Contact contact = new Contact();
        contact.setName(cfg.getProperty("api.contact.name", "N/A"));
        contact.setEmail(cfg.getProperty("api.contact.email", "N/A"));
        contact.setUrl(cfg.getProperty("api.contact.url", "N/A"));
        info.setContact(contact);

        License license = new License();
        license.setName(cfg.getProperty("api.license.name"));
        license.setUrl(cfg.getProperty("api.license.url"));
        if (license.getName() != null && !license.getName().isBlank()) {
            info.setLicense(license);
        }

        return info;
    }

    public List<Server> readServers() {
        String serverList = getRequired("servers");

        String[] serverNames = serverList.split(",");
        List<Server> servers = new ArrayList<>();
        for (String serverName : serverNames) {
            String prefix = "servers." + serverName;
            String url = cfg.getProperty(prefix + ".url");
            while (url.endsWith("/")) {
                url = url.substring(0, url.length() - 1);
            }
            String description = cfg.getProperty(prefix + ".description");
            Server server = new Server();
            server.setUrl(url);
            server.setDescription(description);
            servers.add(server);
        }

        return servers;
    }

    public Optional<Map<String, SecurityScheme>> readSecuritySchemes() {
        String[] security = getMultiple("security");
        if (security.length == 0) {
            return Optional.empty();
        }
        Map<String, SecurityScheme> securitySchemes = new HashMap<>();
        for (String name : security) {
            String prefix = "security." + name;
            Type type = Type.valueOf(getRequired(prefix + ".type").toUpperCase());
            SecurityScheme ss = new SecurityScheme().type(type);
            switch (type) {
            case HTTP:
                ss.scheme("basic");
                break;
            case APIKEY:
                ss.in(In.valueOf(getRequired(prefix + ".in").toUpperCase()));
                ss.name(getRequired(prefix + ".name"));
                break;
            default:
                throw new IllegalArgumentException("Unsupported security scheme for " + name);
            }
            securitySchemes.put(name, ss);
        }
        return Optional.of(securitySchemes);
    }

    public List<SRIDCode> getKnownSrids() {
        List<SRIDCode> knownSrids = new ArrayList<>();
        for (String sridCode : getMultiple("srid")) {
            int srid = Integer.parseInt(sridCode);
            String latLonAttr = get("srid." + sridCode + ".latLon", "false");
            boolean latLon = "true".equalsIgnoreCase(latLonAttr);
            knownSrids.add(new SRIDCode(srid, latLon));
        }
        return knownSrids;
    }

    public List<HakunaProperty> parseTimeProperties(List<HakunaProperty> properties, String[] timePropertyNames)
            throws IllegalArgumentException{
        if (timePropertyNames.length == 0) {
            return Collections.emptyList();
        }

        List<HakunaProperty> timeProperties = new ArrayList<>();
        for (String timePropertyName : timePropertyNames) {
            timeProperties.add(findByName(properties, timePropertyName));
        }
        return timeProperties;
    }

    public String getRequired(String key) {
        String value = get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing required config property '" + key + "'");
        }
        return value;
    }

    public String get(String key) {
        return get(key, null);
    }

    public String get(String key, String fallback) {
        String value = cfg.getProperty(key);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    public String[] getMultiple(String key) {
        return getMultiple(key, new String[0]);
    }

    public String[] getMultiple(String key, String[] fallback) {
        String value = cfg.getProperty(key);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        String[] split = value.split(",");
        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].trim();
        }
        return split;
    }

    public Map<String, String> getAllStartingWith(String prefix) {
        Map<String, String> map = new HashMap<>();
        for (Enumeration<?> e = cfg.propertyNames(); e.hasMoreElements();) {
            Object o = e.nextElement();
            if (o instanceof String) {
                String s = (String) o;
                if (s.startsWith(prefix)) {
                    map.put(s.substring(prefix.length()), cfg.getProperty(s));
                }
            }
        }
        return map;
    }

    protected static int[] getSRIDs(String srid) {
        if (srid == null || srid.isBlank()) {
            return new int[0];
        }
        return Arrays.stream(srid.split(","))
                .map(String::trim)
                .mapToInt(Integer::parseInt)
                .distinct()
                .toArray();
    }

    public HakunaProperty findByName(List<HakunaProperty> properties, String name) {
        for (HakunaProperty property : properties) {
            if (name.equals(property.getName())) {
                return property;
            }
            if (name.equals(property.getColumn()) ) {
                return property;
            }
        }
        throw new IllegalArgumentException("Unknown property: " + name);
    }

    public static boolean isStaticMapping(String mapping) {
        if (mapping == null) {
            return false;
        }
        return mapping.startsWith("'") && mapping.endsWith("'");
    }

    public ValueTransformer instantiateTransformer(String transformerClass) {
        try {
            return (ValueTransformer) Class.forName(transformerClass).getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to init class " + transformerClass);
        }
    }

    public String[] readCollectionIds() {
    	String rawCollectionId = get("collections");
    	LOG.info("collectionIds "+rawCollectionId);
        String[] collectionsIds = getMultiple("collections");
        if (collectionsIds.length == 0) {
            throw new IllegalArgumentException("Missing required config property 'collections'");
        }
        return collectionsIds;
    }

    public FeatureType readCollection(Path path, Map<String, SimpleSource> sourcesByType, String collectionId) throws Exception {
    	LOG.info("collection "+collectionId);
        // Current prefix for properties
        String p = "collections." + collectionId + ".";

        String title = get(p + "title", collectionId);
        String description = get(p + "description", title);
        int[] srids = getSRIDs(get(p + "srid", get("default.collections.srid")));
        HakunaGeometryDimension dim = getGeometryDims(collectionId, get(p + "geometryDimension"));

        String defaultType = get("default.collections.type", "pg");
        String type = get(p + "type", defaultType);
        SimpleSource source = sourcesByType.get(type);
        if (source == null) {
            throw new IllegalArgumentException("Unknown type: " + type + ", collection: " + collectionId);
        }
        SimpleFeatureType ft = source.parse(this, path, collectionId, srids);

        ft.setTitle(title);
        ft.setDescription(description);
        ft.setGeomDimension(dim);
        ft.setMetadata(parseMetadata(p, path));
        ft.setProjectionTransformerFactory(getProjection(p));
        ft.setCacheSettings(parseCacheConfig(collectionId));

        String[] timeProps = getMultiple(p + "time");
        Set<String> timeExclusiveProps = Arrays.stream(getMultiple(p + "time.exclusive")).collect(Collectors.toSet());
        List<DatetimeProperty> datetimeProperties = new ArrayList<>();
        for (String timeProp : timeProps) {
            HakunaProperty backingProperty = ft.getProperties().stream().filter(x -> x.getName().equalsIgnoreCase(timeProp))
                    .findAny().orElseThrow(() -> new IllegalArgumentException("Unknown time prop " + timeProp));
            boolean inclusive = !timeExclusiveProps.contains(timeProp);
            datetimeProperties.add(new DatetimeProperty(backingProperty, inclusive));
        }
        ft.setDatetimeProperties(datetimeProperties);

        List<GetFeatureParam> getFeatureParams = new ArrayList<>();
        String[] parameters = getMultiple(p + "parameters");
        List<HakunaProperty> queryableProperties = new ArrayList<>();
        Set<String> queryablePropertyNames = new HashSet<>();
        for (String parameter : parameters) {
            String cfgPrefix = p + "parameters." + parameter + ".";
            String prop = get(cfgPrefix + "prop", parameter);
            HakunaProperty hakunaProperty = getProperty(ft, prop);
            if (!queryablePropertyNames.contains(hakunaProperty.getName())) {
                queryableProperties.add(hakunaProperty);
            }
            getFeatureParams.add(parseParameter(hakunaProperty, cfgPrefix));
        }
        ft.setQueryableProperties(queryableProperties);
        ft.setParameters(getFeatureParams);

        List<Filter> staticFilters = new ArrayList<>();
        String[] filters = getMultiple(p + "filters");
        for (String filter : filters) {
            String cfgPrefix = p + "filters.";
            staticFilters.add(parseFilter(ft, cfgPrefix, filter));
        }
        ft.setStaticFilters(staticFilters);

        String[] spatialExtent = getMultiple(p + "extent.spatial");
        if (spatialExtent.length != 0) {
            if (ft.getGeom() == null) {
                throw new IllegalArgumentException("Collection " + collectionId
                        + " failed: extent.spatial set but no (main) geometry property set!");
            }
            int expectedExtentSize = ft.getGeom().getDimension() * 2;
            if (spatialExtent.length != expectedExtentSize) {
                throw new IllegalArgumentException("Collection " + collectionId + " failed:"
                        + " extent.spatial contained " + spatialExtent.length + " elements, expected value was "
                        + expectedExtentSize + " (geometry dimension times two)");
            }
            ft.setSpatialExtent(Arrays.stream(spatialExtent).mapToDouble(Double::parseDouble).toArray());
        }
        
        spatialExtent = getMultiple(p + "extent.spatial.crs84");
        if (spatialExtent.length != 0) {
            int expectedExtentSize = 4;
            if (spatialExtent.length != expectedExtentSize) {
                throw new IllegalArgumentException("Collection " + collectionId + " failed:"
                        + " extent.spatial.crs84 contained " + spatialExtent.length + " elements, expected value was "
                        + expectedExtentSize);
            }
            ft.setSpatialExtent(Arrays.stream(spatialExtent).mapToDouble(Double::parseDouble).toArray());
        }

        if (ft.getGeom() != null) {
            if (ft.getSpatialExtent() == null) {
                String msg = "Collection " + collectionId + " warning:"
                        + " spatial extent not configured even though geometry property is configured!";
                LOG.warn(msg);
            } else {
                validateSpatialExtent(ft.getSpatialExtent());
            }
        }

        String[] temporalExtent = getMultiple(p + "extent.temporal");
        if (temporalExtent.length != 0) {
            if (ft.getDatetimeProperties().isEmpty()) {
                throw new IllegalArgumentException(
                        "Collection " + collectionId + " failed: extent.temporal set but no time property set!");
            }
            ft.setTemporalExtent(Arrays.stream(temporalExtent)
                    .map(Instant::parse)
                    .collect(Collectors.toList())
                    .toArray(new Instant[0]));
        }

        return ft;
    }

    private void validateSpatialExtent(double[] spatialExtent) {
        // Part 1 (or Part 2) only support CRS84 (or CRS84h) for spatial extent
        // Hakunapi only supports single spatial extent bounding box
        double x1, y1, x2, y2;
        if (spatialExtent.length == 4) {
            x1 = spatialExtent[0];
            y1 = spatialExtent[1];
            x2 = spatialExtent[2];
            y2 = spatialExtent[3];
        } else if (spatialExtent.length == 6) {
            x1 = spatialExtent[0];
            y1 = spatialExtent[1];
            x2 = spatialExtent[3];
            y2 = spatialExtent[4];
        } else {
            throw new IllegalArgumentException("Spatial extent length must be 4 or 6");
        }

        validateValueWithin(x1, -180.0, 180.0, "Spatial extent minimum longitude");
        validateValueWithin(x2, -180.0, 180.0, "Spatial extent maximum longitude");
        validateValueWithin(y1, -90.0, 90.0, "Spatial extent minimum latitude");
        validateValueWithin(y2, -90.0, 90.0, "Spatial extent maximum latitude");
    }

    private void validateValueWithin(double value, double min, double max, String field) {
        if (value < min || value > max) {
            String err = String.format(Locale.US, "%s value %f not within range [%f,%f]",
                    field, value, min, max);
            throw new IllegalArgumentException(err);
        }
    }

    @SuppressWarnings("unchecked")
	private ProjectionTransformerFactory getProjection(String p) {
        try {
            String projectionTransformerFactoryClass = get(p + "proj", "fi.nls.hakunapi.proj.gt.GeoToolsProjectionTransformerFactory");
            Class<?> cl = Class.forName(projectionTransformerFactoryClass);
            Class<? extends ProjectionTransformerFactory> c = (Class<? extends ProjectionTransformerFactory>) cl;
            ProjectionTransformerFactory pt = c.getDeclaredConstructor().newInstance();
            return pt;
        } catch (Exception e) {
            LOG.warn("Failed to init projection transformer", e.getMessage());
            return null;
        }
    }

    private HakunaGeometryDimension getGeometryDims(String collectionId,String dimmode ) {
        if( dimmode == null ) {
            return HakunaGeometryDimension.DEFAULT;
        }
        try {
            HakunaGeometryDimension dim = HakunaGeometryDimension.valueOf(dimmode);
            LOG.info("using "+dim+" dimension for "+collectionId);
            return dim;
        } catch (IllegalArgumentException e) {
            LOG.warn("using default dimension for "+collectionId + " invalid config value "+dimmode);
            return HakunaGeometryDimension.DEFAULT;
        }
    }

    public HakunaProperty getDynamicProperty(String name, String table, String column, List<HakunaPropertyType> typeChain, boolean nullable, boolean unique, boolean hidden) {
        if (typeChain.size() == 1) {
            HakunaPropertyType type = typeChain.get(0);
            HakunaPropertyWriter writer = hidden ? HakunaPropertyWriters.HIDDEN : HakunaPropertyWriters.getSimplePropertyWriter(name, type);
            switch (type) {
            case BOOLEAN:
                return new HakunaPropertyBoolean(name, table, column, nullable, unique, writer);
            case INT:
                return new HakunaPropertyInt(name, table, column, nullable, unique, writer);
            case LONG:
                return new HakunaPropertyLong(name, table, column, nullable, unique, writer);
            case FLOAT:
                return new HakunaPropertyFloat(name, table, column, nullable, unique, writer);
            case DOUBLE:
                return new HakunaPropertyDouble(name, table, column, nullable, unique, writer);
            case STRING:
                return new HakunaPropertyString(name, table, column, nullable, unique, writer);
            case DATE:
                return new HakunaPropertyDate(name, table, column, nullable, unique, writer);
            case TIMESTAMP:
                return new HakunaPropertyTimestamp(name, table, column, nullable, unique, writer);
            case TIMESTAMPTZ:
                return new HakunaPropertyTimestamptz(name, table, column, nullable, unique, writer);
            case UUID:
                return new HakunaPropertyUUID(name, table, column, nullable, unique, writer);
            case JSON:
                return new HakunaPropertyJSON(name, table, column, type, nullable, unique, writer);
            default:
                throw new IllegalArgumentException("Configuration error");
            }
        } else {
            if (typeChain.get(0) != HakunaPropertyType.ARRAY) {
                throw new IllegalArgumentException("Configuration error");
            }
            HakunaPropertyWriter wrapped = HakunaPropertyWriters.getSimplePropertyWriter(null, typeChain.get(typeChain.size() - 1));
            for (int i = typeChain.size() - 2; i > 0; i--) {
                wrapped = HakunaPropertyWriters.getArrayPropertyWriter(null, wrapped);
            }
            HakunaPropertyWriter writer = HakunaPropertyWriters.getArrayPropertyWriter(name, wrapped);
            return new HakunaPropertyArray(name, table, column, typeChain, nullable, unique, writer);
        }
    }

    private GetFeatureParam parseParameter(HakunaProperty hakunaProperty, String cfgPrefix) {
        String name = get(cfgPrefix + "name", hakunaProperty.getName());
        String desc = get(cfgPrefix + "desc", "Filter the collection by " + name);
        String impl = get(cfgPrefix + "impl", DefaultFilterMapper.class.getName());
        String arg = get(cfgPrefix + "impl.arg");

        Class<?> clazz;
        try {
            clazz = Class.forName(impl);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to load class " + impl);
        }

        if (CustomizableGetFeatureParam.class.isAssignableFrom(clazz)) {
            CustomizableGetFeatureParam instance;
            try {
                instance = (CustomizableGetFeatureParam) clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to instantiate object from class " + impl);
            }
            instance.init(hakunaProperty, name, desc, arg);
            return instance;
        }

        if (FilterMapper.class.isAssignableFrom(clazz)) {
            FilterMapper fm;
            try {
                fm = (FilterMapper) clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to instantiate object class " + impl);
            }
            fm.init(arg);
            return new ConfigurableSimpleGetFeatureParam(hakunaProperty.getFeatureType(), hakunaProperty, fm, name,
                    desc);
        }

        throw new IllegalArgumentException("Don't know what to do with parameter " + name + " class is un-usable");
    }

    private Filter parseFilter(SimpleFeatureType sft, String cfgPrefix, String filterId) {
        String p = cfgPrefix + filterId;
        String type = getRequired(p + ".type");
        String[] value = getMultiple(p + ".value");

        if (type.equals("OR")) {
            List<Filter> or = new ArrayList<>(value.length);
            for (String subFilter : value) {
                or.add(parseFilter(sft, cfgPrefix, subFilter));
            }
            return Filter.or(or);
        }
        if (type.equals("AND")) {
            List<Filter> and = new ArrayList<>(value.length);
            for (String subFilter : value) {
                and.add(parseFilter(sft, cfgPrefix, subFilter));
            }
            return Filter.and(and);
        }

        String propertyName = getRequired(p + ".prop");
        HakunaProperty property = getProperty(sft, propertyName);
        // Allow static filters outside of (public) enumerations
        if (property instanceof HakunaPropertyNumberEnum || property instanceof HakunaPropertyStringEnum) {
            property = ((HakunaPropertyWrapper) property).getWrapped();
        }
        final HakunaProperty prop = property;

        if (type.equals("IS NULL")) {
            return Filter.isNull(prop);
        }
        if (type.equals("IS NOT NULL")) {
            return Filter.isNotNull(prop);
        }

        if (value.length == 0) {
            throw new IllegalArgumentException("Expected atleast a single value");
        }

        if (value.length == 1) {
            String v = value[0];
            switch (type) {
            case "=":
            case "IN":
                return Filter.equalTo(prop, v);
            case "!=":
            case "<>":
            case "NOT IN":
                return Filter.notEqualTo(prop, v);
            case ">":
                return Filter.greaterThan(prop, v);
            case "<":
                return Filter.lessThan(prop, v);
            case ">=":
                return Filter.greaterThanOrEqualTo(prop, v);
            case "<=":
                return Filter.lessThanThanOrEqualTo(prop, v);
            default:
                throw new IllegalArgumentException("Invalid type (for a single value)");
            }
        }

        if (value.length == 2 && type.equals("BETWEEN")) {
            return Filter.and(Filter.greaterThanOrEqualTo(prop, value[0]),
                    Filter.lessThanThanOrEqualTo(prop, value[1]));
        }

        switch (type) {
        case "=":
        case "IN": // IN (1,2,3) => 1 OR 2 OR 3
            return Filter.or(Arrays.stream(value).map(v -> Filter.equalTo(prop, v)).collect(Collectors.toList()));
        case "!=":
        case "<>":
        case "NOT IN": // NOT IN (1,2,3) => (NOT 1) AND (NOT 2) AND (NOT 3)
            return Filter.and(Arrays.stream(value).map(v -> Filter.notEqualTo(prop, v)).collect(Collectors.toList()));
        default:
            throw new IllegalArgumentException("Invalid type");
        }
    }

    public Map<String, Object> parseMetadata(String p, Path configPath) {
        String metadataProp = get(p + "metadata");
        if (metadataProp == null || metadataProp.isBlank()) {
            return null;
        }

        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {};
        ObjectMapper om = new ObjectMapper();

        try {
            return om.readValue(metadataProp, typeRef);
        } catch (Exception e) {
            LOG.debug("Failed to parse metadata as JSON object");
        }

        // Try as a file
        File file = new File(metadataProp);
        if (!file.canRead()) {
            LOG.debug("Can't read file {}", file.toString());
            file = new File(configPath.toFile(), metadataProp);
        }

        if (file.canRead()) {
            try {
                return om.readValue(file, typeRef);
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to parse metadata from file " + metadataProp);
            }
        } else {
            throw new IllegalArgumentException("Can't read file " + metadataProp);
        }
    }

    public HakunaProperty getProperty(FeatureType sft, String prop) {
        switch (prop) {
        case REF_ID_PROP:
            return sft.getId();
        case REF_GEOMETRY_PROP:
        case REF_GEOMETRY_PROP_ALIAS:
            return sft.getGeom();
        default:
            return findByName(sft.getProperties(), prop);
        }
    }

    private CacheSettings parseCacheConfig(String collectionId) {
        String p = "collections." + collectionId + ".cache";
        if (!Boolean.parseBoolean(get(p, "false"))) {
            return null;
        }

        int size = 100;
        long refresh = Duration.ofMinutes(1).toMillis();
        long expire = Duration.ofMinutes(2).toMillis();

        if (get(p + ".size") != null) {
            size = Integer.parseInt(get(p + ".size"));
        }
        if (get(p + ".refresh") != null) {
            refresh = Long.parseLong(get(p + ".refresh"));
        }
        if (get(p + ".expire") != null) {
            expire = Long.parseLong(get(p + ".expire"));
        }

        if (size <= 0) {
            throw new IllegalArgumentException(p + ".size must be positive");
        }
        if (refresh <= 0) {
            throw new IllegalArgumentException(p + ".refresh must be positive");
        }
        if (expire <= 0) {
            throw new IllegalArgumentException(p + ".expire must be positive");
        }
        if (expire < refresh) {
            throw new IllegalArgumentException(p + ".expire must be >= " + p + ".refresh");
        }

        return new CacheSettings(size, refresh, expire);
    }

}
