package fi.nls.hakunapi.core.config;

import static org.junit.Assert.assertArrayEquals;

import java.util.Properties;

import org.junit.Test;

public class HakunaConfigParserTest {

    @Test
    public void testGetSRIDs() {
        String srid = " 3067, 4326 ,  1337, 3067,3067,4326   ";
        int[] actuals = HakunaConfigParser.getSRIDs(srid);
        int[] expecteds = { 3067, 4326, 1337 };
        assertArrayEquals(expecteds, actuals);
    }

    @Test
    public void testExternalizedProperties() {
        String property = "hakunapi.test";
        String initialTestValue = System.getProperty(property);

        System.setProperty(property, "dynamic");

        Properties cfg = new Properties();
        cfg.setProperty("test.static", "my_static_value");
        cfg.setProperty("test.dynamic", "my_${hakunapi.test}_value");

        HakunaConfigParser parser = new HakunaConfigParser(cfg);
        parser.get("my_static_value", "test.static");
        parser.get("my_dynamic_value", "test.dynamic");

        if (initialTestValue != null) {
            System.setProperty(property, initialTestValue);
        } else {
            System.clearProperty(property);
        }
    }

}
