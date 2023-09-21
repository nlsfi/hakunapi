package fi.nls.hakunapi.geojson.hakuna;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

import fi.nls.hakunapi.core.FeatureCollectionWriter;
import fi.nls.hakunapi.core.FloatingPointFormatter;
import fi.nls.hakunapi.core.SingleFeatureWriter;
import fi.nls.hakunapi.core.geom.HakunaGeometry;
import fi.nls.hakunapi.core.geom.HakunaGeometryDimension;
import fi.nls.hakunapi.core.geom.HakunaGeometryEWKB;
import fi.nls.hakunapi.core.geom.HakunaPoint2D;
import fi.nls.hakunapi.core.schemas.Crs;
import fi.nls.hakunapi.core.util.DefaultFloatingPointFormatter;

public class HakunaGeoJSONWriterTest {

    static final int SRID = 84;

    @Test
    public void testWriteFeature() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (SingleFeatureWriter fw = new HakunaGeoJSONSingleFeatureWriter()) {
            fw.init(baos, new DefaultFloatingPointFormatter(0, 5, 0, 8, 0, 5),84);
            fw.startFeature(null, null, 1L);
            fw.writeGeometry("ignored", getPointGeom(130, 50));
            fw.writeProperty("foo", "bar");
            fw.writeProperty("prop0", 0);
            fw.writeProperty("nimi", "\"SOLSIDA\"");
            fw.writeNullProperty("prop1");
            fw.writeProperty("another_geom", getPointGeom(130, 50));
            fw.endFeature();
            fw.end(false, Collections.emptyList(), 1);
        }
        byte[] expecteds = loadResource("expect.json");
        byte[] actuals = baos.toByteArray();
        assertArrayEquals(expecteds, actuals);
    }

    @Test
    public void testWriteFeatureLatLon() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (SingleFeatureWriter fw = new HakunaGeoJSONSingleFeatureWriter()) {
            boolean crsIsLatLon = true;
            fw.init(baos, new DefaultFloatingPointFormatter(0, 5, 0, 8, 0, 5), 4326, crsIsLatLon);
            fw.startFeature(null, null, 1L);
            fw.writeGeometry("ignored", getPointGeom(130, 50));
            fw.writeProperty("foo", "bar");
            fw.writeProperty("prop0", 0);
            fw.writeProperty("nimi", "\"SOLSIDA\"");
            fw.writeNullProperty("prop1");
            fw.writeProperty("another_geom", getPointGeom(130, 50));
            fw.endFeature();
            fw.end(false, Collections.emptyList(), 1);
        }
        byte[] expecteds = loadResource("expect_latlon.json");
        byte[] actuals = baos.toByteArray();
        assertArrayEquals(expecteds, actuals);
    }

    private static HakunaGeometry getPointGeom(final double x, final double y) {
        return new HakunaPoint2D(x, y, 0);
    }

    private byte[] loadResource(String path) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (InputStream in = this.getClass().getClassLoader().getResourceAsStream(path)) {
            byte[] b = new byte[8192];
            int n;
            while ((n = in.read(b)) != -1) {
                baos.write(b, 0, n);
            }
        }
        return baos.toByteArray();
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    @Test
    public void testWriteMultiPolygon() throws Exception {
        String multiPoly = "01060000a0e610000001000000010300008001000000350000001af5522278c53740cf27993ce2bf4e400000000000605640aec4ffc875c537409b7ce827e2bf4e4000000000006056408c5c7ebd6ac53740557bb5c3e1bf4e4000000000006056404ead936867c53740e5b1359ce1bf4e400000000000605640e2a95f5868c53740fefbc81fe0bf4e400000000000605640b8cc1bd568c5374011951061dfbf4e400000000000605640a89619e568c537407b1b9941dfbf4e4000000000006056407010e58c69c53740025b293cdebf4e4000000000006056402858425c6bc537408f1ae863dbbf4e40000000000060564033a27a496cc53740c145e3f3d9bf4e40000000000060564006b423d76cc5374042833011d9bf4e4000000000006056407c69801d6dc53740769118a2d8bf4e4000000000006056409c36f81d6dc53740d921d79fd8bf4e400000000000605640b58d748e6dc537401db58aeed7bf4e40000000000060564066f9eb656cc53740524edf19d7bf4e40000000000060564060080b166ac53740632b4b05d7bf4e40000000000060564051e6cb4462c537401eb05cc0d6bf4e4000000000006056408716cf445ec53740ae127d9cd6bf4e400000000000605640bbb767f258c53740526cd26dd6bf4e4000000000006056406a610d8b54c53740664d7646d6bf4e40000000000060564035daf90057c5374005bb2f41d2bf4e4000000000006056407b5b936857c53740d8608b43d2bf4e4000000000006056405527698357c53740d13a6c1dd2bf4e40000000000060564078d4b43c5dc5374002b2d451d2bf4e4000000000006056406998eb355dc53740d6838972d2bf4e40000000000060564097dda0a765c53740b3cf5eb9d2bf4e4000000000006056404bbf55c465c53740c8e9398ad2bf4e400000000000605640c88268b171c5374089fd9ef5d2bf4e4000000000006056409d45889371c53740dc7b672ad3bf4e400000000000605640f40be2fa75c537403d22c351d3bf4e400000000000605640c23fdefa79c537401a66a275d3bf4e4000000000006056405623c4147ac53740ad1b0654d3bf4e400000000000605640e9aa8aa87fc537408ca2da86d3bf4e400000000000605640384181d77fc53740bd798a88d3bf4e40000000000060564006e3f71e82c537407a09854ed4bf4e400000000000605640406acc1d82c537409aa02854d4bf4e400000000000605640edc48c4981c53740351f14a7d5bf4e40000000000060564071a582d880c537405be99ba4d5bf4e4000000000006056403104eaf07fc537409b968ff9d6bf4e400000000000605640baad5b917fc5374042b1e186d7bf4e400000000000605640c3ce5eb47ec53740de654ed6d8bf4e40000000000060564082a683957fc5374097b2c1dfd8bf4e4000000000006056403d23de727ec537404836e49adabf4e4000000000006056401f6f22a47dc537401898eb93dabf4e400000000000605640a260990f7cc5374056db1e07ddbf4e400000000000605640c4d389e77cc537409dd2540fddbf4e400000000000605640d73454ce7bc537407cf293cadebf4e400000000000605640b5c427f67ac5374010b27ec3debf4e400000000000605640232d7d307ac5374054fdb6fddfbf4e400000000000605640e7befdb379c5374014b54ebbe0bf4e400000000000605640e08e05d378c537401698e71de2bf4e4000000000006056402a217c3378c537402e167e17e2bf4e4000000000006056401af5522278c53740cf27993ce2bf4e400000000000605640";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FloatingPointFormatter f = new DefaultFloatingPointFormatter(0, 5, 0, 5, 0, 13);
        try (SingleFeatureWriter fw = new HakunaGeoJSONSingleFeatureWriter()) {
            fw.init(baos, f, SRID);
            fw.initGeometryWriter(HakunaGeometryDimension.GEOMETRY);
            fw.startFeature(null, null, 1L);
            fw.writeGeometry("ignored", new HakunaGeometryEWKB(hexStringToByteArray(multiPoly)));
            fw.endFeature();
            fw.end(false, Collections.emptyList(), 1);
        }
        byte[] expecteds = loadResource("expectMultipoly.json");
        byte[] actuals = baos.toByteArray();
        assertArrayEquals(expecteds, actuals);
    }

    @Test
    public void testWriteMultiPolygon2() throws Exception {
        String multiPoly2 = "01060000000200000001030000000100000005000000000000000000000000000000000000000000000000002440000000000000000000000000000024400000000000002440000000000000000000000000000024400000000000000000000000000000000001030000000100000005000000000000000000344000000000000034400000000000003E4000000000000034400000000000003E400000000000003E4000000000000034400000000000003E4000000000000034400000000000003440";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FloatingPointFormatter f = new DefaultFloatingPointFormatter(0, 5, 0, 5, 0, 13);
        try (SingleFeatureWriter fw = new HakunaGeoJSONSingleFeatureWriter()) {
            fw.init(baos, f,SRID);
            fw.startFeature(null, null, 1L);
            fw.writeGeometry("ignored", new HakunaGeometryEWKB(hexStringToByteArray(multiPoly2)));
            fw.endFeature();
            fw.end(false, Collections.emptyList(), 1);
        }
        byte[] expecteds = loadResource("expectMultipoly2.json");
        byte[] actuals = baos.toByteArray();
        assertArrayEquals(expecteds, actuals);
    }

    @Test
    public void testWriteLineString() throws Exception {
        String linestring = "0102000020fb0b000002000000ec51b81e02b71a41333333d393ef5c41ec51b81e5e231b413333335393ee5c41";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FloatingPointFormatter f = new DefaultFloatingPointFormatter(0, 5, 0, 5, 0, 13);
        try (SingleFeatureWriter fw = new HakunaGeoJSONSingleFeatureWriter()) {
            fw.init(baos, f,SRID);
            fw.startFeature(null, null, 1L);
            fw.writeGeometry("ignored", new HakunaGeometryEWKB(hexStringToByteArray(linestring)));
            fw.endFeature();
            fw.end(false, Collections.emptyList(), 1);
        }
        byte[] expecteds = loadResource("expectLineString.json");
        byte[] actuals = baos.toByteArray();
        assertArrayEquals(expecteds, actuals);
    }

    @Test
    public void testWriteMetadata() throws Exception {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("foo", 1);
        metadata.put("bar", "hello, world");
        metadata.put("baz", Collections.singletonList(Collections.singletonMap("qux", Collections.emptyList())));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (SingleFeatureWriter fw = new HakunaGeoJSONSingleFeatureWriter()) {
            fw.init(baos, new DefaultFloatingPointFormatter(0, 5, 0, 8, 0, 5),SRID);
            fw.startFeature(null, null, 1L);
            fw.writeGeometry("ignored", getPointGeom(100.0, 200.0));
            fw.writeProperty("foo", "bar");
            fw.writeProperty("prop0", 0);
            fw.writeNullProperty("prop1");
            fw.writeProperty("another_geom", getPointGeom(100.0, 200.0));
            fw.endFeature();
            fw.end(false, Collections.emptyList(), 1);
            fw.writeMetadata("meta", metadata);
        }
        byte[] expecteds = loadResource("expectMetadata.json");
        byte[] actuals = baos.toByteArray();
        assertArrayEquals(expecteds, actuals);
    }

    @Test
    public void testWriteCollectionMetadata() throws Exception {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("foo", 1);
        metadata.put("bar", "hello, world");
        metadata.put("baz", Collections.singletonList(Collections.singletonMap("qux", Collections.emptyList())));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (FeatureCollectionWriter fw = new HakunaGeoJSONFeatureCollectionWriter()) {
            fw.init(baos, new DefaultFloatingPointFormatter(0, 5, 0, 8, 0, 5), SRID);
            fw.startFeatureCollection(null, null);
            fw.startFeature(1L);
            fw.writeGeometry("ignored", getPointGeom(100.0, 200.0));
            fw.writeProperty("foo", "bar");
            fw.writeProperty("prop0", 0);
            fw.writeNullProperty("prop1");
            fw.writeProperty("another_geom", getPointGeom(100.0, 200.0));
            fw.endFeature();
            fw.endFeatureCollection();
            fw.end(false, Collections.emptyList(), 1);
            fw.writeMetadata("meta", metadata);
        }
        byte[] expecteds = loadResource("expectMetadataCollection.json");
        byte[] actuals = baos.toByteArray();
        assertArrayEquals(expecteds, actuals);
    }

    @Test
    public void testWriteNullGeometryFeatureCollection() throws Exception {
        FloatingPointFormatter f = new DefaultFloatingPointFormatter(0, 5, 0, 5, 0, 13);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (FeatureCollectionWriter fw = new HakunaGeoJSONFeatureCollectionWriter()) {
            fw.startFeatureCollection(null, null);

            fw.init(baos, f, SRID);
            fw.initGeometryWriter(HakunaGeometryDimension.GEOMETRY);

            fw.startFeature(1L);
            fw.writeProperty("test", true);
            fw.writeGeometry("ignored", new HakunaPoint2D(20.0, 40.0, Crs.CRS84_SRID));
            fw.endFeature();

            fw.startFeature(2L);
            fw.writeProperty("test", false);
            fw.writeGeometry("ignored", null);
            fw.endFeature();

            fw.endFeatureCollection();
            fw.end(false, Collections.emptyList(), 2);
        }
        String actual = baos.toString(StandardCharsets.UTF_8.name());
        String expected = "{"
                + "'type':'FeatureCollection',"
                + "'features':["
                + "{'type':'Feature','id':1,'properties':{'test':true},'geometry':{'type':'Point','coordinates':[20,40]}},"
                + "{'type':'Feature','id':2,'properties':{'test':false},'geometry':null}],"
                + "'numberReturned':2"
                + "}";
        expected = expected.replace('\'', '"');
        assertEquals(expected, actual);
    }

    @Test
    public void testWriteNullGeometrySingleFeature() throws Exception {
        FloatingPointFormatter f = new DefaultFloatingPointFormatter(0, 5, 0, 5, 0, 13);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (SingleFeatureWriter fw = new HakunaGeoJSONSingleFeatureWriter()) {
            fw.init(baos, f, SRID);
            fw.initGeometryWriter(HakunaGeometryDimension.GEOMETRY);

            fw.startFeature(null, null, 2L);
            fw.writeProperty("test", false);
            fw.writeGeometry("ignored", null);
            fw.endFeature();

            fw.end(false, Collections.emptyList(), 2);
        }
        String actual = baos.toString(StandardCharsets.UTF_8.name());
        String expected = "{'type':'Feature','id':2,'properties':{'test':false},'geometry':null}".replace('\'', '"');
        assertEquals(expected, actual);
    }

}
