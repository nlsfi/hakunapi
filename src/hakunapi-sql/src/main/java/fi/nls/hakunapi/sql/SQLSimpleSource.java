package fi.nls.hakunapi.sql;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Path;
import java.sql.Array;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import fi.nls.hakunapi.core.HakunapiPlaceholder;
import fi.nls.hakunapi.core.PaginationStrategy;
import fi.nls.hakunapi.core.PaginationStrategyCursor;
import fi.nls.hakunapi.core.PaginationStrategyOffset;
import fi.nls.hakunapi.core.SimpleFeatureType;
import fi.nls.hakunapi.core.SimpleSource;
import fi.nls.hakunapi.core.config.HakunaConfigParser;
import fi.nls.hakunapi.core.geom.HakunaGeometryType;
import fi.nls.hakunapi.core.join.Join;
import fi.nls.hakunapi.core.join.ManyToManyJoin;
import fi.nls.hakunapi.core.join.ManyToOneJoin;
import fi.nls.hakunapi.core.join.OneToManyJoin;
import fi.nls.hakunapi.core.join.OneToOneJoin;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyComposite;
import fi.nls.hakunapi.core.property.HakunaPropertyHidden;
import fi.nls.hakunapi.core.property.HakunaPropertyNumberEnum;
import fi.nls.hakunapi.core.property.HakunaPropertyStatic;
import fi.nls.hakunapi.core.property.HakunaPropertyStringEnum;
import fi.nls.hakunapi.core.property.HakunaPropertyTransformed;
import fi.nls.hakunapi.core.property.HakunaPropertyType;
import fi.nls.hakunapi.core.property.HakunaPropertyWriter;
import fi.nls.hakunapi.core.property.HakunaPropertyWriters;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyInt;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyLong;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyString;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyUUID;
import fi.nls.hakunapi.core.transformer.ValueTransformer;

public abstract class SQLSimpleSource implements SimpleSource {

    protected static final Logger LOG = LoggerFactory.getLogger(SQLSimpleSource.class);
    
    protected List<HikariDataSource> dataSourcesToClose = new ArrayList<>();
    protected Map<String, DataSource> dataSources = new HashMap<>();
    
    public abstract String getType(); 
    public abstract SQLFeatureType createFeatureType();
    protected abstract HakunaPropertyGeometry getGeometryColumn(DataSource ds, String schema, String table, String name,
            String column, int[] srids, int sridStorage, boolean isDefault, boolean nullable, boolean hidden) throws SQLException ;
    
    

    protected DataSource getDataSource(HakunaConfigParser cfg, Path path, String name) throws NamingException {
        DataSource ds = dataSources.get(name);
        if (ds == null) {
            if (name.startsWith("java:comp/env/")) {
                ds = getJNDIDataSource(new InitialContext(), name);
            } else {
                HikariDataSource hds = readDataSource(cfg, path, name);
                dataSourcesToClose.add(hds);
                ds = hds;
            }
            dataSources.put(name, ds);
        }
        return ds;
    }
    
    protected DataSource getJNDIDataSource(InitialContext ctx, String name) throws NamingException {
        Object value = ctx.lookup(name);
        if (value == null) {
            throw new IllegalArgumentException("Could not find object from JNDI with name " + name);
        }
        if (!(value instanceof DataSource)) {
            throw new IllegalArgumentException("JNDI object with name " + name + " not a DataSource");
        }
        return (DataSource) value;
    }

    protected HikariDataSource readDataSource(HakunaConfigParser cfg, Path path, String name) {
        final Properties props;

        if (Arrays.stream(cfg.getMultiple("db", new String[0])).anyMatch(name::equals)) {
            // if name appears in db=a,b,c,d listing then consider it's configuration to be inlined
            props = getInlinedDBProperties(cfg, name);
        } else {
            // Not inlined, check separate file $name.properties
            String dataSourcePath = getDataSourcePath(path, name);
            props = loadProperties(dataSourcePath);
        }

        HikariConfig config = new HikariConfig(props);
        HikariDataSource ds = new HikariDataSource(config);
        return ds;
    }

