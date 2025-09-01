package fi.nls.hakunapi.source;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.nls.hakunapi.core.FeatureTypeSchema;
import fi.nls.hakunapi.core.config.HakunaConfigParser;
import fi.nls.hakunapi.core.geom.HakunaGeometryDimension;
import fi.nls.hakunapi.core.geom.HakunaGeometryType;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyType;
import fi.nls.hakunapi.core.property.HakunaPropertyWriter;
import fi.nls.hakunapi.core.property.HakunaPropertyWriters;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyInt;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyLong;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyString;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyUUID;

public class HakunaTestSourceSchemaParser {

	protected static final Logger LOG = LoggerFactory.getLogger(HakunaTestSourceSchemaParser.class);

	protected static HakunaPropertyType determineIdType(String value) {
		return HakunaPropertyType.valueOf(value);
	}

	protected static HakunaPropertyType determinePropertyType(String value) {
		return HakunaPropertyType.valueOf(value);
	}

	protected static HakunaGeometryType determineGeometryType(String type) {
		if (type == null) {
			return null;
		}
		return HakunaGeometryType.valueOf(type);
	}

	public FeatureTypeSchema parseSchema(HakunaConfigParser cfg, String p, final HakunaTestSourceFeatureType ft,
			String collectionId) throws IOException {

		LOG.info("Parsing " + collectionId);

		HakunaPropertyType idType = null;
		HakunaGeometryType geometryType = null;
		Map<String, HakunaPropertyType> propertyTypes = new LinkedHashMap<>();

		String title = cfg.get(p + "title", ft.getName());
		ft.setTitle(title);
		String desc = cfg.get(p + "description", ft.getTitle());
		ft.setDescription(desc);

		idType = determineIdType(cfg.get(p + "id.type", "STRING"));

		geometryType = determineGeometryType(cfg.get(p + "geometry.type"));

		String[] publicProps = cfg.getMultiple(p + "properties");
		LOG.info("Parsing properties " + Stream.of(publicProps).collect(Collectors.joining(",")));

		for (String prop : publicProps) {
			LOG.info("- property " + prop);
			String rawtype = cfg.get(p + "properties." + prop + ".type","STRING");
			boolean isArray = rawtype.trim().endsWith("[]");
			if (isArray) {
				throw new IOException("NYI");
			}
			String type = rawtype;
			propertyTypes.put(prop, determinePropertyType(type));
			LOG.info("- property " + prop + " OK : " + type + (isArray ? "[]" : ""));
		}

		String[] parameterProps = cfg.getMultiple(p + "parameters");

		LOG.info("Parsing parameters " + Stream.of(parameterProps).collect(Collectors.joining(",")));
		for (String prop : parameterProps) {

			if (propertyTypes.containsKey(prop)) {
				continue;
			}
			LOG.info("- parameter " + prop);
			String type = cfg.get(p + "parameters." + prop + ".type");
			propertyTypes.put(prop, determinePropertyType(type));
			LOG.info("- parameter " + prop + " OK : " + type);

		}

		String[] queryableProps = cfg.getMultiple(p + "queryables");
		LOG.info("Parsing queryableProps " + Stream.of(queryableProps).collect(Collectors.joining(",")));

		for (String prop : queryableProps) {

			if (propertyTypes.containsKey(prop)) {
				continue;
			}
			LOG.info("- queryable " + prop);
			String type = cfg.get(p + "queryables." + prop + ".type");
			propertyTypes.put(prop, determinePropertyType(type));
			LOG.info("- queryable " + prop + " OK : " + type);

		}
		
		

		LOG.info("Parsing " + collectionId + " success");

		return new FeatureTypeSchema(idType, geometryType, propertyTypes);

	}

	protected HakunaProperty toHakunaProperty(HakunaConfigParser cfg, String p, HakunaTestSourceFeatureType ft,
			String name, HakunaPropertyType propType, FeatureTypeSchema schema) {

		boolean hidden = "true".equals(cfg.get(p + "properties." + name + ".hidden", "false"));

		String table = ft.getName();
		String mapping = name;
		boolean nullable = true;
		boolean unique = false;

		if (propType == HakunaPropertyType.JSON) {
			throw new RuntimeException("NYI");

		} else if (propType == HakunaPropertyType.GEOMETRY) {
			String geomTypeVal = cfg.get(p + "properties." + name + ".geometry.type");
			if (geomTypeVal == null) {
				geomTypeVal = cfg.get(p + "parameters." + name + ".geometry.type");
			}
			if (geomTypeVal == null) {
				geomTypeVal = cfg.get(p + "queryables." + name + ".geometry.type");
			}
			HakunaGeometryType geometryType = HakunaGeometryType.valueOf(geomTypeVal);
			nullable = true;
			mapping = name;
			int[] srid = ft.getSrid();
			int storageSRID = ft.getStorageSrid();
			int dimension = dimFromGeomDimension(ft.getGeomDimension());

			boolean isDefault = false;
			HakunaPropertyWriter propWriter = hidden ? HakunaPropertyWriters.HIDDEN
					: HakunaPropertyWriters.getGeometryPropertyWriter(mapping, isDefault);
			return new HakunaPropertyGeometry(name, table, mapping, nullable, geometryType, srid, storageSRID,
					dimension, propWriter);
		}

		return cfg.getDynamicProperty(name, table, mapping, Collections.singletonList(propType), nullable, unique,
				hidden);
	}

	protected static HakunaProperty getFeaturesIdProperty(HakunaTestSourceFeatureType ft, FeatureTypeSchema schema) {
		String table = ft.getName();
		String mapping = null;
		boolean nullable = true;
		boolean unique = false;
		HakunaPropertyWriter propWriter = HakunaPropertyWriters.getIdPropertyWriter(ft, ft.getName(), "id",
				schema.getIdType());
		switch (schema.getIdType()) {
		case INT:
			return new HakunaPropertyInt(HakunaConfigParser.REF_ID_PROP, table, mapping, nullable, unique, propWriter);
		case LONG:
			return new HakunaPropertyLong(HakunaConfigParser.REF_ID_PROP, table, mapping, nullable, unique, propWriter);
		case STRING:
			return new HakunaPropertyString(HakunaConfigParser.REF_ID_PROP, table, mapping, nullable, unique,
					propWriter);
		case UUID:
			return new HakunaPropertyUUID(HakunaConfigParser.REF_ID_PROP, table, mapping, nullable, unique, propWriter);
		default:
			throw new IllegalArgumentException("Invalid id type");
		}
	}

	protected HakunaPropertyGeometry getFeaturesGeomProperty(HakunaTestSourceFeatureType ft, FeatureTypeSchema schema,
			HakunaGeometryType geometryType, int[] srid) {
		String table = ft.getName();
		String mapping = "geometry";
		boolean nullable = false;
		int storageSRID = ft.getStorageSrid();
		boolean isDefault = true;
		HakunaPropertyWriter propWriter = HakunaPropertyWriters.getGeometryPropertyWriter(mapping, isDefault);
		return new HakunaPropertyGeometry("geometry", table, mapping, nullable, geometryType, srid, storageSRID,
				dimFromGeomDimension(ft.getGeomDimension()), propWriter);
	}

	protected static int dimFromGeomDimension(HakunaGeometryDimension dim) {
		switch (dim) {
        case XY:
            return 2;
		case XYZ:
			return 3;
		case XYZM:
			return 4;
		default:
			throw new IllegalArgumentException("Unknown dimension " + dim);
		}
	}

}
