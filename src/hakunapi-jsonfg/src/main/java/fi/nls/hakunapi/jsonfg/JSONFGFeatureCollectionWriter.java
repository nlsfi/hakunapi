package fi.nls.hakunapi.jsonfg;

import static fi.nls.hakunapi.core.schemas.Crs.CRS84_SRID;
import static fi.nls.hakunapi.jsonfg.JSONFG.CRS84_DIM;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import fi.nls.hakunapi.core.DatetimeProperty;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.FloatingPointFormatter;
import fi.nls.hakunapi.core.geom.HakunaGeometry;
import fi.nls.hakunapi.core.geom.HakunaGeometryDimension;
import fi.nls.hakunapi.core.projection.ProjectionTransformer;
import fi.nls.hakunapi.core.schemas.Link;
import fi.nls.hakunapi.geojson.hakuna.HakunaGeoJSON;
import fi.nls.hakunapi.geojson.hakuna.HakunaGeoJSONFeatureCollectionWriter;

public class JSONFGFeatureCollectionWriter extends HakunaGeoJSONFeatureCollectionWriter {

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

        FloatingPointFormatter fCrs84 = JSONFG.createCrs84GeometryFormatter();

        geometryJson = new JSONFGGeometryWriter(json, fCrs84, HakunaGeoJSON.GEOMETRY, true, CRS84_DIM);
        placeJson = new JSONFGGeometryWriter(json,  decimalFormatter, JSONFG.PLACE, forceLonLat || !crsIsLatLon, CRS84_DIM);
        propertyGeometryJson.clear();
    }

    @Override
    protected void writeFeatureCollection() throws IOException {
        // NOP
    }

    @Override
    public void startFeatureCollection(FeatureType ft, String layername) throws Exception {

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

        JSONFG.writeMetadata(json, ft, getSrid(), isCrs84);
        
        json.writeFieldName(HakunaGeoJSON.TYPE);
        json.writeStringUnsafe(HakunaGeoJSON.FEATURE_COLLECTION, 0, HakunaGeoJSON.FEATURE_COLLECTION.length);


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
        startJsonFgFeature();
        json.writeString(fid);
    }

    @Override
    public void startFeature(long fid) throws IOException {
        startJsonFgFeature();
        json.writeNumber(fid);
    }

    private void startJsonFgFeature() throws IOException {
        resetFeature();
        json.writeStartObject();
        json.writeFieldName(HakunaGeoJSON.TYPE);
        json.writeStringUnsafe(HakunaGeoJSON.FEATURE, 0, HakunaGeoJSON.FEATURE.length);
        json.writeFieldName(HakunaGeoJSON.ID);

    }

    private void resetFeature() {
        date = null;
        timestamp = null;
        cachedGeometry = null;
        geometryCached = false;

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

        json.writeEndObject();
    }

}
