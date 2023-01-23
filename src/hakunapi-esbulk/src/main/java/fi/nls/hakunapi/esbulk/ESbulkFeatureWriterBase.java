package fi.nls.hakunapi.esbulk;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.SerializedString;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.FeatureWriter;
import fi.nls.hakunapi.core.FloatingPointFormatter;
import fi.nls.hakunapi.core.geom.HakunaGeometry;
import fi.nls.hakunapi.core.geom.HakunaGeometryType;
import fi.nls.hakunapi.core.schemas.Link;

public abstract class ESbulkFeatureWriterBase implements FeatureWriter {

    private static final JsonFactory FACTORY = new JsonFactory();

    private OutputStream out;
    protected JsonGenerator w;

    private ESbulkGeometryWriter cachedGeometryWriter;

    private Map<String, SerializableString> propertyNameCache = new HashMap<>();
    private int srid;
    
    
    
    @Override
    public int getSrid() {
        return srid;
    }

    @Override
    public String getMimeType() {
        return OutputFormatESbulk.MIME_TYPE;
    }

    @Override
    public void init(OutputStream out, FloatingPointFormatter formatter, int srid) throws Exception {
        this.srid = srid;
        if (out instanceof BufferedOutputStream || out instanceof ByteArrayOutputStream) {
            this.out = out;
        } else {
            this.out = new BufferedOutputStream(out);
        }
        this.w = FACTORY.createGenerator(out, JsonEncoding.UTF8);
        this.w.setRootValueSeparator(Strings.NEW_LINE);
    }

    @Override
    public void close() throws Exception {
        w.close();
        out.close();
    }

    public void initGeometryWriter(FeatureType ft) {
        String name = ft.getGeom().getName();
        if (ft.getGeom().getGeometryType() == HakunaGeometryType.POINT) {
            cachedGeometryWriter = new ESbulkPointGeometryWriter(name);
        } else {
            cachedGeometryWriter = new ESbulkGeometryWriter(name);
        }
    }

    @Override
    public void writeGeometry(String name, HakunaGeometry geometry) throws Exception {
        if (geometry != null) {
            cachedGeometryWriter.setJsonGenerator(w);
            geometry.write(cachedGeometryWriter);
        }
    }

    @Override
    public void writeProperty(String name, HakunaGeometry geometry) throws Exception {
        // NOP - single geometry per index
    }

    @Override
    public void writeAttribute(String name, String value) throws Exception {
        writeProperty(name, value);
    }

    @Override
    public void writeProperty(String name, String value) throws Exception {
        if (value != null) {
            if (name != null) {
                w.writeFieldName(getProperty(name));
            }
            w.writeString(value);
        }
    }

    @Override
    public void writeProperty(String name, LocalDate value) throws Exception {
        if (value != null) {
            if (name != null) {
                w.writeFieldName(getProperty(name));
            }
            w.writeString(value.toString());
        }
    }

    @Override
    public void writeProperty(String name, Instant value) throws Exception {
        if (value != null) {
            if (name != null) {
                w.writeFieldName(getProperty(name));
            }
            w.writeString(value.toString());
        }
    }

    @Override
    public void writeProperty(String name, boolean value) throws Exception {
        if (name != null) {
            w.writeFieldName(getProperty(name));
        }
        w.writeBoolean(value);
    }

    @Override
    public void writeProperty(String name, int value) throws Exception {
        if (name != null) {
            w.writeFieldName(getProperty(name));
        }
        w.writeNumber(value);
    }

    @Override
    public void writeProperty(String name, long value) throws Exception {
        if (name != null) {
            w.writeFieldName(getProperty(name));
        }
        w.writeNumber(value);
    }

    @Override
    public void writeProperty(String name, float value) throws Exception {
        if (name != null) {
            w.writeFieldName(getProperty(name));
        }
        w.writeNumber(value);
    }

    @Override
    public void writeProperty(String name, double value) throws Exception {
        if (name != null) {
            w.writeFieldName(getProperty(name));
        }
        w.writeNumber(value);
    }

    @Override
    public void writeNullProperty(String name) throws Exception {
        // NOP
    }

    @Override
    public void writeStartObject(String name) throws Exception {
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
        if (name != null) {
            w.writeFieldName(getProperty(name));
        }
        w.writeStartArray();
    }

    @Override
    public void writeCloseArray() throws Exception {
        w.writeEndArray();
    }

    private SerializableString getProperty(String name) {
        return propertyNameCache.computeIfAbsent(name, __ -> new SerializedString(name));
    }

    protected void writeHeader(SerializedString layername) throws IOException {
        w.writeStartObject();
        w.writeFieldName(Strings.INDEX);
        w.writeStartObject();
        w.writeFieldName(Strings._INDEX);
        w.writeString(layername);
        w.writeEndObject();
        w.writeEndObject();
    }

    @Override
    public void end(boolean timeStamp, List<Link> links, int numberReturned) throws Exception {
        // NOP
    }

    @Override
    public void writeTimeStamp() throws Exception {
        // NOP
    }

    @Override
    public void writeLinks(List<Link> links) throws Exception {
        // NOP
    }

    @Override
    public void writeNumberReturned(int numberReturned) throws Exception {
        // NOP
    }

}
