package fi.nls.hakunapi.gml.geom;

import javax.xml.stream.XMLStreamWriter;

import fi.nls.hakunapi.core.FloatingPointFormatter;
import fi.nls.hakunapi.gml.Namespaces;

public class LineStringWriter extends GMLGeometryWriterBase {

    private boolean firstCoordinate;

    public LineStringWriter(XMLStreamWriter xml, FloatingPointFormatter formatter, String fid, String property) {
        super(xml, formatter, fid, property);
    }

    @Override
    public void init(int srid, int dimension) throws Exception {
        xml.writeStartElement(property); // <MyProp>
        xml.writeStartElement(Namespaces.GML_311.ns.uri, "LineString"); // <gml:LineString>
        xml.writeAttribute("srsName", getSrsName(srid));
    }

    @Override
    public void end() throws Exception {
        xml.writeEndElement(); // </LineString>
        xml.writeEndElement(); // </MyProp>
    }

    @Override
    public void writeCoordinate(double x, double y) throws Exception {
        xml.writeCharacters(format(x, y, firstCoordinate));
        firstCoordinate = false;
    }

    @Override
    public void writeCoordinate(double x, double y, double z) throws Exception {
        xml.writeCharacters(format(x, y, z, firstCoordinate));
        firstCoordinate = false;
    }

    @Override
    public void writeCoordinate(double x, double y, double z, double m) throws Exception {
        xml.writeCharacters(format(x, y, z, m, firstCoordinate));
        firstCoordinate = false;
    }

    @Override
    public void startRing() throws Exception {
        xml.writeStartElement(Namespaces.GML_311.ns.uri, "posList"); // <gml:posList>
        firstCoordinate = true;
    }

    @Override
    public void endRing() throws Exception {
        xml.writeEndElement(); // </gml:posList>
    }



}
