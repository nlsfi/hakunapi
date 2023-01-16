package fi.nls.hakunapi.core.config;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

import fi.nls.hakunapi.core.config.HakunaConfigParser;

public class HakunaConfigParserTest {

    @Test
    public void testGetSRIDs() {
        String srid = " 3067, 4326 ,  1337, 3067,3067,4326   ";
        int[] actuals = HakunaConfigParser.getSRIDs(srid);
        int[] expecteds = { 3067, 4326, 1337 };
        assertArrayEquals(expecteds, actuals);
    }

}
