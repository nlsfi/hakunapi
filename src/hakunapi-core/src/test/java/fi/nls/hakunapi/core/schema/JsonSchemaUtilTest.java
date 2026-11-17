package fi.nls.hakunapi.core.schema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

public class JsonSchemaUtilTest {

    @Test
    public void typeArrayWithNullUsesTheNonNullType() throws Exception {
        String json = """
                {
                  "properties": {
                    "c": {
                      "type": "object",
                      "properties": {
                        "name": { "title": "Name", "type": ["string", "null"] },
                        "count": { "type": ["null", "integer"] },
                        "geom": { "type": "object", "x-geometry-type": "POINTZ" }
                      }
                    }
                  }
                }
                """;
        Schema<?> c = JsonSchemaUtil.getCollectionSchema(JsonSchemaUtil.parse(json), "c");
        Schema<?> name = JsonSchemaUtil.getPropertySchema(c, "name");
        assertTrue(name instanceof StringSchema);
        assertEquals("Name", name.getTitle());
        assertTrue(JsonSchemaUtil.getPropertySchema(c, "count") instanceof IntegerSchema);
        assertEquals("POINTZ", JsonSchemaUtil.getPropertySchema(c, "geom").getExtensions().get("x-geometry-type"));
    }

    @Test
    public void typeArrayOfOnlyNullFallsBackToObject() throws Exception {
        Schema<?> s = JsonSchemaUtil.parse("{ \"type\": [\"null\"] }");
        assertTrue(s instanceof ObjectSchema);
    }

}
