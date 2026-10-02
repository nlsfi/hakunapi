package fi.nls.hakunapi.csv;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.junit.Test;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.GeometryFactory;

import fi.nls.hakunapi.core.FeatureCollectionWriter;
import fi.nls.hakunapi.core.FeatureProducer;
import fi.nls.hakunapi.core.SRIDCode;
import fi.nls.hakunapi.core.SimpleFeatureType;
import fi.nls.hakunapi.core.geom.HakunaGeometryDimension;
import fi.nls.hakunapi.core.geom.HakunaGeometryJTS;
import fi.nls.hakunapi.core.geom.HakunaGeometryType;
import fi.nls.hakunapi.core.property.HakunaPropertyType;
import fi.nls.hakunapi.core.property.HakunaPropertyWriters;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyDouble;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyLong;

public class CSVFeatureCollectionWriterTest {

    @Test
    public void testWritesNumbersWithoutConfiguredFormatter() throws Exception {
        assertEquals("id,geom,value\n1,\"POINT(385000.125 6672000.5)\",1.25\n",
                write(new SRIDCode(3067, false, false, HakunaGeometryDimension.XY), 385000.1254, 6672000.5));
        assertEquals("id,geom,value\n1,\"POINT(24.9384567 60.1698557)\",1.25\n",
                write(SRIDCode.CRS84, 24.93845671, 60.16985571));
    }

    private String write(SRIDCode srid, double x, double y) throws Exception {
        SimpleFeatureType ft = new SimpleFeatureType() {
            @Override
            public FeatureProducer getFeatureProducer() {
                return null;
            }
        };
        ft.setName("test");
        ft.setId(new HakunaPropertyLong("id", "table", "id", false, true,
                HakunaPropertyWriters.getIdPropertyWriter(ft, "test", "id", HakunaPropertyType.LONG)));
        ft.setGeom(new HakunaPropertyGeometry("geom", "table", "geom", true, HakunaGeometryType.POINT,
                new int[] { srid.getSrid() }, srid.getSrid(), 2, HakunaPropertyWriters.getGeometryPropertyWriter("geom", true)));
        ft.setProperties(Collections.singletonList(new HakunaPropertyDouble("value", "table", "value", true, false,
                HakunaPropertyWriters.getSimplePropertyWriter("value", HakunaPropertyType.DOUBLE))));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (FeatureCollectionWriter w = OutputFormatCSV.INSTANCE.getFeatureCollectionWriter()) {
            w.init(baos, srid);
            w.startFeatureCollection(ft, "test");
            w.startFeature(1L);
            w.writeGeometry("geom", new HakunaGeometryJTS(new GeometryFactory().createPoint(new CoordinateXY(x, y))));
            w.writeProperty("value", 1.25);
            w.endFeature();
            w.endFeatureCollection();
        }
        return baos.toString(StandardCharsets.UTF_8);
    }

}
