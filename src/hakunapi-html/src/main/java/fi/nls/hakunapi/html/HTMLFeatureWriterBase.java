package fi.nls.hakunapi.html;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import freemarker.core.Environment;
import freemarker.template.Configuration;
import fi.nls.hakunapi.core.FeatureWriter;
import fi.nls.hakunapi.core.FloatingPointFormatter;
import fi.nls.hakunapi.core.GeometryWriter;
import fi.nls.hakunapi.core.geom.HakunaGeometry;
import fi.nls.hakunapi.core.schemas.Link;
import fi.nls.hakunapi.geojson.hakuna.HakunaJsonWriter;
import fi.nls.hakunapi.html.model.HTMLFeature;
import fi.nls.hakunapi.html.model.HakunaGeoJSONGeometryWriterNoField;
import fi.nls.hakunapi.html.model.PropertiesContext;

public abstract class HTMLFeatureWriterBase implements FeatureWriter {

    protected final Configuration configuration;

    protected OutputStream out;

    protected Environment environment;

    protected FloatingPointFormatter formatter;

    protected ByteArrayOutputStream geojsonBuffer;
    protected HakunaJsonWriter jsonWriter;
    protected GeometryWriter geometryWriter;

    protected HTMLFeature feature;
    protected PropertiesContext properties;

    protected int srid;
    
    public HTMLFeatureWriterBase(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public String getMimeType() {
        return OutputFormatHTML.MIME_TYPE;
    }

    @Override
    public int getSrid() {
        return srid;
    }

    @Override
    public void init(OutputStream out, FloatingPointFormatter formatter, int srid) {
        this.srid = srid;
        this.out = out;
        this.formatter = formatter;
        this.geojsonBuffer = new ByteArrayOutputStream();
        this.jsonWriter = new HakunaJsonWriter(geojsonBuffer, formatter);
        this.geometryWriter = new HakunaGeoJSONGeometryWriterNoField(jsonWriter);
    }

    @Override
    public void close() throws Exception {
        if (environment != null) {
            try {
                environment.process();
            } catch (IOException e) {
                // Ignore
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    @Override
    public void writeGeometry(String name, HakunaGeometry geometry) throws Exception {
        geojsonBuffer.reset();
        geometry.write(geometryWriter);
        jsonWriter.flush();
        feature.setGeometry(geojsonBuffer.toString(StandardCharsets.UTF_8.name()));
    }

    @Override
    public void writeProperty(String name, HakunaGeometry geometry) throws Exception {
        /* Ignore secondary geometries for now
        geojsonBuffer.reset();
        geometry.write(geometryWriter);
        jsonWriter.flush();
        properties.add(name, geojsonBuffer.toString(StandardCharsets.UTF_8.name()));
        */
    }

    @Override
    public void writeAttribute(String name, String value) throws Exception {
        writeProperty(name, value);
    }

    @Override
    public void writeProperty(String name, String value) {
        properties.add(name, value);
    }

    @Override
    public void writeProperty(String name, Instant value) throws Exception {
        if (value == null) {
            writeNullProperty(name);
        } else {
            writeProperty(name, value.toString());
        }
    }

    @Override
    public void writeProperty(String name, LocalDate value) throws Exception {
        if (value == null) {
            writeNullProperty(name);
        } else {
            writeProperty(name, value.toString());
        }
    }

    @Override
    public void writeProperty(String name, boolean value) {
        properties.add(name, value ? "true" : "false");
    }

    @Override
    public void writeProperty(String name, int value) {
        properties.add(name, Integer.toString(value));
    }

    @Override
    public void writeProperty(String name, long value) {
        properties.add(name, Long.toString(value));
    }

    @Override
    public void writeProperty(String name, float value) {
        writeProperty(name, formatter.writeFloat(value));
    }

    @Override
    public void writeProperty(String name, double value) {
        writeProperty(name, formatter.writeDouble(value));
    }

    @Override
    public void writeNullProperty(String name) {
        properties.addNullProperty(name);
    }

    @Override
    public void writeStartObject(String name) throws Exception {
        properties = properties.addMap(name);
    }

    @Override
    public void writeCloseObject() throws Exception {
        properties = properties.getParent();
    }

    @Override
    public void writeStartArray(String name) throws Exception {
        properties = properties.addList(name);
    }

    @Override
    public void writeCloseArray() throws Exception {
        properties = properties.getParent();
    }

    @Override
    public void writeTimeStamp() {
        // NOP
    }

    @Override
    public void writeLinks(List<Link> link) {
        // NOP
    }

    @Override
    public void writeNumberReturned(int numberReturned) {
        // NOP
    }

}
