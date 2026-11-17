package fi.nls.hakunapi.core.schema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

public class JsonSchemaUtilTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void testTypeArray() throws Exception {
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

        assertTrue(JsonSchemaUtil.parse("{ \"type\": [\"null\"] }") instanceof ObjectSchema);
    }

    @Test
    public void testLoadFromFileFailsNamingTheFile() throws Exception {
        Path missing = tmp.getRoot().toPath().resolve("missing.json");
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> JsonSchemaUtil.loadFromFile(missing));
        assertTrue(e.getMessage().contains("missing.json"));

        Path broken = tmp.newFile("broken.json").toPath();
        Files.writeString(broken, "{ \"title\": ");
        e = assertThrows(IllegalArgumentException.class, () -> JsonSchemaUtil.loadFromFile(broken));
        assertTrue(e.getMessage().contains("broken.json"));

        Path notObject = tmp.newFile("array.json").toPath();
        Files.writeString(notObject, "[]");
        e = assertThrows(IllegalArgumentException.class, () -> JsonSchemaUtil.loadFromFile(notObject));
        assertTrue(e.getMessage().contains("array.json"));
    }

}
