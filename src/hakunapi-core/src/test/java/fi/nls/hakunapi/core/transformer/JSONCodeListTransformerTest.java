package fi.nls.hakunapi.core.transformer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import fi.nls.hakunapi.core.property.HakunaPropertyType;

public class JSONCodeListTransformerTest {

    @SuppressWarnings("unchecked")
    @Test
    public void test() throws Exception {
        JSONCodeListTransformer t = new JSONCodeListTransformer();
        Map<String, Object> map = null;
        File f = Path.of(getClass().getClassLoader().getResource("mapping.json").toURI()).toFile();
        map = t.readMap(f);
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
