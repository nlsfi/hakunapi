package fi.nls.hakunapi.source.gpkg;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
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
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.locationtech.jts.geom.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.JDBC;
import org.sqlite.SQLiteConnection;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.HakunapiPlaceholder;
import fi.nls.hakunapi.core.PaginationStrategy;
import fi.nls.hakunapi.core.PaginationStrategyCursor;
import fi.nls.hakunapi.core.PaginationStrategyOffset;
import fi.nls.hakunapi.core.SimpleFeatureType;
import fi.nls.hakunapi.core.SimpleSource;
import fi.nls.hakunapi.core.config.HakunaConfigParser;
import fi.nls.hakunapi.core.geom.HakunaGeometryType;
import fi.nls.hakunapi.core.property.HakunaProperty;
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
import fi.nls.hakunapi.core.transformer.ValueTransformer;
import fi.nls.hakunapi.gpkg.GPKGFeaturesTable;
import fi.nls.hakunapi.gpkg.GPKGGeometryColumn;
import fi.nls.hakunapi.gpkg.GPKGGeometryColumn.GeometryTypeName;
import fi.nls.hakunapi.gpkg.extension.RtreeIndexExtension;
import fi.nls.hakunapi.gpkg.function.ST_IsEmpty;
import fi.nls.hakunapi.gpkg.function.ST_MaxX;
import fi.nls.hakunapi.gpkg.function.ST_MaxY;
import fi.nls.hakunapi.gpkg.function.ST_MinX;
import fi.nls.hakunapi.gpkg.function.ST_MinY;

public class GpkgSimpleSource implements SimpleSource {

    private static final Logger LOG = LoggerFactory.getLogger(GpkgSimpleSource.class);
    private static final String DEFAULT_DRIVER_CLASS = JDBC.class.getName();

    private List<HikariDataSource> dataSourcesToClose = new ArrayList<>();
    private Map<String, DataSource> dataSources = new HashMap<>();

    @Override
    public String getType() {
        return "gpkg";
    }

    private DataSource getDataSource(HakunaConfigParser cfg, Path path, String name) throws SQLException {
        DataSource ds = dataSources.get(name);
        if (ds == null) {
            HikariDataSource hds = readDataSource(cfg, path, name);
            dataSourcesToClose.add(hds);
            ds = hds;
            try (Connection c = ds.getConnection()) {
                SQLiteConnection sqlitec = c.unwrap(SQLiteConnection.class);
                new ST_IsEmpty().create(sqlitec);
                new ST_MinX().create(sqlitec);
                new ST_MinY().create(sqlitec);
                new ST_MaxX().create(sqlitec);
                new ST_MaxY().create(sqlitec);
            }
            dataSources.put(name, ds);
        }
        return ds;
    }

    private HikariDataSource readDataSource(HakunaConfigParser cfg, Path path, String name) {
        HikariConfig config;

        File file = getFile(name, path);
        if (file.exists()) {
            if (!file.canRead()) {
                throw new IllegalArgumentException("Can not read file " + file.getAbsolutePath());
            }
            config = new HikariConfig();
            config.setPoolName(getPoolName(file.getName()));
            config.setDriverClassName(DEFAULT_DRIVER_CLASS);
            config.setJdbcUrl("jdbc:sqlite:" + file.getAbsolutePath());
            config.setConnectionTestQuery("SELECT 1");
            config.setMaxLifetime(60_000);
            config.setIdleTimeout(45_000);
            config.setMaximumPoolSize(50);
        } else {
            final Properties props;
            if (Arrays.stream(cfg.getMultiple("db", new String[0])).anyMatch(name::equals)) {
                // if name appears in db=a,b,c,d listing then consider it's configuration to be inlined
                props = getInlinedDBProperties(cfg, name);
            } else {
                // Not inlined, check separate file $name.properties
                String dataSourcePath = getDataSourcePath(path, name);
                props = loadProperties(dataSourcePath);
                props.putIfAbsent("driverClassName", DEFAULT_DRIVER_CLASS);
            }
            config = new HikariConfig(props);
        }

        return new HikariDataSource(config);
    }

    private File getFile(String name, Path configPath) {
        File file = new File(name);
        return file.isAbsolute() ? file : new File(configPath.getParent().toFile(), name);
    }

    private String getPoolName(String name) {
        int i = name.lastIndexOf('.');
        if (i > 0) {
            name = name.substring(0, i);
        }
        return name + "SQLitePool";
    }

