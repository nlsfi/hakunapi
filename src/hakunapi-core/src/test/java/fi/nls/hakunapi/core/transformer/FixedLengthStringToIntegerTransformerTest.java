package fi.nls.hakunapi.core.transformer;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class FixedLengthStringToIntegerTransformerTest {

    @Test
    public void test() throws JsonParseException, JsonMappingException, IOException {
        String expected = "123456789012345";
        String actual = FixedLengthStringToIntegerTransformer.longToString(Long.parseLong(expected), expected.length());
        assertEquals(expected, actual);

        expected = "-54321";
        actual = FixedLengthStringToIntegerTransformer.longToString(Long.parseLong(expected), expected.length());
        assertEquals(expected, actual);

        expected = "0";
        actual = FixedLengthStringToIntegerTransformer.longToString(Long.parseLong(expected), expected.length());
        assertEquals(expected, actual);

        expected = "100001";
        actual = FixedLengthStringToIntegerTransformer.longToString(Long.parseLong(expected), expected.length());
        assertEquals(expected, actual);

        expected ="09100602260007";
        actual = FixedLengthStringToIntegerTransformer.longToString(Long.parseLong(expected), expected.length());
        assertEquals(expected, actual);
    }

}