    public static Properties getInlinedDBProperties(HakunaConfigParser cfg, String name) {
        Properties props = new Properties();
        cfg.getAllStartingWith("db." + name + ".").forEach(props::setProperty);
        return props;
    }

    public static String getDataSourcePath(Path path, String name) {
        String prefix = "";
        Path parent = path.getParent();
        if (parent != null) {
            prefix = path.getParent().toString() + "/";
        }
        return prefix + name + ".properties";
    }

    public Properties loadProperties(String path) {
        try (InputStream in = getInputStream(path)) {
            Properties props = new Properties();
            props.load(in);
            return HakunapiPlaceholder.replacePlaceholders(props);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read property file", e);
        }
    }

    protected InputStream getInputStream(String path) throws IOException {
        File file = new File(path);
        if (file.isFile()) {
            return new FileInputStream(file);
        }
        InputStream resource = getClass().getResourceAsStream(path);
        if (resource != null) {
            return resource;
        }
        resource = getClass().getClassLoader().getResourceAsStream(path);
        return resource;
    }

    @Override
    public void close() throws IOException {
        dataSourcesToClose.forEach(HikariDataSource::close);
    }

    @Override
    public SimpleFeatureType parse(HakunaConfigParser cfg, Path path, String collectionId, int[] srids) throws Exception {
        // Current prefix for properties
        String p = "collections." + collectionId + ".";
        SQLFeatureType ft = createFeatureType();
        ft.setName(collectionId);

        String db = cfg.get(p + "db");
        DataSource ds = getDataSource(cfg, path, db);
        if (ds == null) {
            throw new IllegalArgumentException("Unknown db: " + db + " collection: " + collectionId);
        }

        String dbSchema = cfg.get(p + "schema", "public").replace("\"", "");

        String primaryTable = cfg.get(p + "table");
        if (primaryTable == null || primaryTable.isEmpty()) {
            throw new IllegalArgumentException("Missing required property " + (p + "table"));
        }
        primaryTable = primaryTable.replace("\"", "");

        int sridStorage = Integer.parseInt(cfg.get(p + "srid.storage", "0"));

        List<Join> joins = new ArrayList<>();
        for (String join : cfg.getMultiple("joins")) {
            Join j = parseJoin(cfg, join);
            joins.add(j);
            joins.add(j.swapOrder());
        }

        ft.setPrimaryTable(primaryTable);
        ft.setDatabase(ds);
        ft.setDbSchema(dbSchema);
        ft.setJoins(joins);

        Map<String, Set<ColumnAlias>> columnsByTable = new HashMap<>();
        String[] idTables = cfg.getMultiple(p + "id.table");
        String[] idMappings = cfg.getMultiple(p + "id.mapping");
        String[] idAliases = cfg.getMultiple(p + "id.alias");
        if (idMappings.length == 0) {
            throw new IllegalArgumentException("Missing required property " + (p + "id.mapping"));
        }
        for (int i = 0; i < idMappings.length; i++) {
            String table = idTables.length > i ? idTables[i] : primaryTable;
            String mapping = idMappings[i];
            String alias = idAliases.length > i ? idAliases[i] : null;
            if (!HakunaConfigParser.isStaticMapping(mapping)) {
                columnsByTable.computeIfAbsent(table, __ -> new HashSet<>()).add(new ColumnAlias(mapping, alias));
            }
        }

        String geometryTable = cfg.get(p + "geometry.table", primaryTable);
        String geometryMapping = cfg.get(p + "geometry.mapping");
        String geometryAlias = cfg.get(p + "geometry.alias");
        if (geometryMapping != null) {
            geometryMapping = geometryMapping.replace("\"", "");    
        }
        if (geometryMapping != null) {
            columnsByTable.computeIfAbsent(geometryTable, __ -> new HashSet<>()).add(new ColumnAlias(geometryMapping, geometryAlias));
        }

        String[] properties = cfg.getMultiple(p + "properties");
        if (properties == null || properties.length == 0 || "*".equals(properties[0])) {
            Set<String> reserved = new HashSet<>();
            if (idMappings.length == 1) {
                reserved.add(idMappings[0]);
            }
            if (geometryMapping != null) {
                reserved.add(geometryMapping);
            }
            for (int i = 1; i < properties.length; i++) {
                if (properties[i].charAt(0) == '~' && properties[i].length() > 1) {
                    reserved.add(properties[i].substring(1));
                }
            }
            properties = discoverProperties(ds, dbSchema, primaryTable, column -> !reserved.contains(column));
        }

        for (String property : properties) {
            String table = cfg.get(p + "properties." + property + ".table", primaryTable);
            String mapping = cfg.get(p + "properties." + property + ".mapping", property);
            String alias = cfg.get(p + "properties." + property + ".alias", null);
            mapping = mapping.replace("\"", "");
            ColumnAlias ca = new ColumnAlias(mapping, alias);
            if (!HakunaConfigParser.isStaticMapping(mapping)) {
                columnsByTable.computeIfAbsent(table, __ -> new HashSet<>()).add(ca);
            }
        }

        Map<String, Map<String, List<HakunaPropertyType>>> propertyTypes = new HashMap<>();
        Map<String, Map<String, Boolean>> propertyNullability = new HashMap<>();
        for (Map.Entry<String, Set<ColumnAlias>> entry : columnsByTable.entrySet()) {
            String table = entry.getKey();
            Set<ColumnAlias> columns = entry.getValue();
            String select = getSelectSchema(dbSchema, table, columns);
            Map<String, List<HakunaPropertyType>> tablePropertyTypes = getPropertyTypes(ds, select);
            propertyTypes.put(table, tablePropertyTypes);
            Map<String, Boolean> propertyNullable = getNullability(ds, select);
            propertyNullability.put(table, propertyNullable);
        }

        HakunaProperty idProperty;
        if (idMappings.length == 1) {
            // Simple
            String table = idTables.length > 0 ? idTables[0] : primaryTable;
            String mapping = idMappings[0];
            String alias = idAliases.length > 0 ? idAliases[0] : mapping;
            HakunaPropertyType idColumnType = propertyTypes.get(table).get(alias).get(0);
            boolean nullable = propertyNullability.get(table).get(alias);
            HakunaPropertyWriter propWriter = HakunaPropertyWriters.getIdPropertyWriter(ft, ft.getName(), "id", idColumnType);
            switch (idColumnType) {
            case INT:
                idProperty = new HakunaPropertyInt("id", table, mapping, nullable, true, propWriter);
                break;
            case LONG:
                idProperty = new HakunaPropertyLong("id", table, mapping, nullable, true, propWriter);
                break;
            case STRING:
                idProperty = new HakunaPropertyString("id", table, mapping, nullable, true, propWriter);
                break;
            case UUID:
                idProperty = new HakunaPropertyUUID("id", table, mapping, nullable, true, propWriter);
                break;
            default:
                throw new IllegalArgumentException("Invalid id type");
            }
        } else {
            // Composite
            List<HakunaProperty> parts = new ArrayList<>();
            for (int i = 0; i < idMappings.length; i++) {
                String table = idTables.length > i ? idTables[i] : primaryTable;
                String mapping = idMappings[i];
                String alias = idAliases.length > 0 ? idAliases[0] : mapping;
                boolean nullable = propertyNullability.get(table).get(alias);
                if (HakunaConfigParser.isStaticMapping(mapping)) {
                    parts.add(HakunaPropertyStatic.create("id", "table", mapping.substring(1, mapping.length() - 1)));
                } else {
                    List<HakunaPropertyType> typeChain = propertyTypes.get(table).get(alias);
                    boolean unique = false;
                    boolean hidden = true;
                    parts.add(getDynamicProperty(cfg, HakunaConfigParser.REF_ID_PROP, table, mapping, typeChain, nullable, unique, hidden));
                }
            }
            String transformerClass = cfg.get(p + "id.transformer");
            String transformerArg = cfg.get(p + "id.transformer.arg");
            ValueTransformer vt = cfg.instantiateTransformer(transformerClass);
            HakunaPropertyWriter propWriter = HakunaPropertyWriters.getIdPropertyWriter(ft, ft.getName(), "id", vt.getPublicType());
            idProperty = new HakunaPropertyComposite("id", parts, vt, true, propWriter);
            vt.init(idProperty, transformerArg);
        }
        ft.setId(idProperty);

        if (geometryMapping != null) {
            boolean isDefaultGeometry = true;
            boolean nullable = propertyNullability.get(geometryTable).get(geometryMapping);
            boolean hidden = false;
            HakunaPropertyGeometry geometryProperty = getGeometryColumn(ds, dbSchema, geometryTable, "geometry",
                    geometryMapping, srids, sridStorage, isDefaultGeometry, nullable, hidden);
            ft.setGeom(geometryProperty);
        }

        List<HakunaProperty> hakunaProps = new ArrayList<>();
        for (String property : properties) {
            String propertyPrefix = p + "properties." + property + ".";
            String mapping = cfg.get(propertyPrefix + "mapping", property);
            mapping = mapping.replace("\"", "");
            String[] enumeration = cfg.getMultiple(propertyPrefix + "enum");
            String transformerClass = cfg.get(propertyPrefix + "transformer");
            String transformerArg = cfg.get(propertyPrefix + "transformer.arg");
            boolean unique = Boolean.parseBoolean(cfg.get(propertyPrefix + "unique", "false"));
            boolean hidden = Boolean.parseBoolean(cfg.get(propertyPrefix + "hidden", "false"));
            String table = cfg.get(p + "properties." + property + ".table", primaryTable);
            String alias = cfg.get(p + "properties." + property + ".alias", mapping);

            if (HakunaConfigParser.isStaticMapping(mapping)) {
                String value = mapping.substring(1, mapping.length() - 1);
                hakunaProps.add(HakunaPropertyStatic.create(property, table, value));
                continue;
            }

            Map<String, List<HakunaPropertyType>> tableTypes = propertyTypes.get(table);
            List<HakunaPropertyType> type = tableTypes.get(alias);
            if (type == null) {
                throw new IllegalArgumentException(
                        "Property " + property + " with mapping " + mapping + " can't find type from table " + table);
            }

            HakunaProperty hakunaProperty;
            if (type.size() == 1 && type.get(0) == HakunaPropertyType.GEOMETRY) {
                boolean isDefaultGeometry = false;
                boolean nullable = propertyNullability.get(table).get(mapping);
                hakunaProperty = getGeometryColumn(ds, dbSchema, table, property, mapping, srids, sridStorage,
                        isDefaultGeometry, nullable, hidden);
            } else {
                boolean nullable = propertyNullability.get(table).get(mapping);
                hakunaProperty = getDynamicProperty(cfg, property, table, mapping, type, nullable, unique, hidden);
                if (transformerClass != null) {
                    ValueTransformer transformer = cfg.instantiateTransformer(transformerClass);
                    hakunaProperty = new HakunaPropertyTransformed(hakunaProperty, transformer);
                    transformer.init(hakunaProperty, transformerArg);
                }
                if (enumeration.length > 0) {
                    if (hakunaProperty.getType() == HakunaPropertyType.INT
                            || hakunaProperty.getType() == HakunaPropertyType.LONG) {
                        hakunaProperty = new HakunaPropertyNumberEnum(hakunaProperty,
                                Arrays.stream(enumeration).map(Long::parseLong).collect(Collectors.toSet()));
                    } else if (hakunaProperty.getType() == HakunaPropertyType.STRING) {
                        hakunaProperty = new HakunaPropertyStringEnum(hakunaProperty,
                                Arrays.stream(enumeration).collect(Collectors.toSet()));
                    } else {
                        // Log the fact that we are ignoring this
                    }
                }
            }
            hakunaProps.add(hakunaProperty);
        }
        ft.setProperties(hakunaProps);

        ft.setPaginationStrategy(getPaginationStrategy(cfg, p, ft));

        return ft;
    }