    protected static Properties getInlinedDBProperties(HakunaConfigParser cfg, String name) {
        Properties props = new Properties();
        cfg.getAllStartingWith("db." + name + ".").forEach(props::setProperty);
        props.putIfAbsent("driverClassName", DEFAULT_DRIVER_CLASS);
        return props;
    }

    protected static String getDataSourcePath(Path path, String name) {
        String prefix = "";
        Path parent = path.getParent();
        if (parent != null) {
            prefix = path.getParent().toString() + "/";
        }
        return prefix + name + ".properties";
    }

    protected Properties loadProperties(String path) {
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
        GpkgFeatureType ft = new GpkgFeatureType();
        ft.setName(collectionId);

        String db = cfg.get(p + "db");
        DataSource ds = getDataSource(cfg, path, db);
        if (ds == null) {
            throw new IllegalArgumentException("Unknown db: " + db + " collection: " + collectionId);
        }
        ft.setDatabase(ds);

        String cfgTable = cfg.get(p + "table", collectionId);
        if (cfgTable == null || cfgTable.isEmpty()) {
            throw new IllegalArgumentException("Missing required property " + (p + "table"));
        }
        String table = cfgTable.replace("\"", "");
        ft.setTable(table);

        GPKGFeaturesTable contents = getFeatureContents(ds, table)
                .orElseThrow(() -> new IllegalArgumentException("Could not find entry from gpkg_contents with table_name: " + table + " and data_type: features"));
        ft.setTitle(contents.getIdentifier());
        ft.setDescription(contents.getDescription());

        // TODO: Transform these to CRS84
        // ft.setSpatialExtent(new double[] { contents.getMinX(), contents.getMinY(), contents.getMaxX(), contents.getMaxY() );

        int cfgSrid = Integer.parseInt(cfg.get(p + "srid.storage", "0"));

        List<String> primaryKeys = getPrimaryKeys(ds, table);
        if (primaryKeys.size() != 1) {
            throw new IllegalArgumentException("Failed to find single primary key for table: " + table);
        }

        String idMapping = cfg.get(p + "id.mapping");
        if (idMapping == null) {
            idMapping = primaryKeys.get(0);
        }

        // Each features table has exactly one geometry column
        GPKGGeometryColumn geometryColumn = discoverGeometryColumn(ds, table)
                .orElseThrow(() -> new IllegalArgumentException("Could not find entry from gpkg_geometry_columns with table_name: " + table));

        String[] properties = cfg.getMultiple(p + "properties");
        if (properties == null || properties.length == 0 || "*".equals(properties[0])) {
            Set<String> reserved = new HashSet<>();
            reserved.add(idMapping);
            reserved.add(geometryColumn.getColumn());
            for (int i = 1; i < properties.length; i++) {
                if (properties[i].charAt(0) == '~' && properties[i].length() > 1) {
                    reserved.add(properties[i].substring(1));
                }
            }
            properties = discoverProperties(ds, table, column -> !reserved.contains(column)).toArray(new String[0]);
        }

        List<String> columns = new ArrayList<>();
        columns.add(idMapping);
        for (String property : properties) {
            String mapping = cfg.get(p + "properties." + property + ".mapping", property);
            mapping = mapping.replace("\"", "");
            if (!HakunaConfigParser.isStaticMapping(mapping)) {
                columns.add(mapping);
            }
        }

        Map<String, HakunaPropertyType> propertyTypes = getPropertyTypes(ds, getSelectSchema(table, columns));

        columns.add(geometryColumn.getColumn());
        Map<String, Boolean> propertyNullability = getNullability(ds, getSelectSchema(table, columns));

        ft.setId(getIdProperty(ft, table, idMapping, propertyTypes, propertyNullability));
        ft.setGeom(getGeometryProperty(geometryColumn, srids, cfgSrid, propertyNullability));
        ft.setSpatialIndex(hasGeometryIndex(ft.getGeom()));

        List<HakunaProperty> hakunaProperties = new ArrayList<>();
        for (String property : properties) {
            hakunaProperties.add(getProperty(cfg, ft, p, table, property, propertyTypes, propertyNullability));
        }
        ft.setProperties(hakunaProperties);

        ft.setPaginationStrategy(getPaginationStrategy(cfg, p, ft));

        return ft;
    }

