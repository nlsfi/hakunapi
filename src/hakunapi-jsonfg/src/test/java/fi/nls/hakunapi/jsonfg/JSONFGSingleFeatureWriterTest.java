package fi.nls.hakunapi.jsonfg;

import java.io.ByteArrayOutputStream;
import java.util.Collections;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import fi.nls.hakunapi.core.FeatureStream;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.ValueProvider;
import fi.nls.hakunapi.core.geom.HakunaGeometryDimension;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.request.GetFeatureCollection;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import fi.nls.hakunapi.core.util.CrsUtil;

public class JSONFGSingleFeatureWriterTest extends JSONFGTestUtils {

    @Test
    public void testWriteJSONFGFeatureWithDate() throws Exception {
        String ftName = "ExampleWithDate";
        JSONFGTestUtils data = new JSONFGTestUtils();

        FeatureType ft = data.getFeatureTypeWithDate(ftName);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        GetFeatureRequest request = new GetFeatureRequest();
        GetFeatureCollection coll = new GetFeatureCollection(ft);
        request.addCollection(coll);

        try (FeatureStream fs = data.getFeaturesWithDate();
                JSONFGSingleFeatureWriter fw = new JSONFGSingleFeatureWriter()) {

            ValueProvider feat = fs.next();

            int srid =data.PLACE_SRID;
            int maxDecimalCoordinates = CrsUtil.getMaxDecimalCoordinates(srid);

            fw.init(baos, maxDecimalCoordinates, srid, data.PLACE_SRID_IS_LATLON);
            fw.initGeometryWriter(HakunaGeometryDimension.XY);

            // Note: contains implicit call to startFeature !
            int i = 0;
            for (HakunaProperty prop : coll.getProperties()) {
                prop.write(feat, i++, fw);
            }

            fw.endFeature();
            fw.end(true, Collections.emptyList(), 1);
        }

        System.out.println(baos.toString("UTF-8"));
        System.out.flush();

        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.CLOSE_CLOSEABLE);

        JsonNode json = mapper.readTree(baos.toByteArray());

        mapper.writeValue(System.out, json);
        
        
        data.validateFeatureWithDate(json);

    }
    
    @Test
    public void testWriteJSONFGFeatureWithTimestamp() throws Exception {
        String ftName = "ExampleWithTimestamp";
        JSONFGTestUtils data = new JSONFGTestUtils();

        FeatureType ft = data.getFeatureTypeWithTimestamp(ftName);

         ByteArrayOutputStream baos = new ByteArrayOutputStream();

        GetFeatureRequest request = new GetFeatureRequest();
        GetFeatureCollection coll = new GetFeatureCollection(ft);
        request.addCollection(coll);

        try (FeatureStream fs = data.getFeaturesWithTimestamp();
                JSONFGSingleFeatureWriter fw = new JSONFGSingleFeatureWriter()) {

            ValueProvider feat = fs.next();

            int srid =data.PLACE_SRID;
            int maxDecimalCoordinates = CrsUtil.getMaxDecimalCoordinates(srid);

            fw.init(baos, maxDecimalCoordinates, srid, data.PLACE_SRID_IS_LATLON);
            fw.initGeometryWriter(HakunaGeometryDimension.XY);

            // Note: contains implicit call to startFeature !
            int i = 0;
            for (HakunaProperty prop : coll.getProperties()) {
                prop.write(feat, i++, fw);
            }

            fw.endFeature();
            fw.end(true, Collections.emptyList(), 1);
        }

        System.out.println(baos.toString("UTF-8"));
        System.out.flush();

        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.CLOSE_CLOSEABLE);

        JsonNode json = mapper.readTree(baos.toByteArray());

        mapper.writeValue(System.out, json);

        data.validateFeatureWithTimestamp(json);

    }

}
