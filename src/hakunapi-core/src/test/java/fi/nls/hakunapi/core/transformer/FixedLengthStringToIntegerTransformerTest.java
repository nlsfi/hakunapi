package fi.nls.hakunapi.core.transformer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FixedLengthStringToIntegerTransformerTest {

    @Test
    public void test() {
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