    protected String[] discoverProperties(DataSource ds, String schema, String table, Predicate<String> check) throws SQLException {
        try (Connection c = ds.getConnection()) {
            DatabaseMetaData md = c.getMetaData();
            try (ResultSet rs = md.getColumns(null, schema, table, null)) {
                List<String> columns = new ArrayList<>();
                while (rs.next()) {
                    String column = rs.getString(4);
                    if (check.test(column)) {
                        columns.add(column);
                    }
                }
                return columns.toArray(new String[0]);
            }
        }
    }

    public static Map<String, Boolean> getNullability(DataSource ds, String select) throws SQLException {
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(select)) {
            LOG.debug(ps.toString());
            Map<String, Boolean> labelToNullable = new LinkedHashMap<>();
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                ResultSetMetaData rsmd = rs.getMetaData();
                int numCols = rsmd.getColumnCount();
                for (int i = 1; i <= numCols; i++) {
                    String label = rsmd.getColumnLabel(i);
                    boolean nullable = rsmd.isNullable(i) != ResultSetMetaData.columnNoNulls;
                    labelToNullable.put(label, nullable);
                }
            }
            return labelToNullable;
        }
    }

    protected static List<HakunaPropertyType> parseArrayType(Class<?> cls) {
        List<HakunaPropertyType> chain = new ArrayList<>();
        while (cls.isArray()) {
            chain.add(HakunaPropertyType.ARRAY);
            cls = cls.getComponentType();
        }
        if (cls.equals(Short.class) || cls.equals(Integer.class)) {
            chain.add(HakunaPropertyType.INT);
        } else if (cls.equals(Long.class) || cls.equals(BigInteger.class)) {
            chain.add(HakunaPropertyType.LONG);
        } else if (cls.equals(Float.class)) {
            chain.add(HakunaPropertyType.FLOAT);
        } else if (cls.equals(Double.class) || cls.equals(BigDecimal.class)) {
            chain.add(HakunaPropertyType.DOUBLE);
        } else if (cls.equals(Boolean.class)) {
            chain.add(HakunaPropertyType.BOOLEAN);
        } else if (cls.equals(String.class)) {
            chain.add(HakunaPropertyType.STRING);
        } else {
            throw new IllegalArgumentException("Unsupported array type from class!");
        }
        return chain;
    }

    protected static HakunaPropertyType parseArrayType(String typeName) {
        switch (typeName) {
        case "_bool":
            return HakunaPropertyType.BOOLEAN;
        case "_int2":
        case "_int4":
            return HakunaPropertyType.INT;
        case "_int8":
            return HakunaPropertyType.LONG;
        case "_float4":
            return HakunaPropertyType.FLOAT;
        case "_numeric":
        case "_float8":
            return HakunaPropertyType.DOUBLE;
        case "_varchar":
        case "_text":
            return HakunaPropertyType.STRING;
        }
        throw new IllegalArgumentException("Can not determine array type from " + typeName);
    }

    protected PaginationStrategy getPaginationStrategy(HakunaConfigParser cfg, String p, SimpleFeatureType sft) {
        String paginationStrategy = cfg.get(p + "pagination.strategy", "cursor").toLowerCase();
        switch (paginationStrategy) {
        case "cursor":
            return getPaginationCursor(cfg, p, sft);
        case "offset":
            return PaginationStrategyOffset.INSTANCE;
        default:
            throw new IllegalArgumentException("Unknown pagination strategy " + paginationStrategy);
        }
    }

    protected PaginationStrategyCursor getPaginationCursor(HakunaConfigParser cfg, String p, SimpleFeatureType sft) {
        String[] pagination = cfg.getMultiple(p + "pagination");
        String[] paginationOrder = cfg.getMultiple(p + "pagination.order");
        // int maxGroupSize = Integer.parseInt(get(p + "pagination.maxGroupSize", "1"));

        if (pagination.length == 0) {
            HakunaProperty id = new HakunaPropertyHidden(sft.getId());
            boolean asc = true;
            return new PaginationStrategyCursor(
                    Collections.singletonList(id),
                    Collections.singletonList(asc)
                    );
        } else {
            List<HakunaProperty> props = new ArrayList<>(pagination.length);
            List<Boolean> ascending = new ArrayList<>(pagination.length);
            for (int i = 0; i < pagination.length; i++) {
                HakunaProperty prop = cfg.getProperty(sft, pagination[i]);
                HakunaProperty hidden = new HakunaPropertyHidden(prop);
                Boolean asc = paginationOrder.length > i ? !"DESC".equalsIgnoreCase(paginationOrder[i]) : true;
                props.add(hidden);
                ascending.add(asc);
            }
            return new PaginationStrategyCursor(props, ascending);
        }
    }

    public Map<String, List<HakunaPropertyType>> getPropertyTypes(DataSource ds, String select) throws SQLException {
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(select)) {
            LOG.debug(ps.toString());
            Map<String, List<HakunaPropertyType>> propertyTypes = new LinkedHashMap<>();
            try (ResultSet rs = ps.executeQuery()) {
                boolean hasRow = rs.next();
                ResultSetMetaData rsmd = rs.getMetaData();
                int numCols = rsmd.getColumnCount();
                for (int i = 1; i <= numCols; i++) {
                    String label = rsmd.getColumnLabel(i);
                    int type = rsmd.getColumnType(i);
                    rsmd.isNullable(i);
                    String typeName = rsmd.getColumnTypeName(i);
                    HakunaPropertyType hakunaType = fromJDBCType(type, typeName);
                    if (hakunaType == null) {
                        throw new IllegalArgumentException("Unknown type " + type + " column " + label);
                    }
                    if (hakunaType != HakunaPropertyType.ARRAY) {
                        propertyTypes.put(label, Collections.singletonList(hakunaType));    
                    } else {
                        List<HakunaPropertyType> chain = null;
                        if (hasRow) {
                            Array array = rs.getArray(i);
                            if (array != null) {
                                Object actualArray = array.getArray();
                                Class<?> cls = actualArray.getClass();
                                array.free();
                                chain = parseArrayType(cls);
                            }
                        }
                        if (chain == null) {
                            chain = Arrays.asList(HakunaPropertyType.ARRAY, parseArrayType(typeName));
                        }
                        propertyTypes.put(label, chain);
                    }
                }
            }
            return propertyTypes;
        }
    }

    public HakunaPropertyType fromJDBCType(int columnType, String columnTypeName) {
        
        if("json".equals(columnTypeName)||"jsonb".equals(columnTypeName)) {
            return HakunaPropertyType.JSON;
        }
        
        switch (columnType) {
        case java.sql.Types.BIT:
        case java.sql.Types.BOOLEAN:
            return HakunaPropertyType.BOOLEAN;
        case java.sql.Types.TINYINT:
        case java.sql.Types.SMALLINT:
        case java.sql.Types.INTEGER:
            return HakunaPropertyType.INT;
        case java.sql.Types.BIGINT:
            return HakunaPropertyType.LONG;
        case java.sql.Types.REAL:
            return HakunaPropertyType.FLOAT;
        case java.sql.Types.FLOAT:
        case java.sql.Types.DECIMAL:
        case java.sql.Types.DOUBLE:
        case java.sql.Types.NUMERIC:
            return HakunaPropertyType.DOUBLE;
        case java.sql.Types.CHAR:
        case java.sql.Types.NCHAR:
        case java.sql.Types.VARCHAR:
        case java.sql.Types.NVARCHAR:
            return HakunaPropertyType.STRING;
        case java.sql.Types.DATE:
            return HakunaPropertyType.DATE;
        case java.sql.Types.TIMESTAMP:
            switch (columnTypeName) {
            case "timestamptz":
                return HakunaPropertyType.TIMESTAMPTZ;
            default:
                return HakunaPropertyType.TIMESTAMP;
            }
        case java.sql.Types.OTHER:
            switch (columnTypeName) {
            case "uuid":
                return HakunaPropertyType.UUID;
            case "geometry":
                return HakunaPropertyType.GEOMETRY;
            default:
                return null;
            }
        case java.sql.Types.ARRAY:
            return HakunaPropertyType.ARRAY;
        default:
            return null;
        }
    }


    protected int getSrid(String table, String column, int sridStorage, int dbSrid) {
        if (dbSrid <= 0 && sridStorage <= 0) {
            throw new IllegalArgumentException(String.format("For table %s column %s srid is unknown!", table, column));
        } else if (dbSrid <= 0) {
            return sridStorage;
        } else {
            if (sridStorage > 0) {
                LOG.warn("For table {} column {} using srid {} from database instead of configured {}", table, column, dbSrid, sridStorage);
            }
            return dbSrid;
        }
    }

    protected static String unquote(String column) {
        if (column == null || column.isEmpty()) {
            return column;
        }
        if (column.charAt(0) == '"') {
            return column.substring(1, column.length() - 1);
        }
        return column;
    }

    protected HakunaProperty getDynamicProperty(HakunaConfigParser cfg, String name, String table, String column,
            List<HakunaPropertyType> typeChain, boolean nullable, boolean unique, boolean hidden) {
        return cfg.getDynamicProperty(name, table, column, typeChain, nullable, unique, hidden);
    }


    protected final class ColumnAlias {

        public final String column;
        public final String alias;
        public final boolean escape;

        public ColumnAlias(String column, String alias) {
            this(column, alias, true);
        }

        public ColumnAlias(String column, String alias, boolean escape) {
            this.column = column;
            this.alias = alias;
            this.escape = escape;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((alias == null) ? 0 : alias.hashCode());
            result = prime * result + ((column == null) ? 0 : column.hashCode());
            result = prime * result + (escape ? 1231 : 1237);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ColumnAlias other = (ColumnAlias) obj;
            if (alias == null) {
                if (other.alias != null)
                    return false;
            } else if (!alias.equals(other.alias))
                return false;
            if (column == null) {
                if (other.column != null)
                    return false;
            } else if (!column.equals(other.column))
                return false;
            if (escape != other.escape)
                return false;
            return true;
        }

    }

    protected Join parseJoin(HakunaConfigParser cfg, String join) {
        String propertyPrefix = "joins." + join + ".";
        String type = cfg.get(propertyPrefix + "type");
        String leftTable = cfg.get(propertyPrefix + "leftTable");
        String leftColumn = cfg.get(propertyPrefix + "leftColumn");
        String rightTable = cfg.get(propertyPrefix + "rightTable");
        String rightColumn = cfg.get(propertyPrefix + "rightColumn");
        String joinTable = cfg.get(propertyPrefix + "joinTable");
        String joinColumnLeft = cfg.get(propertyPrefix + "joinColumnLeft");
        String joinColumnRight = cfg.get(propertyPrefix + "joinColumnRight");
        switch (type.toLowerCase()) {
        case "onetoone":
            return new OneToOneJoin(leftTable, leftColumn, rightTable, rightColumn);
        case "onetomany":
            return new OneToManyJoin(leftTable, leftColumn, rightTable, rightColumn);
        case "manytoone":
            return new ManyToOneJoin(leftTable, leftColumn, rightTable, rightColumn);
        case "manytomany":
            return new ManyToManyJoin(leftTable, leftColumn, rightTable, rightColumn, joinTable, joinColumnLeft,
                    joinColumnRight);
        default:
            throw new IllegalArgumentException("Invalid join type " + type + " for join " + join);
        }
    }

    protected String getSelectSchema(String schema, String table, Set<ColumnAlias> columns) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        for (ColumnAlias col : columns) {
            if (col.escape) {
                sb.append('"').append(col.column).append('"');
            } else {
                sb.append(col.column);
            }
            if (col.alias != null) {
                sb.append(" AS ");
                sb.append('"').append(col.alias).append('"');
            }
            sb.append(',');
        }
        sb.setLength(sb.length() - 1);
        sb.append(" FROM ");
        sb.append('"').append(schema).append('"').append('.');
        sb.append('"').append(table).append('"');
        sb.append(" LIMIT 1");
        return sb.toString();
    }

}
