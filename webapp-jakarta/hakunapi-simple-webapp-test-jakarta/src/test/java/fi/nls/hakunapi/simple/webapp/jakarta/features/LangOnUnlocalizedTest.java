package fi.nls.hakunapi.simple.webapp.jakarta.features;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import jakarta.servlet.ServletContextEvent;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;

import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import fi.nls.hakunapi.simple.webapp.jakarta.HakunaContextListener;
import fi.nls.hakunapi.simple.webapp.jakarta.HakunaTestServletContext;
import fi.nls.hakunapi.simple.webapp.jakarta.SimpleFeaturesApplication;

/**
 * The feature-data endpoints accept lang only because the metadata resources
 * propagate it into the items links they build. A service that declares no
 * languages builds no such links, so there lang is still an unknown parameter
 * and must be rejected: accepting it unconditionally would silently swallow a
 * typo on every unlocalized deployment.
 *
 * hakuna_schema declares schema= but no locale=, so it is unlocalized.
 */
public class LangOnUnlocalizedTest extends JerseyTest {

    @Override
    protected Application configure() {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("hakuna_schema/hakuna.properties").getFile());

        System.setProperty("hakuna.config.path", file.getParentFile().getAbsolutePath() + "/");

        final HakunaTestServletContext sc = new HakunaTestServletContext();
        final ServletContextEvent sce = new ServletContextEvent(sc);
        new HakunaContextListener().contextInitialized(sce);

        return new SimpleFeaturesApplication(sc);
    }

    @Test
    public void testLangIsRejectedOnAnUnlocalizedService() {
        Response r = target("/collections/fence/items").queryParam("lang", "fi").request().get();

        assertEquals(400, r.getStatus());
        assertTrue(r.readEntity(String.class).contains("lang"));
    }

    @Test
    public void testItemsStillWorkWithoutLang() {
        Response r = target("/collections/fence/items").request().get();

        assertEquals(200, r.getStatus());
    }
}
