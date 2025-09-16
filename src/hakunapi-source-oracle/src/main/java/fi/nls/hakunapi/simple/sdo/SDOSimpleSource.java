package fi.nls.hakunapi.simple.sdo;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import fi.nls.hakunapi.core.SimpleFeatureType;
import fi.nls.hakunapi.core.SimpleSource;
import fi.nls.hakunapi.core.config.HakunaConfigParser;
import fi.nls.hakunapi.core.geom.HakunaGeometryType;
import fi.nls.hakunapi.core.join.Join;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyComposite;
import fi.nls.hakunapi.core.property.HakunaPropertyNumberEnum;
import fi.nls.hakunapi.core.property.HakunaPropertyStatic;
import fi.nls.hakunapi.core.property.HakunaPropertyStringEnum;
import fi.nls.hakunapi.core.property.HakunaPropertyTransformed;
import fi.nls.hakunapi.core.property.HakunaPropertyType;
import fi.nls.hakunapi.core.property.HakunaPropertyWriter;
import fi.nls.hakunapi.core.property.HakunaPropertyWriters;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyDouble;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyInt;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyLong;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyString;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyUUID;
import fi.nls.hakunapi.core.transformer.ValueTransformer;
import fi.nls.hakunapi.simple.sdo.sql.SQLFeatureType;
import fi.nls.hakunapi.simple.sdo.sql.SQLSimpleSource;

public class SDOSimpleSource extends SQLSimpleSource implements SimpleSource {

	@Override
	public String getType() {
		return "ora";
	}

	@Override
	public SQLFeatureType createFeatureType() {
		return new SDOFeatureType();
	}

	@Override
	public SimpleFeatureType parse(HakunaConfigParser cfg, Path path, String collectionId, int[] srids)
	        throws Exception {
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
			columnsByTable.computeIfAbsent(geometryTable, __ -> new HashSet<>())
			        .add(new ColumnAlias(geometryMapping, geometryAlias));
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
			HakunaPropertyWriter propWriter = HakunaPropertyWriters.getIdPropertyWriter(ft, ft.getName(), "id",
			        idColumnType);
			switch (idColumnType) {
			case INT:
				idProperty = new HakunaPropertyInt("id", table, mapping, nullable, true, propWriter);
				break;
			case LONG:
				idProperty = new HakunaPropertyLong("id", table, mapping, nullable, true, propWriter);
				break;
			case DOUBLE:
				idProperty = new HakunaPropertyDouble("id", table, mapping, nullable, true, propWriter);
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
					parts.add(getDynamicProperty(cfg, HakunaConfigParser.REF_ID_PROP, table, mapping, typeChain,
					        nullable, unique, hidden));
				}
			}
			String transformerClass = cfg.get(p + "id.transformer");
			String transformerArg = cfg.get(p + "id.transformer.arg");
			ValueTransformer vt = cfg.instantiateTransformer(transformerClass);
			HakunaPropertyWriter propWriter = HakunaPropertyWriters.getIdPropertyWriter(ft, ft.getName(), "id",
			        vt.getPublicType());
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

		ft.setCaseInsensitiveStrategy(getCaseInsensitiveStrategy(cfg, p, ft));

		return ft;
	}

	protected HakunaPropertyGeometry getGeometryColumn(DataSource ds, String schema, String table, String name,
	        String column, int[] srids, int sridStorage, boolean isDefault, boolean nullable, boolean hidden)
	        throws SQLException {

		HakunaGeometryType geometryType = HakunaGeometryType.GEOMETRY;
		HakunaPropertyWriter propWriter = hidden ? HakunaPropertyWriters.HIDDEN
		        : HakunaPropertyWriters.getGeometryPropertyWriter(name, isDefault);
		int dim = 2;

		LOG.warn("FORCED GEOMETRY type " + geometryType + " dim " + dim);
		return new HakunaPropertyGeometry(name, table, column, nullable, geometryType, srids, sridStorage, dim,
		        propWriter);
	}

	public HakunaPropertyType fromJDBCType(int columnType, String columnTypeName) {
	        
	     /*   if("json".equals(columnTypeName)||"jsonb".equals(columnTypeName)) {
	            return HakunaPropertyType.JSON;
	        }
	       */ 
	        if(columnTypeName.equalsIgnoreCase("MDSYS.SDO_GEOMETRY")){
	        	return HakunaPropertyType.GEOMETRY;
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
		sb.append(" FETCH FIRST 1 ROWS ONLY");
		return sb.toString();
	}

}
