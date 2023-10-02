package fi.nls.hakunapi.core.config;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

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
        cfg.setProperty("servers.dev.url", "https://${X-Forwarded-Host}/${X-Forwarded-Path}");

        HakunaConfigParser parser = new HakunaConfigParser(cfg);

        assertEquals("my_static_value", parser.get("test.static"));
        assertEquals("my_dynamic_value", parser.get("test.dynamic"));
        assertEquals("https://${X-Forwarded-Host}/${X-Forwarded-Path}", parser.get("servers.dev.url"));

        if (initialTestValue != null) {
            System.setProperty(property, initialTestValue);
        } else {
            System.clearProperty(property);
        }
    }

}
