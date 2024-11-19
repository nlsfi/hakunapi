package fi.nls.hakunapi.source.gpkg;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;

import fi.nls.hakunapi.core.FeatureStream;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.ValueProvider;
import fi.nls.hakunapi.core.config.HakunaConfigParser;
import fi.nls.hakunapi.core.geom.HakunaGeometry;
import fi.nls.hakunapi.core.geom.HakunaGeometryType;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyType;
import fi.nls.hakunapi.core.request.GetFeatureCollection;
import fi.nls.hakunapi.core.request.GetFeatureRequest;

public class GpkgSimpleSourceTest {

    private static HakunaConfigParser cfg;
    private static GpkgSimpleSource source;

    @BeforeClass
    public static void setup() throws IOException {
        Properties prop = new Properties();
        try (InputStream in = GpkgSimpleSourceTest.class.getClassLoader().getResourceAsStream("sample_feature_table.properties");
                Reader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            prop.load(r);
        }

        String gpkgFile = GpkgSimpleSourceTest.class.getClassLoader().getResource("sample_feature_table.gpkg").getFile();
        prop.setProperty("collections.sample_feature_table.db", gpkgFile);

        cfg = new HakunaConfigParser(prop);
        source = new GpkgSimpleSource();
    }

    @Test
    public void testParsingFeatureType() throws Exception {
        FeatureType ft = cfg.readCollection(null, Map.of(source.getType(), source), "sample_feature_table");
        assertEquals("sample_feature_table", ft.getTitle());
        assertEquals("", ft.getDescription());
        assertEquals(5, ft.getProperties().size());
        assertEquals(HakunaGeometryType.POINT, ft.getGeom().getGeometryType());
        assertEquals(3067, ft.getGeom().getStorageSRID());
        Map<String, HakunaProperty> propertiesByName = ft.getProperties()
                .stream()
                .collect(Collectors.toMap(x -> x.getName(), x -> x));
        assertEquals(HakunaPropertyType.INT, propertiesByName.get("int_attribute").getType());
        assertEquals(HakunaPropertyType.STRING, propertiesByName.get("text_attribute").getType());
        assertEquals(HakunaPropertyType.DOUBLE, propertiesByName.get("real_attribute").getType());
        assertEquals(HakunaPropertyType.BOOLEAN, propertiesByName.get("boolean_attribute").getType());
        assertEquals(HakunaPropertyType.TIMESTAMPTZ, propertiesByName.get("last_modified").getType());
    }

    @Test
    public void testReadingFeatures() throws Exception {
        FeatureType ft = cfg.readCollection(null, Map.of(source.getType(), source), "sample_feature_table");
        GetFeatureRequest request = new GetFeatureRequest();
        request.setSRID(3067);
        request.setLimit(1);
        GetFeatureCollection collection = new GetFeatureCollection(ft);
        List<HakunaProperty> properties = collection.getProperties();
        InMemoryFeatureWriter writer = new InMemoryFeatureWriter();
        try (FeatureStream fs = ft.getFeatureProducer().getFeatures(request, collection)) {
            writer.startFeatureCollection(ft, "test");
            while (fs.hasNext()) {
                ValueProvider vp = fs.next();
                int i = 0;
                for (HakunaProperty property : properties) {
                    property.write(vp, i++, writer);
                }
                writer.endFeature();
            }
            writer.endFeatureCollection();
        }

        assertEquals(1, writer.written.size());
        List<Object> feature = writer.written.get(0);

        long expectedIdAttribute = 1;
        long actualIdAttribute = feature.stream()
                .filter(x -> x.getClass() == Long.class)
                .map(x -> (long) x)
                .findAny().get();
        assertEquals(expectedIdAttribute, actualIdAttribute);

        Geometry expectedGeometry = new WKTReader().read("POINT (500000 6770000)");
        Geometry actualGeometry = feature.stream()
                .filter(x -> x instanceof HakunaGeometry)
                .map(HakunaGeometry.class::cast)
                .map(HakunaGeometry::toJTSGeometry)
                .findAny().get();
        assertEquals(expectedGeometry, actualGeometry);

        int expectedIntAttribute = 13;
        int actualIntAttribute = feature.stream()
                .filter(x -> x.getClass() == Integer.class)
                .map(x -> (int) x)
                .findAny().get();
        assertEquals(expectedIntAttribute, actualIntAttribute);

        String expectedTextAttribute = "hello world";
        String actualTextAttribute = feature.stream()
                .filter(x -> x.getClass() == String.class)
                .map(String.class::cast)
                .findAny().get();
        assertEquals(expectedTextAttribute, actualTextAttribute);

        double expectedRealAttribute = 0.3333;
        double actualRealAttribute = feature.stream()
                .filter(x -> x.getClass() == Double.class)
                .map(x -> (double) x)
                .findAny().get();
        assertEquals(expectedRealAttribute, actualRealAttribute, 1e-10);

        boolean expectedBooleanAttribute = false;
        boolean actualBooleanAttribute = feature.stream()
                .filter(x -> x.getClass() == Boolean.class)
                .map(x -> (boolean) x)
                .findAny().get();
        assertEquals(expectedBooleanAttribute, actualBooleanAttribute);

        String expectedLastModified = OffsetDateTime.parse("2024-11-19T14:57:22.590+02:00").toInstant().toString();
        String actualLastModified = feature.stream()
                .filter(x -> x.getClass() == Instant.class)
                .map(x -> x.toString())
                .findAny().get();
        assertEquals(expectedLastModified, actualLastModified);
    }

}
