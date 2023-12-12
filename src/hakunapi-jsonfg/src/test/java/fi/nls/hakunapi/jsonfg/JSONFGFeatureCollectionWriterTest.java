package fi.nls.hakunapi.jsonfg;

import java.io.ByteArrayOutputStream;
import java.util.Collections;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import fi.nls.hakunapi.core.FeatureCollectionWriter;
import fi.nls.hakunapi.core.FeatureStream;
import fi.nls.hakunapi.core.FloatingPointFormatter;
import fi.nls.hakunapi.core.geom.HakunaGeometryDimension;
import fi.nls.hakunapi.core.request.GetFeatureCollection;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import fi.nls.hakunapi.core.util.DefaultFloatingPointFormatter;

public class JSONFGFeatureCollectionWriterTest extends JSONFGTestUtils {

    @Test
    public void testWriteJSONFGFeatureCollection() throws Exception {
        FloatingPointFormatter f = new DefaultFloatingPointFormatter(0, 5, 0, 5, 0, 13);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        GetFeatureRequest request = new GetFeatureRequest();
        GetFeatureCollection coll = new GetFeatureCollection(ft);
        request.addCollection(coll);

        try (FeatureStream fs = new TestFeaturesStream();
                FeatureCollectionWriter fw = new JSONFGFeatureCollectionWriter()) {

            fw.init(baos, f, PLACE_SRID);
            fw.initGeometryWriter(HakunaGeometryDimension.XY);

            fw.startFeatureCollection(ft, null);

            int numberReturned = writeFeatureCollection(fw, ft, coll.getProperties(), fs, 0, -1).numberReturned;

            fw.endFeatureCollection();
            fw.end(false, Collections.emptyList(), numberReturned);
        }

        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.CLOSE_CLOSEABLE);

        JsonNode json = mapper.readTree(baos.toByteArray());

        mapper.writeValue(System.out, json);

    }

}
