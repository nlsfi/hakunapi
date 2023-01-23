package fi.nls.hakunapi.gml.geom;

import javax.xml.stream.XMLStreamWriter;

import fi.nls.hakunapi.core.FloatingPointFormatter;
import fi.nls.hakunapi.gml.Namespaces;

public class PolygonWriter extends GMLGeometryWriterBase {

    private boolean exteriorRing;
    private boolean firstCoordinate;

    public PolygonWriter(XMLStreamWriter xml, FloatingPointFormatter formatter, String fid, String property) {
        super(xml, formatter, fid, property);
    }

    @Override
    public void init(int srid, int dimension) throws Exception {
        xml.writeStartElement(property); // <MyProp>
        xml.writeStartElement(Namespaces.GML_311.ns.uri, "Polygon"); // <gml:Polygon>
        xml.writeAttribute("srsName", getSrsName(srid));
        exteriorRing = true;
    }

    @Override
    public void end() throws Exception {
        xml.writeEndElement(); // </gml:Polygon>
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
        if (exteriorRing) {
            xml.writeStartElement(Namespaces.GML_311.ns.uri, "exterior"); // <gml:exterior>
            exteriorRing = false;
        } else {
            xml.writeStartElement(Namespaces.GML_311.ns.uri, "interior"); // <gml:interior>
        }
        xml.writeStartElement(Namespaces.GML_311.ns.uri, "LinearRing"); // <gml:LinearRing>
        xml.writeStartElement(Namespaces.GML_311.ns.uri, "posList"); // <gml:posList>
        firstCoordinate = true;
    }

    @Override
    public void endRing() throws Exception {
        xml.writeEndElement(); // </gml:posList>
        xml.writeEndElement(); // </gml:LinearRing>
        xml.writeEndElement(); // </gml:interior> or </gml:exterior>
    }

}
