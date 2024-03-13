package fi.nls.hakunapi.simple.webapp.jakarta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.Properties;

import org.junit.Test;
import org.mockito.Mockito;

import fi.nls.hakunapi.core.config.HakunaApplicationJson;

public class HakunaContextListenerTest {

    @Test
    public void testGetConfigPath() throws URISyntaxException {
        HakunaContextListener listener = Mockito.spy(HakunaContextListener.class);

        String barPath = Paths.get(getClass().getClassLoader().getResource("bar.properties").toURI()).toString();
        String fooPath = Paths.get(getClass().getClassLoader().getResource("foo.properties").toURI()).toString();
        String dirPath = barPath.substring(0, barPath.lastIndexOf(FileSystems.getDefault().getSeparator()));

        System.setProperty("hakuna.config.path", dirPath);
        assertEquals("With only directory expect /path/$key.properties", fooPath,
                listener.getConfigPath("foo").get().toString());

        System.setProperty("foo.hakuna.config.path", barPath);
        assertEquals("Expect $key.hakuna.config.path to get preferred", barPath,
                listener.getConfigPath("foo").get().toString());

        Mockito.when(listener.getEnv("HAKUNA_CONFIG_PATH")).thenReturn(fooPath);

        assertEquals("Expect more specific system property to get preferred", barPath,
                listener.getConfigPath("foo").get().toString());

        Mockito.when(listener.getEnv("FOO_HAKUNA_CONFIG_PATH")).thenReturn(fooPath);

        assertEquals("Expect environment variable to get preferred", fooPath,
                listener.getConfigPath("foo").get().toString());

        // Cleanup
        System.clearProperty("hakuna.config.path");
        System.clearProperty("foo.hakuna.config.path");
    }

    @Test
    public void testGetConfigPathHakunapi() throws URISyntaxException {
        HakunaContextListener listener = Mockito.spy(HakunaContextListener.class);

        String fooPath = Paths.get(getClass().getClassLoader().getResource("foo.properties").toURI()).toString();
        String barPath = Paths.get(getClass().getClassLoader().getResource("bar.properties").toURI()).toString();

        assertTrue(listener.getConfigPath("foo").isEmpty());
        assertTrue(listener.getConfigPath("bar").isEmpty());

        System.setProperty("hakunapi.config.path", barPath);
        assertEquals(barPath, listener.getConfigPath("foo").get().toString());
        assertEquals(barPath, listener.getConfigPath("bar").get().toString());

        Mockito.when(listener.getEnv("HAKUNAPI_CONFIG_PATH")).thenReturn(fooPath);
        assertEquals(fooPath, listener.getConfigPath("foo").get().toString());
        assertEquals(fooPath, listener.getConfigPath("bar").get().toString());

        System.setProperty("bar.hakunapi.config.path", barPath);
        assertEquals(fooPath, listener.getConfigPath("foo").get().toString());
        assertEquals(barPath, listener.getConfigPath("bar").get().toString());

        Mockito.when(listener.getEnv("BAR_HAKUNAPI_CONFIG_PATH")).thenReturn(fooPath);
        assertEquals(fooPath, listener.getConfigPath("foo").get().toString());
        assertEquals(fooPath, listener.getConfigPath("bar").get().toString());

        // Cleanup
        System.clearProperty("hakunapi.config.path");
        System.clearProperty("bar.hakunapi.config.path");
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
