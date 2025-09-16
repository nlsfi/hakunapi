package fi.nls.hakunapi.gml;

import java.io.OutputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.FeatureWriter;
import fi.nls.hakunapi.core.FloatingPointFormatter;
import fi.nls.hakunapi.core.SRIDCode;
import fi.nls.hakunapi.core.geom.HakunaGeometry;
import fi.nls.hakunapi.core.schemas.Link;
import fi.nls.hakunapi.core.util.HakunaBufferedOutputStream;
import fi.nls.hakunapi.gml.geom.GMLGeometryWriter;

public abstract class WFS110FeatureWriterBase implements FeatureWriter {

    protected static final XMLOutputFactory XOF = XMLOutputFactory.newInstance();
    
    private final char[] buf = new char[32];

    protected XMLStreamWriter xml;
    protected MapNamespaceContext nsCtx;

    protected FloatingPointFormatter formatter;

    protected String namespaceURI;
    protected String currentFeatureTypename;
    protected String currentFID;

    protected Queue<String> attributeBuffer = new ArrayDeque<>();
    
    protected void addNamespace(MapNamespaceContext ctx, Namespace ns) {
        ctx.add(ns.prefix, ns.uri);
    }
    
    public void setFormatter(FloatingPointFormatter formatter) {
        this.formatter = formatter;
    }
    
    @Override
    public void init(OutputStream out, SRIDCode srid) throws XMLStreamException {
        this.xml = XOF.createXMLStreamWriter(new HakunaBufferedOutputStream(out), "UTF-8");
        this.nsCtx = new MapNamespaceContext();
        
        addNamespace(nsCtx, Namespaces.GML_311.ns);
        addNamespace(nsCtx, Namespaces.WFS_110.ns);
        addNamespace(nsCtx, Namespaces.XSI.ns);

        xml.setNamespaceContext(nsCtx);
        xml.writeStartDocument("utf-8", "1.0");
    }
    
    @Override
    public void end(boolean timeStamp, List<Link> links, int numberReturned) throws Exception {
        // NOP
    }
    
    protected void writeNamespaces(List<Namespace> namespaces) throws XMLStreamException {
        for (Namespace ns : namespaces) {
            xml.writeNamespace(ns.prefix, ns.uri);
        }
    }

    protected void writeSchemaLocation(List<Namespace> namespaces) throws XMLStreamException {
        xml.writeAttribute(Namespaces.XSI.ns.uri, "schemaLocation", getSchemaLocation(namespaces));
    }

    private String getSchemaLocation(List<Namespace> namespaces) {
        StringBuilder sb = new StringBuilder();
        for (Namespace namespace : namespaces) {
            if (namespace.schemaLocation != null) {
                sb.append(namespace.uri).append(' ').append(namespace.schemaLocation).append(' ');
            }
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    protected void setFeatureType(FeatureType ft) {
        this.namespaceURI = ft.getNS();
        this.currentFeatureTypename = ft.getName();        
    }

    @Override
    public void writeGeometry(String name, HakunaGeometry geometry) throws Exception {
        geometry.write(new GMLGeometryWriter(xml, formatter, currentFID, name));
    }

    @Override
    public void writeProperty(String name, HakunaGeometry geometry) throws Exception {
        geometry.write(new GMLGeometryWriter(xml, formatter, currentFID, name));
    }

    @Override
    public void writeProperty(String name, String value) throws Exception {
        xml.writeStartElement(name);
        while (!attributeBuffer.isEmpty()) {
            String nsUri = attributeBuffer.remove();
            String attrName = attributeBuffer.remove();
            String attrValue = attributeBuffer.remove();
            xml.writeAttribute(nsUri, attrName, attrValue);
        }
        if (value != null) {
            xml.writeCharacters(value);
        }
        xml.writeEndElement();
    }
    
    private void writeProperty(String name, char[] arr, int off, int len) throws Exception {
        xml.writeStartElement(name);
        while (!attributeBuffer.isEmpty()) {
            String nsUri = attributeBuffer.remove();
            String attrName = attributeBuffer.remove();
            String attrValue = attributeBuffer.remove();
            xml.writeAttribute(nsUri, attrName, attrValue);
        }
        if (arr != null) {
            xml.writeCharacters(arr, off, len);
        }
        xml.writeEndElement();
    }

    @Override
    public void writeProperty(String name, Instant value) throws Exception {
        if (value == null) {
            return;
        }
        writeProperty(name, value.toString());
    }

    @Override
    public void writeProperty(String name, LocalDate value) throws Exception {
        if (value == null) {
            return;
        }
        writeProperty(name, value.toString());
    }

    @Override
    public void writeProperty(String name, boolean value) throws Exception {
        writeProperty(name, Boolean.toString(value));
    }

    @Override
    public void writeProperty(String name, int value) throws Exception {
        writeProperty(name, Integer.toString(value));
    }

    @Override
    public void writeProperty(String name, long value) throws Exception {
        writeProperty(name, Long.toString(value));
    }

    @Override
    public void writeProperty(String name, float value) throws Exception {
        writeProperty(name, buf, 0, formatter.writeFloat(value, buf, 0));
    }

    @Override
    public void writeProperty(String name, double value) throws Exception {
        writeProperty(name, buf, 0, formatter.writeDouble(value, buf, 0));
    }

    @Override
    public void writeAttribute(String name, String value) throws Exception {
        attributeBuffer.add("");
        attributeBuffer.add(name);
        attributeBuffer.add(value);
    }

    @Override
    public void writeNullProperty(String name) throws Exception {
        attributeBuffer.add(Namespaces.XSI.ns.uri);
        attributeBuffer.add("nil");
        attributeBuffer.add("true");
        writeProperty(name, (String) null);
    }

    @Override
    public void writeStartObject(String name) throws Exception {
        xml.writeStartElement(name);
    }

    @Override
    public void writeCloseObject() throws Exception {
        xml.writeEndElement();
    }

    @Override
    public void writeStartArray(String name) throws Exception {
        xml.writeStartElement(name);
    }

    @Override
    public void writeCloseArray() throws Exception {
        xml.writeEndElement();
    }

    @Override
    public void writeTimeStamp() throws Exception {
        // This should go into the root element - not gonna happen with streaming
    }

    @Override
    public void writeLinks(List<Link> link) throws Exception {
        // This should go into the root element - not gonna happen with streaming
    }

    @Override
    public void writeNumberReturned(int numberReturned) throws Exception {
        // This should go into the root element - not gonna happen with streaming
    }

    @Override
    public void close() throws Exception {
        xml.close();
    }

}
