package fi.nls.hakunapi.geojson.hakuna;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import fi.nls.hakunapi.core.FeatureWriter;
import fi.nls.hakunapi.core.FloatingPointFormatter;
import fi.nls.hakunapi.core.GeometryWriter;
import fi.nls.hakunapi.core.SRIDCode;
import fi.nls.hakunapi.core.geom.HakunaGeometry;
import fi.nls.hakunapi.core.schemas.Link;

public abstract class HakunaGeoJSONWriter implements FeatureWriter {
    
    protected static final byte[] GEOMETRY = "geometry".getBytes(StandardCharsets.UTF_8);

    protected static final byte[] TIMESTAMP = "timeStamp".getBytes(StandardCharsets.UTF_8);
    protected static final byte[] LINKS = "links".getBytes(StandardCharsets.UTF_8);
    protected static final byte[] NUMBER_RETURNED = "numberReturned".getBytes(StandardCharsets.UTF_8);

    protected static final byte[] HREF = "href".getBytes(StandardCharsets.UTF_8);
    protected static final byte[] REL = "rel".getBytes(StandardCharsets.UTF_8);
    protected static final byte[] TYPE = "type".getBytes(StandardCharsets.UTF_8);
    protected static final byte[] TITLE = "title".getBytes(StandardCharsets.UTF_8);
    protected static final byte[] HREFLANG = "hreflang".getBytes(StandardCharsets.UTF_8);

    private static final Set<String> RESERVED_ROOT_PROPERTIES = new HashSet<>(
            Arrays.asList("type", "features", "geometry", "properties", "coordinates", "geometries", "bbox", "crs", "timeStamp", "links", "numberReturned", "numberMatched")
    );

    protected boolean forceLonLat;
    protected Function<Boolean, FloatingPointFormatter> formatterFactory;

    protected SRIDCode srid;
    protected HakunaJsonWriter json;
    protected GeometryWriter geometryJson;
    
    protected Map<String, byte[]> propertyNameUTF8Cache = new HashMap<>();
    protected Map<String, GeometryWriter> propertyGeometryJson = new HashMap<>();

    protected boolean propertiesOpen = false;
    protected boolean geometryCached = false;
    protected HakunaGeometry cachedGeometry;
    
    public void setFormatterFactory(Function<Boolean, FloatingPointFormatter> formatterFactory) {
        this.formatterFactory = formatterFactory;
    }

    public void setForceLonLat(boolean forceLonLat) {
        this.forceLonLat = forceLonLat;
    }

    @Override
    public void init(OutputStream out, SRIDCode srid) throws IOException {
        this.srid = srid;
        this.json = new HakunaJsonWriter(out, formatterFactory.apply(srid.isDegrees()));
        geometryJson = createGeometryWriter(HakunaGeoJSON.GEOMETRY, srid);
        propertyGeometryJson.clear();
    }

    @Override
    public void close() throws IOException {
        // close FeatureCollection or Feature
        if (json != null) {
            json.writeEndObject();
            json.close();
            json = null;
        }
    }

    @Override
    public void writeTimeStamp() throws IOException {
        json.writeFieldName(TIMESTAMP);
        json.writeString(Instant.now().toString());
    }

    @Override
    public void writeLinks(List<Link> links) throws IOException {
        if (links != null && !links.isEmpty()) {
            json.writeFieldName(LINKS);
            json.writeStartArray();
            for (Link link : links) {
                writeLink(link);
            }
            json.writeEndArray();
        }
    }

    private void writeLink(Link link) throws IOException {
        json.writeStartObject();
        json.writeStringField(HREF, link.getHref());
        json.writeStringField(REL, link.getRel());
        json.writeStringField(TYPE, link.getType());
        if (link.getTitle() != null && !link.getTitle().isEmpty()) {
            json.writeStringField(TITLE, link.getTitle());
        }
        if (link.getHreflang() != null && !link.getHreflang().isEmpty()) {
            json.writeStringField(HREFLANG, link.getHreflang());
        }
        json.writeEndObject();
    }

    @Override
    public void writeMetadata(String key, Map<String, Object> metadata) throws Exception {
        if (key != null && metadata != null) {
            if (RESERVED_ROOT_PROPERTIES.contains(key)) {
                throw new IllegalArgumentException("Key " + key + " is a reserved keyword");
            }
            writeAnyObject(key, metadata);
        }
    }

