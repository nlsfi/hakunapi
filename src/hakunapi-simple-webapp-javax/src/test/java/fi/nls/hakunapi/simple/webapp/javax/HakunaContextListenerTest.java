package fi.nls.hakunapi.simple.webapp.javax;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.Properties;

import org.junit.Test;

import fi.nls.hakunapi.core.config.HakunaApplicationJson;
import fi.nls.hakunapi.simple.webapp.javax.HakunaContextListener;

public class HakunaContextListenerTest {

    @Test
    public void testGetConfigPath() throws URISyntaxException {
        String barPath = Paths.get(getClass().getClassLoader().getResource("bar.properties").toURI()).toString();
        String fooPath = Paths.get(getClass().getClassLoader().getResource("foo.properties").toURI()).toString();
        String dirPath = barPath.substring(0, barPath.lastIndexOf(FileSystems.getDefault().getSeparator()));

        System.setProperty("hakuna.config.path", dirPath);
        assertEquals("With only directory expect /path/$key.properties", fooPath,
                HakunaContextListener.getConfigPath("foo").get().toString());

        System.setProperty("foo.hakuna.config.path", barPath);
        assertEquals("Expect $key.hakuna.config.path to get preferred", barPath,
                HakunaContextListener.getConfigPath("foo").get().toString());
    }

    @Test
    public void testApplyAppJson() {
        String json = "{ \"contextpath\": { \"servers.dev.url\": \"http://localhost:8988/hakuna\" } }";
        System.setProperty(HakunaApplicationJson.HAKUNA_APPLICATION_JSON_PROP, json);

        HakunaContextListener listener = new HakunaContextListener();
        Properties config = new Properties();
        listener.applyAppJson("contextpath", config, StandardCharsets.UTF_8);

        assertEquals("http://localhost:8988/hakuna", config.getProperty("servers.dev.url"));

    }

    @Test
    public void testOverrideWithAppJson() {
        String json = "{ \"contextpath\": { \"servers.dev.url\": \"http://localhost:8988/hakuna\" } }";
        System.setProperty(HakunaApplicationJson.HAKUNA_APPLICATION_JSON_PROP, json);

        HakunaContextListener listener = new HakunaContextListener();
        Properties config = new Properties();
        config.setProperty("servers.dev.url", "http://localhost:1111/hakuna");
        listener.applyAppJson("contextpath", config, StandardCharsets.UTF_8);

        assertEquals("http://localhost:8988/hakuna", config.getProperty("servers.dev.url"));
    }

}
