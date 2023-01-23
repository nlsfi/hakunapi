package fi.nls.hakunapi.core.transformer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import fi.nls.hakunapi.core.property.HakunaPropertyType;

public class JSONCodeListTransformerTest {

    @SuppressWarnings("unchecked")
    @Test
    public void test() throws JsonParseException, JsonMappingException, IOException {
        JSONCodeListTransformer t = new JSONCodeListTransformer();
        URL url = getClass().getClassLoader().getResource("mapping.json");
        Map<String, Object> map = t.readMap(url);
        t.initFromMap(HakunaPropertyType.STRING, map);

        assertEquals("foo", t.toPublic("A"));
        assertEquals("bar", t.toPublic("B"));
        assertEquals("baz", t.toPublic("C"));
        assertNull(t.toPublic("D"));
        assertNull(t.toPublic("E"));
        assertEquals("qux", t.toPublic("F"));
        assertEquals("qux", t.toPublic(null));

        assertEquals("A", t.toInner("foo"));
        assertEquals("B", t.toInner("bar"));
        assertEquals("C", t.toInner("baz"));

        List<Object> innerValuesFromNull = (List<Object>) t.toInner(null);
        assertEquals(2, innerValuesFromNull.size());
        assertTrue(innerValuesFromNull.contains("D"));
        assertTrue(innerValuesFromNull.contains("E"));

        List<Object> innerValuesFromQux = (List<Object>) t.toInner("qux");
        assertEquals(2, innerValuesFromQux.size());
        assertTrue(innerValuesFromQux.contains("F"));
        assertTrue(innerValuesFromQux.contains(null));
    }

}
