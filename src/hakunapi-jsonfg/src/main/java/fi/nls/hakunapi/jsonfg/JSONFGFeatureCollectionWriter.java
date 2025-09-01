package fi.nls.hakunapi.jsonfg;

import static fi.nls.hakunapi.core.schemas.Crs.CRS84_SRID;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import fi.nls.hakunapi.core.DatetimeProperty;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.GeometryWriter;
import fi.nls.hakunapi.core.SRIDCode;
import fi.nls.hakunapi.core.geom.HakunaGeometry;
import fi.nls.hakunapi.core.projection.ProjectionTransformer;
import fi.nls.hakunapi.core.schemas.Link;
import fi.nls.hakunapi.geojson.hakuna.HakunaGeoJSON;
import fi.nls.hakunapi.geojson.hakuna.HakunaGeoJSONFeatureCollectionWriter;

public class JSONFGFeatureCollectionWriter extends HakunaGeoJSONFeatureCollectionWriter {

    private GeometryWriter placeJson;

    private DatetimeProperty dateTimeProperty;
    private String dateTimePropertyName;

    private ProjectionTransformer outputCrs84Proj;
    private boolean isCrs84;

    private Instant timestamp;
    private LocalDate date;
       
    @Override
    public void init(OutputStream out, SRIDCode srid) throws IOException {
        super.init(out, srid);
        geometryJson = createGeometryWriter(HakunaGeoJSON.GEOMETRY, SRIDCode.CRS84);
        placeJson = createGeometryWriter(JSONFG.PLACE, srid);
        propertyGeometryJson.clear();
    }

    @Override
    protected void writeFeatureCollection() throws IOException {
        // NOP
    }

    @Override
    public void startFeatureCollection(FeatureType ft, String layername) throws Exception {

        if (ft.getDatetimeProperties() != null && !ft.getDatetimeProperties().isEmpty()) {
            dateTimeProperty = ft.getDatetimeProperties().get(0);
            dateTimePropertyName = dateTimeProperty.getProperty().getName();
        }
        
        if (srid.getSrid() == CRS84_SRID) {
            outputCrs84Proj = null;
            isCrs84 = true;
        } else {
            outputCrs84Proj = ft.getProjectionTransformerFactory().toCRS84(srid.getSrid());
            isCrs84 = outputCrs84Proj.isNOP();
        }

        JSONFG.writeMetadata(json, ft, srid.getSrid());
        
        json.writeFieldName(HakunaGeoJSON.TYPE);
        json.writeStringUnsafe(HakunaGeoJSON.FEATURE_COLLECTION, 0, HakunaGeoJSON.FEATURE_COLLECTION.length);


        json.writeFieldName(HakunaGeoJSON.FEATURES);
        json.writeStartArray();
    }

    @Override
    public void endFeatureCollection() throws IOException {
        // close "features":[ ]
        json.writeEndArray();
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
        resetFeature();
        super.startFeature(fid);
    }

    @Override
    public void startFeature(long fid) throws IOException {
        resetFeature();
        super.startFeature(fid);
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
        if (name.equals(dateTimePropertyName)) {
            timestamp = value;
        }
        super.writeProperty(name, value);
    }

    @Override
    public void writeProperty(String name, LocalDate value) throws Exception {
        if (name.equals(dateTimePropertyName)) {
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
