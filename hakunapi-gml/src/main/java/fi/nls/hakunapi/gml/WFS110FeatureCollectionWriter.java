package fi.nls.hakunapi.gml;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamException;

import fi.nls.hakunapi.core.FeatureCollectionWriter;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.FloatingPointFormatter;
import fi.nls.hakunapi.core.schemas.Link;

public class WFS110FeatureCollectionWriter extends WFS110FeatureWriterBase implements FeatureCollectionWriter {

    public static final String MIME_TYPE = WFS110FeatureWriterBase.MIME_TYPE;
    
    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }
    
    

    @Override
    public void init(OutputStream out, FloatingPointFormatter formatter, int srid) throws XMLStreamException {
        super.init(out, formatter, srid);

        xml.writeStartElement(Namespaces.WFS_110.ns.uri, "FeatureCollection"); // <gml:FeatureCollection>
        List<Namespace> namespaces = Arrays.stream(Namespaces.values())
                .map(e -> e.ns)
                .collect(Collectors.toList());
        
        writeNamespaces(namespaces);
        writeSchemaLocation(namespaces);
    }

    @Override
    public void end(boolean timeStamp, List<Link> links, int numberReturned) throws Exception {
        xml.writeEndElement(); // </gml:FeatureCollection>
    }

    @Override
    public void startFeatureCollection(FeatureType ft, String layername) throws Exception {
        setFeatureType(ft);
        xml.writeStartElement(Namespaces.GML_311.ns.uri, "featureMembers"); // <gml:featureMembers>
        xml.setDefaultNamespace(namespaceURI);
        xml.writeDefaultNamespace(namespaceURI);
        xml.writeAttribute(Namespaces.XSI.ns.uri, "schemaLocation", namespaceURI + " " + ft.getSchemaLocation());
    }

    @Override
    public void endFeatureCollection() throws Exception {
        xml.writeEndElement(); // </gml:featureMembers>
    }

    @Override
    public void startFeature(String fid) throws Exception {
        this.currentFID = fid;
        xml.writeStartElement(namespaceURI, currentFeatureTypename);
        xml.writeAttribute(Namespaces.GML_311.ns.uri, "id", fid);
    }

    @Override
    public void startFeature(long fid) throws Exception {
        startFeature(String.valueOf(fid));
    }

    @Override
    public void endFeature() throws Exception {
        xml.writeEndElement(); // Close AbstractObject
    }

    
}
