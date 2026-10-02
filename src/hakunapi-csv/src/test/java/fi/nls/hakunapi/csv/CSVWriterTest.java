package fi.nls.hakunapi.csv;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import fi.nls.hakunapi.core.geom.HakunaGeometryJTS;
import fi.nls.hakunapi.core.util.DefaultFloatingPointFormatter;

public class CSVWriterTest {

    @Test
    public void testWKTSeparatesCoordinatesInEveryDimension() throws Exception {
        GeometryFactory gf = new GeometryFactory();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (CSVWriter csv = new CSVWriter(baos, DefaultFloatingPointFormatter.DEFAULT_METERS)) {
            csv.init(new String[] { "geom" });
            csv.writeGeometry(new HakunaGeometryJTS(gf.createLineString(new Coordinate[] {
                    new Coordinate(1.5, 2, 3),
                    new Coordinate(4, 5.25, 6)
            })));
        }
        assertEquals("geom\n\"LINESTRING Z(1.5 2 3,4 5.25 6)\"\n", baos.toString(StandardCharsets.UTF_8));
    }

}
