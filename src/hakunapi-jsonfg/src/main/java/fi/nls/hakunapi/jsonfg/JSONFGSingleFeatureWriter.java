package fi.nls.hakunapi.jsonfg;

import java.time.Instant;
import java.time.LocalDate;

import static fi.nls.hakunapi.core.schemas.Crs.CRS84_SRID;
import fi.nls.hakunapi.core.DatetimeProperty;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.geom.HakunaGeometry;
import fi.nls.hakunapi.core.geom.HakunaGeometryDimension;
import fi.nls.hakunapi.core.projection.ProjectionTransformer;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;
import fi.nls.hakunapi.core.util.CrsUtil;
import fi.nls.hakunapi.geojson.hakuna.HakunaGeoJSON;
import fi.nls.hakunapi.geojson.hakuna.HakunaGeoJSONSingleFeatureWriter;

public class JSONFGSingleFeatureWriter extends HakunaGeoJSONSingleFeatureWriter {

    protected JSONFGGeometryWriter placeJson;

    FeatureType collectionFt = null;
    DatetimeProperty dateTimeProperty;
    String dateTimePropertyName;
    ProjectionTransformer outputCrs84Proj;

    boolean isCrs84;

    private Instant timestamp;
    private LocalDate date;

    @Override
    public void initGeometryWriter(HakunaGeometryDimension dims) {
        this.dims = dims;
        geometryJson = new JSONFGGeometryWriter(json, HakunaGeoJSON.GEOMETRY, forceLonLat || !crsIsLatLon, dims);
        placeJson = new JSONFGGeometryWriter(json, JSONFG.PLACE, forceLonLat || !crsIsLatLon, dims);
        propertyGeometryJson.clear();
    }

    protected void startJsonFgFeature(FeatureType ft) throws Exception {

        collectionFt = ft;

        if (ft.getDatetimeProperties() != null && !ft.getDatetimeProperties().isEmpty()) {
            dateTimeProperty = ft.getDatetimeProperties().get(0);
            dateTimePropertyName = dateTimeProperty.getProperty().getName();
        }

        if (getSrid() == CRS84_SRID) {
            outputCrs84Proj = null;
            isCrs84 = true;
        } else {
            outputCrs84Proj = collectionFt.getProjectionTransformerFactory().toCRS84(getSrid());
            isCrs84 = outputCrs84Proj.isNOP();
        }

        String ftName = ft.getName();
        String ftJSONFGSchema = JSONFG.getJSONFGSchema(ft);

        HakunaPropertyGeometry geomType = ft.getGeom();
        Integer geometryDimension = JSONFG.getGeometryDimension(geomType);

        json.writeStartObject();

        // conformsTo
        json.writeFieldName(JSONFG.CONFORMS_TO);
        json.writeStartArray();
        for (byte[] conf : JSONFG.CONF) {
            json.writeStringUnsafe(conf);
        }
        json.writeEndArray();

        json.writeStringField(JSONFG.FEATURE_TYPE, ftName);

        if (geometryDimension != null) {
            json.writeFieldName(JSONFG.GEOMETRY_DIMENSION);
            json.writeNumber(geometryDimension);
        }
        if (ftJSONFGSchema != null) {
            json.writeStringField(JSONFG.FEATURE_SCHEMA, ftJSONFGSchema);
        }

        if (!isCrs84) {
            String coordRefSys = CrsUtil.toUri(getSrid());
            json.writeStringField(JSONFG.COORD_REF_SYS, coordRefSys);
        }

        json.writeFieldName(HakunaGeoJSON.TYPE);
        json.writeStringUnsafe(HakunaGeoJSON.FEATURE, 0, HakunaGeoJSON.FEATURE.length);
        json.writeFieldName(HakunaGeoJSON.ID);

    }

    @Override
    public void startFeature(FeatureType ft, String layername, String fid) throws Exception {
        startJsonFgFeature(ft);
        json.writeString(fid);
    }

    @Override
    public void startFeature(FeatureType ft, String layername, long fid) throws Exception {
        startJsonFgFeature(ft);
        json.writeNumber(fid);
    }

    @Override
    public void endFeature() throws Exception {
        closeProperties();

        HakunaGeometry placeGeometry = null;
        HakunaGeometry geometry = null;
        if (isCrs84) {
            placeGeometry = null;
            geometry = cachedGeometry;
        } else {
            placeGeometry = cachedGeometry;
            geometry = JSONFG.getFootprintGeometry(placeGeometry, outputCrs84Proj);
        }

        JSONFG.writePlace(json, placeGeometry, geometry, placeJson, geometryJson);
        JSONFG.writeTemporal(json, dateTimeProperty, date, timestamp);

        // writeEndObject handled outside
    }

    @Override
    public void writeGeometry(String name, HakunaGeometry geometry) throws Exception {
        geometryCached = true;
        cachedGeometry = geometry;
    }

    @Override
    public void writeProperty(String name, Instant value) throws Exception {
        if (dateTimePropertyName != null && name.equals(dateTimePropertyName)) {
            timestamp = value;
        }
        super.writeProperty(name, value);
    }

    @Override
    public void writeProperty(String name, LocalDate value) throws Exception {
        if (dateTimePropertyName != null || name.equals(dateTimePropertyName)) {
            date = value;
        }
        super.writeProperty(name, value);
    }

}
