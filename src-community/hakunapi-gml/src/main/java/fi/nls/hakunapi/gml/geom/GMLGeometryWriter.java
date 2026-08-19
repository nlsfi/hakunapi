package fi.nls.hakunapi.gml.geom;

import javax.xml.stream.XMLStreamWriter;

import fi.nls.hakunapi.core.FloatingPointFormatter;
import fi.nls.hakunapi.core.GeometryWriter;
import fi.nls.hakunapi.core.geom.HakunaGeometryType;

public class GMLGeometryWriter implements GeometryWriter {

    private final XMLStreamWriter xml;
    private final FloatingPointFormatter formatter;
    private final String fid;
    private final String property;

    private GMLGeometryWriterBase facaded;

    public GMLGeometryWriter(XMLStreamWriter xml, FloatingPointFormatter formatter, String fid, String property) {
        this.xml = xml;
        this.formatter = formatter;
        this.fid = fid;
        this.property = property;
    }

    @Override
    public void init(HakunaGeometryType type, int srid, int dimension) throws Exception {
        switch (type) {
        case POINT:
            this.facaded = new PointWriter(xml, formatter, fid, property);
            break;
        case LINESTRING:
            this.facaded = new LineStringWriter(xml, formatter, fid, property);
            break;
        case POLYGON:
            this.facaded = new PolygonWriter(xml, formatter, fid, property);
            break;
        case MULTIPOINT:
        case MULTILINESTRING:
        case MULTIPOLYGON:
        case GEOMETRYCOLLECTION:
            throw new IllegalArgumentException("Not yet implemented");
        }
        facaded.init(srid, dimension);
    }

    @Override
    public void end() throws Exception {
        facaded.end();
    }

    @Override
    public void writeCoordinate(double x, double y) throws Exception {
        facaded.writeCoordinate(x, y);
    }

    @Override
    public void writeCoordinate(double x, double y, double z) throws Exception {
        facaded.writeCoordinate(x, y, z);
    }

    @Override
    public void writeCoordinate(double x, double y, double z, double m) throws Exception {
        writeCoordinate(x, y, z);
    }

    @Override
    public void startRing() throws Exception {
        facaded.startRing();
    }

    @Override
    public void endRing() throws Exception {
        facaded.endRing();
    }

}
