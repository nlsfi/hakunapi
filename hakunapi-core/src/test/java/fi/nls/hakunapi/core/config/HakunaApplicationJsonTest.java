package fi.nls.hakunapi.core.config;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.junit.Test;

public class HakunaApplicationJsonTest {

    @Test
    public void testLoadContextJson() {

        String json = "{ \"contextpath\": { \"servers.dev.url\": \"http://localhost:8988/hakuna\" } }";

        HakunaApplicationJson appJson = HakunaApplicationJson.readFromString(json);

        assertEquals("http://localhost:8988/hakuna",
                appJson.getContextProperties("contextpath").getProperty("servers.dev.url"));
    }

    @Test
    public void testIgnoreEmptyContextJson() {

        String json = null;

        HakunaApplicationJson.readFromString(json);
    }

    @Test
    public void testApplyContextJson() {

        String json = "{ \"contextpath\": { \"servers.dev.url\": \"http://localhost:8988/hakuna\" } }";

        Properties config = new Properties();
        HakunaApplicationJson appJson = HakunaApplicationJson.readFromString(json);
        appJson.setContextProperties("contextpath", config);
        assertEquals("http://localhost:8988/hakuna", config.getProperty("servers.dev.url"));
    }

    @Test
    public void testIgnoreOtherContextJson() {

        String json = "{ \"othercontextpath\": { \"servers.dev.url\": \"http://localhost:1111/hakuna\" } }";

        Properties config = new Properties();
        config.setProperty("servers.dev.url", "http://localhost:8988/hakuna");
        HakunaApplicationJson appJson = HakunaApplicationJson.readFromString(json);
        appJson.setContextProperties("contextpath", config);
        assertEquals("http://localhost:8988/hakuna", config.getProperty("servers.dev.url"));
    }

    @Test
    public void testIgnoreInvalidContextJson() {

        String json = "{ xx\" } }";

        Properties config = new Properties();
        config.setProperty("servers.dev.url", "http://localhost:8988/hakuna");
        HakunaApplicationJson appJson = HakunaApplicationJson.readFromString(json);
        appJson.setContextProperties("contextpath", config);
        assertEquals("http://localhost:8988/hakuna", config.getProperty("servers.dev.url"));
    }

    @Test
    public void testOverrideWithContextJson() {
        String json = "{ \"contextpath\": { \"servers.dev.url\": \"http://localhost:8988/hakuna\" } }";

        Properties config = new Properties();
        config.setProperty("servers.dev.url", "http://localhost:1111/hakuna");
        HakunaApplicationJson appJson = HakunaApplicationJson.readFromString(json);
        appJson.setContextProperties("contextpath", config);
        assertEquals("http://localhost:8988/hakuna", config.getProperty("servers.dev.url"));

    }

}
