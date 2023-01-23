package fi.nls.hakunapi.gml;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.SingleFeatureWriter;

public class WFS110SingleFeatureWriter extends WFS110FeatureWriterBase implements SingleFeatureWriter {

    @Override
    public String getMimeType() {
        return WFS110FeatureCollectionWriter.MIME_TYPE;
    }

    @Override
    public void startFeature(FeatureType ft, String layername, long fid) throws Exception {
        startFeature(ft, layername, String.valueOf(fid));
    }

    @Override
    public void startFeature(FeatureType ft, String layername, String fid) throws Exception {
        setFeatureType(ft);

        List<Namespace> namespaces = Arrays.stream(Namespaces.values())
                .map(e -> e.ns)
                .collect(Collectors.toList());

        xml.setDefaultNamespace(namespaceURI);
        xml.writeStartElement(namespaceURI, currentFeatureTypename); // <MyFeature>
        xml.writeDefaultNamespace(namespaceURI);

        writeNamespaces(namespaces);
        xml.writeAttribute(Namespaces.GML_311.ns.uri, "id", fid);

        namespaces.add(new Namespace(null, namespaceURI, ft.getSchemaLocation()));
        writeSchemaLocation(namespaces);
    }

    @Override
    public void endFeature() throws Exception {
        xml.writeEndElement(); // </MyFeature>
    }
    
}
