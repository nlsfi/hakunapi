package fi.nls.hakunapi.core.geom;

import java.nio.ByteOrder;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFilter;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.WKBConstants;
import org.locationtech.jts.io.WKBWriter;

import fi.nls.hakunapi.core.GeometryWriter;
import fi.nls.hakunapi.core.util.GeoPackageWKBWriter;

public class HakunaGeometryJTS implements HakunaGeometry {

    private final Geometry geom;
    private int dimension;
    private int numMeasures;

    public HakunaGeometryJTS(Geometry geom) {
        this.geom = geom;
        this.dimension = 0;
        this.numMeasures = -1;
    }

    @Override
    public void toWKB(byte[] buf, int off) {
        boolean z = (getDimension() - getNumMeasures()) > 2;
        boolean m = getNumMeasures() > 0;
        GeoPackageWKBWriter.write(geom, z, m, buf, off, ByteOrder.nativeOrder());
    }

    @Override
    public int getWKBLength() {
        boolean z = (getDimension() - getNumMeasures()) > 2;
        boolean m = getNumMeasures() > 0;
        return GeoPackageWKBWriter.length(geom, z, m);
    }

    @Override
    public byte[] toEWKB() {
        return new WKBWriter(getDimensionFromFirstCoordinate(geom), true).write(geom);
    }

    @Override
    public Geometry toJTSGeometry() {
        return geom;
    }

    @Override
    public void write(GeometryWriter writer) throws Exception {
        writeGeometry(geom, writer, true);
    }

    private void writeGeometry(Geometry geom, GeometryWriter writer, boolean init) throws Exception {
        if (geom instanceof Point) {
            writePoint((Point) geom, writer, init);
        } else if (geom instanceof LineString) {
            writeLineString((LineString) geom, writer, init);
        } else if (geom instanceof Polygon) {
            writePolygon((Polygon) geom, writer, init);
        } else if (geom instanceof MultiPoint) {
            writeMultiPoint((MultiPoint) geom, writer, init);
        } else if (geom instanceof MultiLineString) {
            writeMultiLineString((MultiLineString) geom, writer, init);
        } else if (geom instanceof MultiPolygon) {
            writeMultiPolygon((MultiPolygon) geom, writer, init);
        } else if (geom instanceof GeometryCollection) {
            throw new RuntimeException("Geometry collection not yet implemented");
        } else {
            throw new RuntimeException("Unknown geometry type");
        }
    }

    private void writePoint(Point geom, GeometryWriter writer, boolean init) throws Exception {
        if (init) {
            writer.init(HakunaGeometryType.POINT, geom.getSRID(), getDimensionFromFirstCoordinate(geom));
        }

        writeCoordinateSequence(geom.getCoordinateSequence(), writer);

        if (init) {
            writer.end();
        }
    }

    private void writeLineString(LineString geom, GeometryWriter writer, boolean init) throws Exception {
        if (init) {
            writer.init(HakunaGeometryType.LINESTRING, geom.getSRID(), getDimensionFromFirstCoordinate(geom));
        }

        writer.startRing();
        writeCoordinateSequence(geom.getCoordinateSequence(), writer);
        writer.endRing();

        if (init) {
            writer.end();
        }
    }

    private void writePolygon(Polygon geom, GeometryWriter writer, boolean init) throws Exception {
        if (init) {
            writer.init(HakunaGeometryType.POLYGON, geom.getSRID(), getDimensionFromFirstCoordinate(geom));
        }

        writer.startRing();
        writeLineString(geom.getExteriorRing(), writer, false);
        for (int i = 0; i < geom.getNumInteriorRing(); i++) {
            writeLineString(geom.getInteriorRingN(i), writer, false);
        }
        writer.endRing();

        if (init) {
            writer.end();
        }
    }

    private void writeMultiPoint(MultiPoint geom, GeometryWriter writer, boolean init) throws Exception {
        if (init) {
            writer.init(HakunaGeometryType.MULTIPOINT, geom.getSRID(), getDimensionFromFirstCoordinate(geom));
        }

        writer.startRing();
        for (int i = 0; i < geom.getNumGeometries(); i++) {
            Point g = (Point) geom.getGeometryN(i);
            writePoint(g, writer, false);
        }
        writer.endRing();

        if (init) {
            writer.end();
        }
    }