    private List<String> getPrimaryKeys(DataSource ds, String table) throws SQLException {
        List<String> primaryKeys = new ArrayList<>();

        try (Connection c = ds.getConnection()) {
            DatabaseMetaData meta = c.getMetaData();
            try (ResultSet rs = meta.getPrimaryKeys(null, null, table)) {
                while (rs.next()) {
                    primaryKeys.add(rs.getString(4)); // COLUMN_NAME
                }
            }
        }

        return primaryKeys;
    }

    private Optional<GPKGFeaturesTable> getFeatureContents(DataSource ds, String table) throws SQLException {
        String select = "SELECT identifier, description, min_x, min_y, max_x, max_y, srs_id FROM gpkg_contents WHERE data_type=? AND table_name=?";
        try (Connection c = ds.getConnection();
                PreparedStatement ps = c.prepareStatement(select)) {
            ps.setString(1, GPKGFeaturesTable.DATA_TYPE);
            ps.setString(2, table);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                String identifier = rs.getString("identifier");
                String description = rs.getString("description");
                double minX = rs.getDouble("min_x");
                double minY = rs.getDouble("min_y");
                double maxX = rs.getDouble("max_x");
                double maxY = rs.getDouble("max_y");
                Envelope bbox = new Envelope(minX, maxX, minY, maxY);
                int srsId = rs.getInt("srs_id");
                return Optional.of(new GPKGFeaturesTable(table, identifier, description, bbox, srsId));
            }
        }
    }

    /**
     * Each features table has exactly one geometry column, a BLOB
     */
    private Optional<GPKGGeometryColumn> discoverGeometryColumn(DataSource ds, String table) throws SQLException {
        String select = "SELECT column_name, geometry_type_name, srs_id, z, m FROM gpkg_geometry_columns WHERE table_name=?";
        try (Connection c = ds.getConnection();
                PreparedStatement ps = c.prepareStatement(select)) {
            ps.setString(1, table);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                String column = rs.getString("column_name");
                String geometryTypeName = rs.getString("geometry_type_name");
                GeometryTypeName type = GeometryTypeName.valueOf(geometryTypeName.toUpperCase());
                int srid = rs.getInt("srs_id");
                boolean hasZ = rs.getInt("z") != 0;
                boolean hasM = rs.getInt("m") != 0;
                return Optional.of(new GPKGGeometryColumn(table, column, type, srid, hasZ, hasM));
            }
        }
    }

    private List<String> discoverProperties(DataSource ds, String table, Predicate<String> check) throws SQLException {
        try (Connection c = ds.getConnection()) {
            DatabaseMetaData md = c.getMetaData();
            try (ResultSet rs = md.getColumns(null, null, table, null)) {
                List<String> columns = new ArrayList<>();
                while (rs.next()) {
                    String column = rs.getString(4);
                    if (check.test(column)) {
                        columns.add(column);
                    }
                }
                return columns;
            }
        }
    }

    private HakunaProperty getIdProperty(FeatureType ft, String table, String idMapping, Map<String, HakunaPropertyType> propertyTypes,
            Map<String, Boolean> propertyNullability) {
        HakunaPropertyType idColumnType = propertyTypes.get(idMapping);
        boolean nullable = false;
        HakunaPropertyWriter propWriter = HakunaPropertyWriters.getIdPropertyWriter(ft, ft.getName(), "id", idColumnType);
        switch (idColumnType) {
        case INT:
            return new HakunaPropertyInt("id", table, idMapping, nullable, true, propWriter);
        case LONG:
            return new HakunaPropertyLong("id", table, idMapping, nullable, true, propWriter);
        case STRING:
            return new HakunaPropertyString("id", table, idMapping, nullable, true, propWriter);
        default:
            throw new IllegalArgumentException("Invalid id type");
        }
    }

    private HakunaPropertyGeometry getGeometryProperty(
            GPKGGeometryColumn geometryColumn,
            int[] srids,
            int cfgSrid,
            Map<String, Boolean> propertyNullability) throws SQLException {
        String table = geometryColumn.getTable();
        String column = geometryColumn.getColumn();
        String name = "geometry";
        boolean defaultGeometry = true;
        boolean nullable = propertyNullability.get(geometryColumn.getColumn());
        int dim = 2 + (geometryColumn.isHasZ() ? 1 : 0) + (geometryColumn.isHasM() ? 1 : 0);
        int srid = getSrid(table, column, cfgSrid, geometryColumn.getSrid());
        if (Arrays.stream(srids).noneMatch(it -> it == srid)) {
            throw new IllegalArgumentException(String.format(
                    "For table %s column %s storage srid is %d which is missing from configured srid list!", table, column, srid));
        }
        HakunaGeometryType geometryType = HakunaGeometryType.valueOf(geometryColumn.getGeometryTypeName().name());
        HakunaPropertyWriter propWriter = HakunaPropertyWriters.getGeometryPropertyWriter(name, defaultGeometry);
        return new HakunaPropertyGeometry(name, table, column, nullable, geometryType, srids, srid, dim, propWriter);
    }

    private int getSrid(String table, String column, int sridStorage, int dbSrid) {
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

    private boolean hasGeometryIndex(HakunaPropertyGeometry geom) throws SQLException {
        GpkgFeatureType ft = (GpkgFeatureType) geom.getFeatureType();
        try (Connection c = ft.getDatabase().getConnection();
                PreparedStatement ps = c.prepareStatement(
                        "SELECT COUNT(*) "
                                + "FROM gpkg_extensions "
                                + "WHERE extension_name = ? "
                                + "AND table_name = ? "
                                + "AND column_name = ?")) {
            ps.setString(1, RtreeIndexExtension.EXTENSION_NAME);
            ps.setString(2, geom.getTable());
            ps.setString(3, geom.getColumn());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) == 1;
            }
        }
    }

    private HakunaProperty getProperty(
            HakunaConfigParser cfg,
            GpkgFeatureType ft,
            String p,
            String table,
            String property,
            Map<String, HakunaPropertyType> propertyTypes,
            Map<String, Boolean> propertyNullability) throws Exception {
        String propertyPrefix = p + "properties." + property + ".";
        String mapping = cfg.get(propertyPrefix + "mapping", property);
        mapping = mapping.replace("\"", "");
        String[] enumeration = cfg.getMultiple(propertyPrefix + "enum");
        String transformerClass = cfg.get(propertyPrefix + "transformer");
        String transformerArg = cfg.get(propertyPrefix + "transformer.arg");
        boolean unique = Boolean.parseBoolean(cfg.get(propertyPrefix + "unique", "false"));
        boolean hidden = Boolean.parseBoolean(cfg.get(propertyPrefix + "hidden", "false"));
        String alias = mapping;

        if (HakunaConfigParser.isStaticMapping(mapping)) {
            String value = mapping.substring(1, mapping.length() - 1);
            return HakunaPropertyStatic.create(property, table, value);
        }

        HakunaPropertyType type = propertyTypes.get(alias);
        if (type == null) {
            throw new IllegalArgumentException(
                    "Property " + property + " with mapping " + mapping + " can't find type from table " + table);
        }

        boolean nullable = propertyNullability.get(mapping);
        HakunaProperty hakunaProperty = cfg.getDynamicProperty(property, table, mapping, Arrays.asList(type), nullable, unique, hidden);

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

        return hakunaProperty;
    }

    private String getSelectSchema(String table, Iterable<String> columns) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        for (String col : columns) {
            sb.append('"').append(col).append('"');
            sb.append(',');
        }
        sb.setLength(sb.length() - 1);
        sb.append(" FROM ");
        sb.append('"').append(table).append('"');
        sb.append(" LIMIT 1");
        return sb.toString();
    }

    private Map<String, HakunaPropertyType> getPropertyTypes(DataSource ds, String select) throws SQLException {
        Map<String, HakunaPropertyType> propertyTypes = new LinkedHashMap<>();
        try (Connection c = ds.getConnection();
                PreparedStatement ps = c.prepareStatement(select);
                ResultSet rs = ps.executeQuery()) {
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
                    throw new IllegalArgumentException("Unknown type: " + type + ", typeName: " + typeName + ", column: " + label);
                }
                propertyTypes.put(label, hakunaType);
            }
        }
        return propertyTypes;
    }

    private HakunaPropertyType fromJDBCType(int columnType, String columnTypeName) {
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
        default:
            return null;
        }
    }

    private Map<String, Boolean> getNullability(DataSource ds, String select) throws SQLException {
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(select)) {
            LOG.info(ps.toString());
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

    private PaginationStrategy getPaginationStrategy(HakunaConfigParser cfg, String p, SimpleFeatureType sft) {
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

    private PaginationStrategyCursor getPaginationCursor(HakunaConfigParser cfg, String p, SimpleFeatureType sft) {
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

}