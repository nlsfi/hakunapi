package fi.nls.hakunapi.filter;

import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import fi.nls.hakunapi.core.geom.HakunaGeometryFactory;

public class GeoJSONReader {

    private static final GeometryFactory GF = HakunaGeometryFactory.GF;

    public static Geometry toGeometry(Map<String, Object> geometry) {
        String geomType = GeoJSONUtil.getString(geometry, GeoJSON.TYPE);
        switch (geomType) {
        case GeoJSON.POINT:
            return toPoint(geometry);
        case GeoJSON.LINESTRING:
            return toLineString(geometry);
        case GeoJSON.POLYGON:
            return toPolygon(geometry);
        case GeoJSON.MULTI_POINT:
            return toMultiPoint(geometry);
        case GeoJSON.MULTI_LINESTRING:
            return toMultiLineString(geometry);
        case GeoJSON.MULTI_POLYGON:
            return toMultiPolygon(geometry);
        case GeoJSON.GEOMETRY_COLLECTION:
            return toGeometryCollection(geometry);
        }
        throw new IllegalArgumentException("Invalid geometry type");
    }

    public static Point toPoint(Map<String, Object> geometry) {
        return GF.createPoint(toCoordinate(
                GeoJSONUtil.getList(geometry, GeoJSON.COORDINATES)));
    }

    public static LineString toLineString(Map<String, Object> geometry) {
        Coordinate[] coordinates = toCoordinates(
                GeoJSONUtil.getList(geometry, GeoJSON.COORDINATES));
        return GF.createLineString(coordinates);
    }

    public static Polygon toPolygon(Map<String, Object> geometry) {
        return toPolygon(
                GeoJSONUtil.getList(geometry, GeoJSON.COORDINATES));
    }

    public static MultiPoint toMultiPoint(Map<String, Object> geometry) {
        Coordinate[] coordinates = toCoordinates(
                GeoJSONUtil.getList(geometry, GeoJSON.COORDINATES));
        return GF.createMultiPointFromCoords(coordinates);
    }

    public static MultiLineString toMultiLineString(Map<String, Object> geometry) {
        Coordinate[][] coordinates = toCoordinatesArray(
                GeoJSONUtil.getList(geometry, GeoJSON.COORDINATES));
        int n = coordinates.length;
        LineString[] lineStrings = new LineString[n];
        for (int i = 0; i < n; i++) {
            lineStrings[i] = GF.createLineString(coordinates[i]);
        }
        return GF.createMultiLineString(lineStrings);
    }

    public static MultiPolygon toMultiPolygon(Map<String, Object> geometry) {
        List<Object> arrayOfPolygons = GeoJSONUtil.getList(geometry, GeoJSON.COORDINATES);
        int n = arrayOfPolygons.size();
        Polygon[] polygons = new Polygon[n];
        for (int i = 0; i < n; i++) {
            polygons[i] = toPolygon(
                    GeoJSONUtil.getList(arrayOfPolygons, i));
        }
        return GF.createMultiPolygon(polygons);
    }

    public static GeometryCollection toGeometryCollection(Map<String, Object> geometry) {
        List<Object> geometryArray = GeoJSONUtil.getList(geometry, GeoJSON.GEOMETRIES);
        int n = geometryArray.size();
        Geometry[] geometries = new Geometry[n];
        for (int i = 0; i < n; i++) {
            geometries[i] = toGeometry(
                    GeoJSONUtil.getMap(geometryArray, i));
        }
        return GF.createGeometryCollection(geometries);
    }

    private static Coordinate toCoordinate(List<Object> coordinate) {
        return new Coordinate(
                GeoJSONUtil.getDouble(coordinate, 0),
                GeoJSONUtil.getDouble(coordinate, 1));
    }

    private static Coordinate[] toCoordinates(List<Object> arrayOfCoordinates) {
        int n = arrayOfCoordinates.size();
        Coordinate[] coordinates = new Coordinate[n];
        for (int i = 0; i < n; i++) {
            coordinates[i] = toCoordinate(GeoJSONUtil.getList(arrayOfCoordinates, i));
        }
        return coordinates;
    }

    private static Coordinate[][] toCoordinatesArray(List<Object> arrayOfArrayOfCoordinates) {
        int n = arrayOfArrayOfCoordinates.size();
        Coordinate[][] coordinates = new Coordinate[n][];
        for (int i = 0; i < n; i++) {
            coordinates[i] = toCoordinates(GeoJSONUtil.getList(arrayOfArrayOfCoordinates, i));
        }
        return coordinates;
    }

    private static Polygon toPolygon(List<Object> coordinatesArray) {
        Coordinate[][] coordinates = toCoordinatesArray(coordinatesArray);
        LinearRing exterior = GF.createLinearRing(coordinates[0]);
        LinearRing[] interiors = new LinearRing[coordinates.length - 1];
        for (int i = 1; i < coordinates.length; i++) {
            interiors[i - 1] = GF.createLinearRing(coordinates[i]);
        }
        return GF.createPolygon(exterior, interiors);
    }

}