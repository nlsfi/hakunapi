package fi.nls.hakunapi.geojson.hakuna;

// TODO: forced to share package fi.nls.hakunapi.geojson.hakuna due to json.writeStringUnsafe scope
// TODO: multiple datetimeProperties
// TODO: WGS84 check spec
// TODO: figure out when to write geometry and/or place with or without coordRefSys
// TODO: preprojected geoms?

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.locationtech.jts.geom.Geometry;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.FloatingPointFormatter;
import fi.nls.hakunapi.core.GeometryWriter;
import fi.nls.hakunapi.core.geom.HakunaGeometry;
import fi.nls.hakunapi.core.geom.HakunaGeometryDimension;
import fi.nls.hakunapi.core.geom.HakunaGeometryJTS;
import fi.nls.hakunapi.core.projection.NOPProjectionTransformer;
import fi.nls.hakunapi.core.projection.ProjectionHelper;
import fi.nls.hakunapi.core.projection.ProjectionTransformer;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;
import fi.nls.hakunapi.core.schemas.Crs;
import fi.nls.hakunapi.core.schemas.Link;
import fi.nls.hakunapi.core.util.CrsUtil;
import fi.nls.hakunapi.jsonfg.JSONFG;

public class JSONFGFeatureCollectionWriter extends HakunaGeoJSONFeatureCollectionWriter implements JSONFG {

	protected int outputSrid;
	protected JSONFGGeometryWriter placeJson;

	// local cached (+inherited geometryCached & cachedGeometry )
	FeatureType collectionFt = null;
	LocalDate date = null;
	Instant timestamp = null;
	ProjectionTransformer outputWgs84Proj;

	@Override
	public void init(OutputStream out, FloatingPointFormatter formatter, int srid, boolean crsIsLatLon)
			throws IOException {
		super.init(out, formatter, Crs.CRS84_SRID, crsIsLatLon);
		outputSrid = srid;

	}

	@Override
	public void initGeometryWriter(HakunaGeometryDimension dims) {
		this.dims = dims;
		geometryJson = new JSONFGGeometryWriter(json, HakunaGeoJSON.GEOMETRY, true, dims);
		placeJson = new JSONFGGeometryWriter(json, PLACE, true, dims);
		propertyGeometryJson.clear();
	}

	protected void writeFeatureCollection() throws IOException {
	}

	@Override
	public void writeProperty(String name, HakunaGeometry geometry) throws Exception {
		openProperties();
		final GeometryWriter geomWriter = propertyGeometryJson.computeIfAbsent(name, k -> new JSONFGGeometryWriter(json,
				k.getBytes(StandardCharsets.UTF_8), forceLonLat || !crsIsLatLon, dims));
		geometry.write(geomWriter);
	}

	@Override
	public void startFeatureCollection(FeatureType ft, String layername) throws Exception {

		collectionFt = ft;
		if (isWGS84(outputSrid)) {
			outputWgs84Proj = NOPProjectionTransformer.INSTANCE;
		} else {
			outputWgs84Proj = collectionFt.getProjectionTransformerFactory().toCRS84(outputSrid);
		}

		boolean isWgs84 = isWGS84(outputSrid);
		String ftName = ft.getName();
		String ftJSONFGSchema = getJSONFGSchema(ft);

		HakunaPropertyGeometry geomType = ft.getGeom();
		Integer geometryDimension = getGeometryDimension(geomType);

		json.writeStartObject();

		// conformsTo
		json.writeFieldName(CONFORMS_TO);
		json.writeStartArray();
		for (byte[] conf : CONF) {
			json.writeStringUnsafe(conf);
		}
		json.writeEndArray();

		// type
		json.writeFieldName(HakunaGeoJSON.TYPE);
		json.writeStringUnsafe(HakunaGeoJSON.FEATURE_COLLECTION, 0, HakunaGeoJSON.FEATURE_COLLECTION.length);

		json.writeStringField(FEATURE_TYPE, ftName);

		if (geometryDimension != null) {
			json.writeFieldName(GEOMETRY_DIMENSION);
			json.writeNumber(geometryDimension);
		}
		if (ftJSONFGSchema != null) {
			json.writeStringField(FEATURE_SCHEMA, ftJSONFGSchema);
		}

		if (!isWgs84) {
			String coordRefSys = CrsUtil.toUri(outputSrid);
			json.writeStringField(COORD_REF_SYS, coordRefSys);
		}

		json.writeFieldName(HakunaGeoJSON.FEATURES);
		json.writeStartArray();
	}

	@Override
	public void endFeatureCollection() throws IOException {
		// close "features":[ ]
		json.writeEndArray();
		collectionFt = null;
	}

	@Override
	public void end(boolean timeStamp, List<Link> links, int numberReturned) throws IOException {
		if (timeStamp) {
			writeTimeStamp();
		}
		writeLinks(links);
		writeNumberReturned(numberReturned);
	}

	@Override
	public void startFeature(String fid) throws IOException {
		startFeature();
		json.writeString(fid);
	}

	@Override
	public void startFeature(long fid) throws IOException {
		startFeature();
		json.writeNumber(fid);
	}

	private void startFeature() throws IOException {
		date = null;
		timestamp = null;
		cachedGeometry = null;
		geometryCached = false;
		json.writeStartObject();
		json.writeFieldName(HakunaGeoJSON.TYPE);
		json.writeStringUnsafe(HakunaGeoJSON.FEATURE, 0, HakunaGeoJSON.FEATURE.length);
		json.writeFieldName(HakunaGeoJSON.ID);

	}

	@Override
	public void writeProperty(String name, Instant value) throws Exception {
		super.writeProperty(name, value);

		// TODO what when why multiple temporal?
		if (timestamp == null && value != null) {
			timestamp = value;
		}
	}

	@Override
	public void writeProperty(String name, LocalDate value) throws Exception {
		super.writeProperty(name, value);

		// TODO what when why multiple temporal?
		if (date == null && value != null) {
			date = value;
		}
	}

	@Override
	public void writeGeometry(String name, HakunaGeometry geometry) throws Exception {
		// force cache to enable quirks
		geometryCached = true;
		cachedGeometry = geometry;
	}

	@Override
	public void endFeature() throws Exception {
		closeProperties();

		// TODO: pre reprojected geoms
		if (geometryCached && cachedGeometry != null) {
			// PLACE
			cachedGeometry.write(placeJson);

			// GEOMETRY WGS84
			// TOWGS84
			Geometry wgs84Geometry = ProjectionHelper.reproject(cachedGeometry.toJTSGeometry().copy(), outputWgs84Proj);
			HakunaGeometryJTS jts = new HakunaGeometryJTS(wgs84Geometry);
			jts.write(geometryJson);

			// cleanup
			geometryCached = false;
			cachedGeometry = null;

		} else {
			// TODO check if this is required
			json.writeFieldName(GEOMETRY);
			json.writeNull();

		}

		// TODO what when why multiple temporal?
		if (date != null || timestamp != null) {

			json.writeFieldName(TIME);
			json.writeStartObject();

			if (timestamp != null) {
				json.writeFieldName(_TIMESTAMP);
				json.writeStringUnsafe(timestamp.toString());
			} else if (date != null) {
				json.writeFieldName(DATE);
				json.writeStringUnsafe(date.toString());
			}

			json.writeEndObject();
		}

		json.writeEndObject();
	}
}
