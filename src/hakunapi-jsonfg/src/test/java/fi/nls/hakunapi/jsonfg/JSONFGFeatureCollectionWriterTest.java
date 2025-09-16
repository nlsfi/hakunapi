package fi.nls.hakunapi.jsonfg;

import java.io.ByteArrayOutputStream;
import java.util.Collections;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import fi.nls.hakunapi.core.FeatureCollectionWriter;
import fi.nls.hakunapi.core.FeatureStream;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.OutputFormat;
import fi.nls.hakunapi.core.request.GetFeatureCollection;
import fi.nls.hakunapi.core.request.GetFeatureRequest;

public class JSONFGFeatureCollectionWriterTest {
    
    private OutputFormat format = new JSONFGOutputFormatFactory().create(Collections.emptyMap());

    @Test
    public void testWriteJSONFGWithDateProperty() throws Exception {
        String ftName = "ExampleWithDate";
        JSONFGTestUtils data = new JSONFGTestUtils();

        FeatureType ft = data.getFeatureTypeWithDate(ftName);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        GetFeatureRequest request = new GetFeatureRequest();
        request.setLimit(-1);
        GetFeatureCollection coll = new GetFeatureCollection(ft);
        request.addCollection(coll);

        try (FeatureStream fs = data.getFeaturesWithDate();
                FeatureCollectionWriter fw = format.getFeatureCollectionWriter()) {
            fw.init(baos, data.PLACE_SRID);

            fw.startFeatureCollection(ft, null);

            int numberReturned = JSONFGTestUtils.writeFeatureCollection(fw, ft, coll.getProperties(), fs, request, coll).numberReturned;

            fw.endFeatureCollection();
            fw.end(false, Collections.emptyList(), numberReturned);
        }

        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.CLOSE_CLOSEABLE);

        // System.out.println(new String(baos.toByteArray()));
        
        JsonNode json = mapper.readTree(baos.toByteArray());

        // mapper.writeValue(System.out, json);

        data.validateFeatureCollection(json);

    }

    @Test
    public void testWriteJSONFGWithTimestampProperty() throws Exception {
        String ftName = "ExampleWithTimestamp";
        JSONFGTestUtils data = new JSONFGTestUtils();

        FeatureType ft = data.getFeatureTypeWithTimestamp(ftName);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        GetFeatureRequest request = new GetFeatureRequest();
        request.setLimit(-1);
        GetFeatureCollection coll = new GetFeatureCollection(ft);
        request.addCollection(coll);

        try (FeatureStream fs = data.getFeaturesWithTimestamp();
                FeatureCollectionWriter fw = format.getFeatureCollectionWriter()) {
            fw.init(baos, data.PLACE_SRID);

            fw.startFeatureCollection(ft, null);

            int numberReturned = JSONFGTestUtils.writeFeatureCollection(fw, ft, coll.getProperties(), fs, request, coll).numberReturned;

            fw.endFeatureCollection();
            fw.end(false, Collections.emptyList(), numberReturned);
        }

        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.CLOSE_CLOSEABLE);

        JsonNode json = mapper.readTree(baos.toByteArray());

        // mapper.writeValue(System.out, json);

        data.validateFeatureCollection(json);

    }
}
