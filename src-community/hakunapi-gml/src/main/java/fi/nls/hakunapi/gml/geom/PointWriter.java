package fi.nls.hakunapi.gml.geom;

import javax.xml.stream.XMLStreamWriter;

import fi.nls.hakunapi.core.FloatingPointFormatter;
import fi.nls.hakunapi.gml.Namespaces;

public class PointWriter extends GMLGeometryWriterBase {

    public PointWriter(XMLStreamWriter xml, FloatingPointFormatter formatter, String fid, String property) {
        super(xml, formatter, fid, property);
    }

    @Override
    public void init(int srid, int dimension) throws Exception {
        xml.writeStartElement(property);
        xml.writeStartElement(Namespaces.GML_311.ns.uri, "Point");
        xml.writeAttribute("srsName", getSrsName(srid));
    }

    @Override
    public void end() throws Exception {
        xml.writeEndElement();
        xml.writeEndElement();
    }

    @Override
    public void writeCoordinate(double x, double y) throws Exception {
        xml.writeStartElement(Namespaces.GML_311.ns.uri, "pos");
        xml.writeCharacters(format(x, y, true));
        xml.writeEndElement();
    }

    @Override
    public void writeCoordinate(double x, double y, double z) throws Exception {
        xml.writeStartElement(Namespaces.GML_311.ns.uri, "pos");
        xml.writeCharacters(format(x, y, z, true));
        xml.writeEndElement();
    }

    @Override
    public void writeCoordinate(double x, double y, double z, double m) throws Exception {
        xml.writeStartElement(Namespaces.GML_311.ns.uri, "pos");
        xml.writeCharacters(format(x, y, z, m, true));
        xml.writeEndElement();
    }

    @Override
    public void startRing() throws Exception {
        // NOP
    }

    @Override
    public void endRing() throws Exception {
        // NOP
    }

}
