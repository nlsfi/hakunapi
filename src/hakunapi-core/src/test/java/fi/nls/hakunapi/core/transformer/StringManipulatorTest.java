package fi.nls.hakunapi.core.transformer;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import fi.nls.hakunapi.core.property.HakunaProperty;

public class StringManipulatorTest {

    @Test
    public void test() throws JsonParseException, JsonMappingException, IOException {
        StringManipulator t = new StringManipulator();

        t.parseParts("%d[0:3],'-',%d[3:6],'-',%d[6:10],'-',%d[10:14]");

        Object actual = t.toInner("498-401-876-11");
        assertEquals("49840108760011", actual);

        actual = t.toPublic("49840108760011");
        assertEquals("498-401-876-11", actual);

        actual = t.toInner("44-401-1-0");
        assertEquals("04440100010000", actual);

        actual = t.toPublic("04440100010000");
        assertEquals("44-401-1-0", actual);

        actual = t.toInner("12345-6789-321-1");
        assertEquals(HakunaProperty.UNKNOWN, actual);

        actual = t.toInner("---");
        assertEquals(HakunaProperty.UNKNOWN, actual);

        actual = t.toInner("foobar");
        assertEquals(HakunaProperty.UNKNOWN, actual);
    }

}
