package fi.nls.hakunapi.geojson.hakuna;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;

import org.junit.Test;

import fi.nls.hakunapi.core.FeatureCollectionWriter;
import fi.nls.hakunapi.core.FeatureProducer;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.SRIDCode;
import fi.nls.hakunapi.core.SimpleFeatureType;
import fi.nls.hakunapi.core.geom.HakunaGeometry;
import fi.nls.hakunapi.core.geom.HakunaGeometryType;
import fi.nls.hakunapi.core.geom.HakunaPoint2D;
import fi.nls.hakunapi.core.property.HakunaPropertyWriters;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;
import fi.nls.hakunapi.esbulk.ESbulkFeatureCollectionWriter;

public class ESbulkFeatureCollectionWriterTest {

    @Test
    public void testWriteFeature() throws Exception {
        HakunaPropertyGeometry geomProp = new HakunaPropertyGeometry("geometry", "foo", "bar", false, HakunaGeometryType.POINT, new int[] { 84 }, 84, 2, HakunaPropertyWriters.getGeometryPropertyWriter("bar", true));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (FeatureCollectionWriter fw = new ESbulkFeatureCollectionWriter()) {
            fw.init(baos, SRIDCode.CRS84);
            fw.startFeatureCollection(getFeatureType(geomProp), "mylayer");

            fw.startFeature(1);
            fw.writeGeometry("geometry", getPointGeom(100.0, 50.0));
            fw.writeProperty("foo", "bar");
            fw.writeProperty("baz", 0);
            fw.writeProperty("qux", LocalDate.parse("2020-01-01"));
            fw.endFeature();

            fw.startFeature(2);
            fw.writeGeometry("geometry", getPointGeom(50.0, 100.0));
            fw.writeProperty("foo", "barx");
            fw.writeProperty("baz", 55);
            fw.writeProperty("qux", LocalDate.parse("2020-03-01"));
            fw.endFeature();

            fw.endFeatureCollection();
            fw.end(false, null, 1);
        }

        String actual = baos.toString();
        String expected = "{\"index\":{\"_index\":\"mylayer\"}}\n"
                + "{\"id\":1,\"geometry\":[100.0,50.0],\"foo\":\"bar\",\"baz\":0,\"qux\":\"2020-01-01\"}\n"
                + "{\"index\":{\"_index\":\"mylayer\"}}\n"
                + "{\"id\":2,\"geometry\":[50.0,100.0],\"foo\":\"barx\",\"baz\":55,\"qux\":\"2020-03-01\"}";

        assertEquals(expected, actual);
    }

    private FeatureType getFeatureType(HakunaPropertyGeometry geometryProperty) {
        SimpleFeatureType sft = new SimpleFeatureType() {
            @Override
            public FeatureProducer getFeatureProducer() {
                return null;
            }
        };
        sft.setGeom(geometryProperty);
        return sft;
    }

    private static HakunaGeometry getPointGeom(final double x, final double y) {
        return new HakunaPoint2D(x, y, 0);
    }

}