    @SuppressWarnings("unchecked")
    private void writeAnyObject(String k, Object v) throws Exception {
        if (k != null) {
            json.writeFieldName(k.getBytes(StandardCharsets.UTF_8));
        }

        if (v == null) {
            json.writeNull();
        } else if (v instanceof Boolean) {
            json.writeBoolean((Boolean) v);
        } else if (v instanceof Integer) {
            json.writeNumber((Integer) v);
        } else if (v instanceof Long) {
            json.writeNumber((Long) v);
        } else if (v instanceof Float) {
            json.writeNumber((Float) v);
        } else if (v instanceof Double) {
            json.writeNumber((Double) v);
        } else if (v instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) v;
            json.writeStartObject();
            for (Map.Entry<String, Object> e : map.entrySet()) {
                writeAnyObject(e.getKey(), e.getValue());
            }
            json.writeEndObject();
        } else if (v instanceof List) {
            List<Object> list = (List<Object>) v;
            json.writeStartArray();
            for (Object o : list) {
                writeAnyObject(null, o);
            }
            json.writeEndArray();
        } else {
            json.writeString(v.toString());
        }
    }

    @Override
    public void writeNumberReturned(int numberReturned) throws IOException {
        if (numberReturned < 0) {
            return;
        }
        json.writeFieldName(NUMBER_RETURNED);
        json.writeNumber(numberReturned);
    }

    @Override
    public void writeGeometry(String name, HakunaGeometry geometry) throws Exception {
        if (propertiesOpen) {
            geometryCached = true;
            cachedGeometry = geometry;
        } else {
            if (geometry == null) {
                json.writeFieldName(GEOMETRY);
                json.writeNull();
            } else {
                geometry.write(geometryJson);
            }
        }
    }

    @Override
    public void writeProperty(String name, HakunaGeometry geometry) throws Exception {
        openProperties();
        geometry.write(propertyGeometryJson.computeIfAbsent(name, __ -> createGeometryWriter(name, srid)));
    }

    @Override
    public void writeProperty(String name, String value) throws IOException {
        if (value != null) {
            openProperties();
            if (name != null) {
                json.writeStringField(getProperty(name), value);
            } else {
                json.writeString(value);
            }
        }
    }

    @Override
    public void writeProperty(String name, Instant value) throws Exception {
        if (value != null) {
            openProperties();
            if (name != null) {
                json.writeFieldName(getProperty(name));
            }
            json.writeStringUnsafe(value.toString());
        }
    }

    @Override
    public void writeProperty(String name, LocalDate value) throws Exception {
        if (value != null) {
            openProperties();
            if (name != null) {
                json.writeFieldName(getProperty(name));
            }
            json.writeStringUnsafe(value.toString());
        }
    }

    @Override
    public void writeProperty(String name, boolean value) throws IOException {
        openProperties();
        if (name != null) {
            json.writeFieldName(getProperty(name));
        }
        json.writeBoolean(value);
    }

    @Override
    public void writeProperty(String name, int value) throws IOException {
        openProperties();
        if (name != null) {
            json.writeFieldName(getProperty(name));
        }
        json.writeNumber(value);
    }

    @Override
    public void writeProperty(String name, long value) throws IOException {
        openProperties();
        if (name != null) {
            json.writeFieldName(getProperty(name));
        }
        json.writeNumber(value);
    }

    @Override
    public void writeProperty(String name, float value) throws IOException {
        openProperties();
        if (name != null) {
            json.writeFieldName(getProperty(name));
        }
        json.writeNumber(value);
    }

    @Override
    public void writeProperty(String name, double value) throws IOException {
        openProperties();
        if (name != null) {
            json.writeFieldName(getProperty(name));
        }
        json.writeNumber(value);
    }

    @Override
    public void writeAttribute(String name, String value) throws Exception {
        writeProperty(name, value);
    }

    public void writeNullProperty(String name) throws IOException {
        openProperties();
        if (name != null) {
            json.writeNullField(getProperty(name));
        } else {
            json.writeNull();
        }
    }

    protected void openProperties() throws IOException {
        if (!propertiesOpen) {
            json.writeFieldName(HakunaGeoJSON.PROPERTIES);
            json.writeStartObject();
            propertiesOpen = true;
        }
    }

    protected void closeProperties() throws IOException {
        if (propertiesOpen) {
            json.writeEndObject();
            propertiesOpen = false;
        }
    }

    @Override
    public void writeStartObject(String name) throws IOException {
        openProperties();
        if (name != null && !json.insideArray()) {
            json.writeFieldName(getProperty(name));
        }
        json.writeStartObject();
    }

    @Override
    public void writeCloseObject() throws IOException {
        json.writeEndObject();
    }

    @Override
    public void writeStartArray(String name) throws IOException {
        openProperties();
        if (name != null) {
            json.writeFieldName(getProperty(name));
        }
        json.writeStartArray();
    }

    @Override
    public void writeCloseArray() throws IOException {
        json.writeEndArray();
    }
    
    public void  writeJsonProperty(String name, byte[] value) throws IOException {
        
        if (value != null) {
            openProperties();
            if (name != null) {
                json.writeFieldName(getProperty(name));
            }
            json.writeJson(value);
        }
    }

    protected GeometryWriter createGeometryWriter(String name, SRIDCode srid) {
        return createGeometryWriter(name.getBytes(StandardCharsets.UTF_8), srid);
    }

    protected GeometryWriter createGeometryWriter(byte[] name, SRIDCode srid) {
        FloatingPointFormatter f = formatterFactory.apply(srid.isDegrees());
        boolean lonLat = forceLonLat || !srid.isLatLon();
        return new HakunaGeoJSONGeometryXYZMWriter(json, f, name, lonLat, srid.getDimension());
    }

    private byte[] getProperty(String name) {
        byte[] buf = propertyNameUTF8Cache.get(name);
        if (buf == null) {
            buf = name.getBytes(StandardCharsets.UTF_8);
            propertyNameUTF8Cache.put(name, buf);
        }
        return buf;
    }

}
