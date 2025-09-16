package fi.nls.hakunapi.jsonfg;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;

import org.locationtech.jts.geom.Geometry;

import fi.nls.hakunapi.core.DatetimeProperty;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.GeometryWriter;
import fi.nls.hakunapi.core.geom.HakunaGeometry;
import fi.nls.hakunapi.core.geom.HakunaGeometryJTS;
import fi.nls.hakunapi.core.projection.ProjectionHelper;
import fi.nls.hakunapi.core.projection.ProjectionTransformer;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;
import fi.nls.hakunapi.core.schemas.Crs;
import fi.nls.hakunapi.core.util.CrsUtil;
import fi.nls.hakunapi.geojson.hakuna.HakunaJsonWriter;

public final class JSONFG {

    public static final String TYPE = "jsonfg";

    public static final String MEDIA_MAIN_TYPE = "application";
    public static final String MEDIA_SUB_TYPE = "vnd.ogc.fg+json";
    public static final String MIME_TYPE = MEDIA_MAIN_TYPE + "/" + MEDIA_SUB_TYPE;

    public static final byte[] CONFORMS_TO = "conformsTo".getBytes();
    public static final byte[] FEATURE_TYPE = "featureType".getBytes();
    public static final byte[] GEOMETRY_DIMENSION = "geometryDimension".getBytes();
    public static final byte[] FEATURE_SCHEMA = "featureSchema".getBytes();
    public static final byte[] COORD_REF_SYS = "coordRefSys".getBytes();

    public static final byte[] TIME = "time".getBytes();
    public static final byte[] DATE = "date".getBytes();

    public static final byte[] TIMESTAMP = "timestamp".getBytes();
    public static final byte[] INTERVAL = "interval".getBytes();

    public static final byte[] GEOMETRY = "geometry".getBytes(StandardCharsets.UTF_8);
    public static final byte[] PLACE = "place".getBytes();

    public static final byte[][] CONF = new byte[][] {
            //
            "http://www.opengis.net/spec/json-fg-1/0.2/conf/core".getBytes(),
            // "http://www.opengis.net/spec/json-fg-1/0.2/conf/types-schemas".getBytes()
    };
    
    /*
     * TODO: JSONFG schema differs from JSON schema. We'll need JSONFG schema
     * resource or SchemaFormatFactorySpi or such to support providing JSONFG schema
     */
    public static String getJSONFGSchema(FeatureType ft) {
        return null;
    }

    public static void writePlace(HakunaJsonWriter json, HakunaGeometry placeGeometry, HakunaGeometry geometry,
            GeometryWriter placeWriter, GeometryWriter geometryWriter) throws Exception {
        if (placeGeometry != null) {
            placeGeometry.write(placeWriter);
            if (geometry != null) {
                geometry.write(geometryWriter);
            }
        } else if (geometry != null) {
            geometry.write(geometryWriter);
        } else {
            json.writeFieldName(GEOMETRY);
            json.writeNull();
        }
    }

    public static void writeTemporal(HakunaJsonWriter json, DatetimeProperty prop, LocalDate date, Instant timestamp)
            throws IOException {
        if (prop == null) {
            return;
        }

        if (date == null && timestamp == null) {
            json.writeNullField(JSONFG.TIME);
        } else {

            json.writeFieldName(JSONFG.TIME);
            json.writeStartObject();

            if (timestamp != null) {
                json.writeFieldName(JSONFG.TIMESTAMP);
                json.writeStringUnsafe(timestamp.toString());
            } else if (date != null) {
                json.writeFieldName(DATE);
                json.writeStringUnsafe(date.toString());
            }

            json.writeEndObject();
        }
    }

    public static HakunaGeometry getFootprintGeometry(HakunaGeometry placeGeometry,
            ProjectionTransformer outputCrs84Proj) {
        
        if(placeGeometry==null) {
            return null;
        }

        Geometry src = placeGeometry.toJTSGeometry();

        Geometry dup = src.copy();
        Geometry wgs84Geometry = ProjectionHelper.reproject(dup, outputCrs84Proj);
        return new HakunaGeometryJTS(wgs84Geometry);
    }

    public static void writeMetadata(HakunaJsonWriter json, FeatureType ft, int srid) throws IOException {

        String ftName = ft.getName();
        String ftJSONFGSchema = JSONFG.getJSONFGSchema(ft);

        HakunaPropertyGeometry geomType = ft.getGeom();
        int geometryDimension = JSONFG.getGeometryDimension(geomType);

        json.writeStartObject();

        json.writeFieldName(JSONFG.CONFORMS_TO);
        json.writeStartArray();
        for (byte[] conf : JSONFG.CONF) {
            json.writeStringUnsafe(conf);
        }
        json.writeEndArray();

        json.writeStringField(JSONFG.FEATURE_TYPE, ftName);

        if (geometryDimension != UNKNOWN_DIMENSION) {
            json.writeFieldName(JSONFG.GEOMETRY_DIMENSION);
            json.writeNumber(geometryDimension);
        }
        if (ftJSONFGSchema != null) {
            json.writeStringField(JSONFG.FEATURE_SCHEMA, ftJSONFGSchema);
        }

        if (srid != Crs.CRS84_SRID) {
            String coordRefSys = CrsUtil.toUri(srid);
            json.writeStringField(JSONFG.COORD_REF_SYS, coordRefSys);
        }        
    }
    
    private static final int UNKNOWN_DIMENSION = -1;

    private static int getGeometryDimension(HakunaPropertyGeometry geomProperty) {
        if (geomProperty == null || geomProperty.getGeometryType() == null) {
            return UNKNOWN_DIMENSION;
        }
        switch (geomProperty.getGeometryType()) {
        case POINT:
        case MULTIPOINT:
            return 0;
        case LINESTRING:
        case MULTILINESTRING:
            return 1;
        case POLYGON:
        case MULTIPOLYGON:
            return 2;
        default:
            return UNKNOWN_DIMENSION;
        }
    }

}