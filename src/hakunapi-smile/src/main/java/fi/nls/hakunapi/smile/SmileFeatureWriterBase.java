package fi.nls.hakunapi.smile;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;

import fi.nls.hakunapi.core.FeatureWriter;
import fi.nls.hakunapi.core.GeometryWriter;
import fi.nls.hakunapi.core.SRIDCode;
import fi.nls.hakunapi.core.geom.HakunaGeometry;
import fi.nls.hakunapi.core.schemas.Link;

public abstract class SmileFeatureWriterBase implements FeatureWriter {

    private static final SmileFactory FACTORY = new SmileFactory();

    protected OutputStream out;
    protected JsonGenerator w;

    protected boolean propertiesOpen = false;
    protected HakunaGeometry cachedGeometry;

    protected GeometryWriter mainGeometryWriter;

    protected Map<String, SerializableString> propertyNameCache = new HashMap<>();
    protected Map<String, GeometryWriter> propertyGeometryWriterCache = new HashMap<>();
    
    @Override
    public void init(OutputStream out, SRIDCode srid) throws Exception {
        this.out = out;
        this.w = FACTORY.createGenerator(out);
        this.mainGeometryWriter = new SmileGeometryWriter(w, GeoJSONStrings.GEOMETRY);
    }

    @Override
    public void close() throws Exception {
        w.close();
        out.close();
    }

    @Override
    public void writeTimeStamp() throws Exception {
        w.writeFieldName(GeoJSONStrings.TIMESTAMP);
        w.writeString(Instant.now().toString());
    }

    @Override
    public void writeLinks(List<Link> links) throws Exception {
        if (links != null && !links.isEmpty()) {
            w.writeFieldName(GeoJSONStrings.LINKS);
            w.writeStartArray();
            for (Link link : links) {
                writeLink(link);
            }
            w.writeEndArray();
        }
    }

    private void writeLink(Link link) throws IOException {
        w.writeStartObject();

        w.writeFieldName(GeoJSONStrings.HREF);
        w.writeString(link.getHref());

        w.writeFieldName(GeoJSONStrings.REL);
        w.writeString(link.getRel());

        w.writeFieldName(GeoJSONStrings.TYPE);
        w.writeString(link.getType());

        if (link.getTitle() != null && !link.getTitle().isEmpty()) {
            w.writeFieldName(GeoJSONStrings.TITLE);
            w.writeString(link.getTitle());
        }

        if (link.getHreflang() != null && !link.getHreflang().isEmpty()) {
            w.writeFieldName(GeoJSONStrings.HREFLANG);
            w.writeString(link.getHreflang());
        }

        w.writeEndObject();
    }

    @Override
    public void writeNumberReturned(int numberReturned) throws Exception {
        if (numberReturned >= 0) {
            w.writeFieldName(GeoJSONStrings.NUMBER_RETURNED);
            w.writeNumber(numberReturned);
        }
    }

    protected void openProperties() throws IOException {
        if (!propertiesOpen) {
            w.writeFieldName(GeoJSONStrings.PROPERTIES);
            w.writeStartObject();
            propertiesOpen = true;
        }
    }

    protected void closeProperties() throws IOException {
        if (propertiesOpen) {
            w.writeEndObject();
            propertiesOpen = false;
        }
    }

    @Override
    public void writeGeometry(String name, HakunaGeometry geometry) throws Exception {
        if (propertiesOpen) {
            cachedGeometry = geometry;
        } else {
            geometry.write(mainGeometryWriter);
        }
    }

    @Override
    public void writeProperty(String name, HakunaGeometry geometry) throws Exception {
        if (geometry != null) {
            openProperties();
            GeometryWriter writer = propertyGeometryWriterCache
                    .computeIfAbsent(name, k -> new SmileGeometryWriter(w, new SerializedString(k)));
            geometry.write(writer);
        }
    }

    @Override
    public void writeAttribute(String name, String value) throws Exception {
        writeProperty(name, value);
    }

    @Override
    public void writeProperty(String name, String value) throws Exception {
        if (value == null) {
            writeNullProperty(name);
        } else {
            openProperties();
            if (name != null) {
                w.writeFieldName(getProperty(name));
            }
            w.writeString(value);
        }
    }

    @Override
    public void writeProperty(String name, LocalDate value) throws Exception {
        if (value == null) {
            writeNullProperty(name);
        } else {
            openProperties();
            if (name != null) {
                w.writeFieldName(getProperty(name));
            }
            w.writeString(value.toString());
        }
    }

    @Override
    public void writeProperty(String name, Instant value) throws Exception {
        if (value == null) {
            writeNullProperty(name);
        } else {
            openProperties();
            if (name != null) {
                w.writeFieldName(getProperty(name));
            }
            w.writeString(value.toString());
        }
    }

    @Override
    public void writeProperty(String name, boolean value) throws Exception {
        openProperties();
        if (name != null) {
            w.writeFieldName(getProperty(name));
        }
        w.writeBoolean(value);
    }

    @Override
    public void writeProperty(String name, int value) throws Exception {
        openProperties();
        if (name != null) {
            w.writeFieldName(getProperty(name));
        }
        w.writeNumber(value);
    }

    @Override
    public void writeProperty(String name, long value) throws Exception {
        openProperties();
        if (name != null) {
            w.writeFieldName(getProperty(name));
        }
        w.writeNumber(value);
    }

    @Override
    public void writeProperty(String name, float value) throws Exception {
        openProperties();
        if (name != null) {
            w.writeFieldName(getProperty(name));
        }
        w.writeNumber(value);
    }

    @Override
    public void writeProperty(String name, double value) throws Exception {
        openProperties();
        if (name != null) {
            w.writeFieldName(getProperty(name));
        }
        w.writeNumber(value);
    }

    @Override
    public void writeNullProperty(String name) throws Exception {
        openProperties();
        if (name != null) {
            w.writeFieldName(getProperty(name));
        }
        w.writeNull();
    }

    @Override
    public void writeStartObject(String name) throws Exception {
        openProperties();
        if (name != null) {
            w.writeFieldName(getProperty(name));
        }
        w.writeStartObject();
    }

    @Override
    public void writeCloseObject() throws Exception {
        w.writeEndObject();
    }

    @Override
    public void writeStartArray(String name) throws Exception {
        openProperties();
        if (name != null) {
            w.writeFieldName(getProperty(name));
        }
        w.writeStartArray();
    }

    private SerializableString getProperty(String name) {
        return propertyNameCache.computeIfAbsent(name, __ -> new SerializedString(name));
    }

    @Override
    public void writeCloseArray() throws Exception {
        w.writeEndArray();
    }

}
