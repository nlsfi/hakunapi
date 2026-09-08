package fi.nls.hakunapi.source;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.nls.hakunapi.core.FeatureTypeSchema;
import fi.nls.hakunapi.core.PaginationStrategyOffset;
import fi.nls.hakunapi.core.SimpleFeatureType;
import fi.nls.hakunapi.core.SimpleSource;
import fi.nls.hakunapi.core.config.HakunaConfigParser;
import fi.nls.hakunapi.core.geom.HakunaGeometryJTS;
import fi.nls.hakunapi.core.geom.HakunaGeometryType;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyType;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;

public class HakunaTestSource implements SimpleSource {
	private static final Logger LOG = LoggerFactory.getLogger(HakunaTestSource.class);

	@Override
	public String getType() {
		return "test";
	}

	protected final HakunaTestSourceSchemaParser schemaParser = new HakunaTestSourceSchemaParser();

	protected final WKTReader wktReader = new WKTReader();

	@Override
	public SimpleFeatureType parse(HakunaConfigParser cfg, Path path, String collectionId, int[] srids)
			throws IOException {
		LOG.info("Parse GATEWAY collection " + collectionId);
		try {

			String defaultStorageSrid = cfg.get("default.storage.srid", "3067");

			String p = "collections." + collectionId + ".";

			int storageSrid = Integer.parseInt(cfg.get(p + ".storage.srid", defaultStorageSrid), 10);

			final HakunaTestSourceFeatureType ft = new HakunaTestSourceFeatureType();
			ft.setStorageSrid(storageSrid);

			final FeatureTypeSchema schema = schemaParser.parseSchema(cfg, p, ft, collectionId);
			ft.setName(collectionId);
			ft.setId(HakunaTestSourceSchemaParser.getFeaturesIdProperty(ft, schema));
			final HakunaGeometryType geometryType = schema.getGeomType();
			if (geometryType != null) {
				HakunaPropertyGeometry geomProp = schemaParser.getFeaturesGeomProperty(ft, schema, geometryType, srids);
				ft.setGeom(geomProp);
			}
			ft.setSrid(srids);
			ft.setProperties(getFeaturesProperties(cfg, p, ft, schema));

			String t = p + "testdata.";
			final Map<String, String> testdata = cfg.getAllStartingWith(t);
			final List<HakunaProperty> props = ft.getProperties();
			List<Object[]> objList = testdata.entrySet().stream().sorted((a, b) -> a.getKey().compareTo(b.getKey()))
					.map(entry -> {
						String[] vals = entry.getValue().split(";", -1);
						Object[] objs = new Object[vals.length];
						System.arraycopy(vals, 0, objs, 0, vals.length);
						Geometry geom;
						try {
							geom = wktReader.read(objs[1].toString());
						} catch (ParseException e) {
							geom = null;
						}
						objs[1] = new HakunaGeometryJTS(geom);
						// Column 0 is the feature id, typed by the schema
						objs[0] = toTestValue(schema.getIdType(), vals[0]);
						// Columns 2.. are the properties, in the same order as
						// getColumnIndex() assumes (property n is column n + 2).
						// An empty field is a null value, everything else is
						// parsed to the property's own type.
						if (objs.length - 2 != props.size()) {
							throw new IllegalArgumentException("Testdata row has " + (objs.length - 2)
									+ " property columns, expected " + props.size());
						}
						for (int i = 2; i < objs.length; i++) {
							objs[i] = toTestValue(props.get(i - 2).getType(), vals[i]);
						}
						return objs;
					}).collect(Collectors.toList());
			ft.setObjs(objList);

			ft.setPaginationStrategy(PaginationStrategyOffset.INSTANCE);

			return ft;
		} catch (Throwable t) {
			LOG.error("Parse Test collection " + collectionId + "FAILURE", t);

			throw t;
		}

	}

	private static Object toTestValue(HakunaPropertyType type, String value) {
		if (value.isEmpty()) {
			return null;
		}
		switch (type) {
		case BOOLEAN:
			return Boolean.valueOf(value);
		case INT:
			return Integer.valueOf(value);
		case LONG:
			return Long.valueOf(value);
		case FLOAT:
			return Float.valueOf(value);
		case DOUBLE:
			return Double.valueOf(value);
		default:
			return value;
		}
	}

	private List<HakunaProperty> getFeaturesProperties(final HakunaConfigParser cfg, final String p,
			final HakunaTestSourceFeatureType ft, final FeatureTypeSchema featureTypeSchema) {
		return featureTypeSchema.getPropertyTypes().entrySet().stream().map(
				entry -> schemaParser.toHakunaProperty(cfg, p, ft, entry.getKey(), entry.getValue(), featureTypeSchema))
				.filter(Objects::nonNull).collect(Collectors.toList());
	}

}
