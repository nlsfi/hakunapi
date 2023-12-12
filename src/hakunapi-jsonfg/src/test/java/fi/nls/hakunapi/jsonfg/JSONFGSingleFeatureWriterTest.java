package fi.nls.hakunapi.jsonfg;

import java.io.ByteArrayOutputStream;
import java.util.Collections;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import fi.nls.hakunapi.core.FeatureStream;
import fi.nls.hakunapi.core.FloatingPointFormatter;
import fi.nls.hakunapi.core.ValueProvider;
import fi.nls.hakunapi.core.geom.HakunaGeometryDimension;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.request.GetFeatureCollection;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import fi.nls.hakunapi.core.util.DefaultFloatingPointFormatter;

public class JSONFGSingleFeatureWriterTest extends JSONFGTestUtils {

	@Test
	public void testWriteJSONFGFeature() throws Exception {
		FloatingPointFormatter f = new DefaultFloatingPointFormatter(0, 5, 0, 5, 0, 13);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		GetFeatureRequest request = new GetFeatureRequest();
		GetFeatureCollection coll = new GetFeatureCollection(ft);
		request.addCollection(coll);

		try (FeatureStream fs = new TestFeaturesStream();
				JSONFGSingleFeatureWriter fw = new JSONFGSingleFeatureWriter()) {

			ValueProvider feat = fs.next();

			fw.init(baos, f, PLACE_SRID);
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

	}

}