    private void writeMultiLineString(MultiLineString geom, GeometryWriter writer, boolean init) throws Exception {
        if (init) {
            writer.init(HakunaGeometryType.MULTILINESTRING, geom.getSRID(), getDimensionFromFirstCoordinate(geom));
        }

        writer.startRing();
        for (int i = 0; i < geom.getNumGeometries(); i++) {
            LineString g = (LineString) geom.getGeometryN(i);
            writeLineString(g, writer, false);
        }
        writer.endRing();

        if (init) {
            writer.end();
        }
    }

    private void writeMultiPolygon(MultiPolygon geom, GeometryWriter writer, boolean init) throws Exception {
        if (init) {
            writer.init(HakunaGeometryType.MULTIPOLYGON, geom.getSRID(), getDimensionFromFirstCoordinate(geom));
        }

        writer.startRing();
        for (int i = 0; i < geom.getNumGeometries(); i++) {
            Polygon g = (Polygon) geom.getGeometryN(i);
            writePolygon(g, writer, false);
        }
        writer.endRing();

        if (init) {
            writer.end();
        }
    }

    private void writeCoordinateSequence(CoordinateSequence cs, GeometryWriter writer) throws Exception {
        for (int i = 0; i < cs.size(); i++) {
            switch (cs.getDimension()) {
            case 2:
                writer.writeCoordinate(cs.getX(i), cs.getY(i));
                break;
            case 3:
                writer.writeCoordinate(cs.getX(i), cs.getY(i), cs.getOrdinate(i, 2));
                break;
            case 4:
                writer.writeCoordinate(cs.getX(i), cs.getY(i), cs.getOrdinate(i, 2), cs.getOrdinate(i, 3));
                break;
            }
        }
    }

    @Override
    public Envelope boundedBy() {
        return geom.getEnvelopeInternal();
    }

    @Override
    public int getSrid() {
        return geom.getSRID();
    }

    @Override
    public int getWKBType() {
        if (geom instanceof Point)
            return WKBConstants.wkbPoint;
        else if (geom instanceof LineString)
            return WKBConstants.wkbLineString;
        else if (geom instanceof Polygon)
            return WKBConstants.wkbPolygon; 
        else if (geom instanceof MultiPoint)
            return WKBConstants.wkbMultiPoint; 
        else if (geom instanceof MultiLineString)
            return WKBConstants.wkbMultiLineString;
        else if (geom instanceof MultiPolygon)
            return WKBConstants.wkbMultiPolygon;
        else if (geom instanceof GeometryCollection)
            return WKBConstants.wkbGeometryCollection;
        else
            throw new IllegalArgumentException("Unknown geometry type");
    }

    @Override
    public int getDimension() {
        if (dimension == 0) {
            dimension = getDimensionFromFirstCoordinate(geom);
        }
        return dimension;
    }

    private int getNumMeasures() {
        if (numMeasures < 0) {
            numMeasures = numMeasures(geom);
        }
        return numMeasures;
    }

    private static int getDimensionFromFirstCoordinate(Geometry g) {
        int[] intptr = new int[1];
        g.apply(new CoordinateSequenceFilter() {

            @Override
            public boolean isGeometryChanged() {
                return false;
            }

            @Override
            public boolean isDone() {
                return true;
            }

            @Override
            public void filter(CoordinateSequence seq, int i) {
                intptr[0] = seq.getDimension();
            }
        });
        return intptr[0];
    }

    private static int numMeasures(Geometry g) {
        int[] intptr = new int[1];
        g.apply(new CoordinateSequenceFilter() {

            @Override
            public boolean isGeometryChanged() {
                return false;
            }

            @Override
            public boolean isDone() {
                return true;
            }

            @Override
            public void filter(CoordinateSequence seq, int i) {
                intptr[0] = seq.getMeasures();
            }
        });
        return intptr[0];
    }

}
