package fi.nls.hakunapi.simple.webapp.jakarta.features;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import jakarta.servlet.ServletContextEvent;

import org.junit.Test;

import fi.nls.hakunapi.simple.webapp.jakarta.HakunaContextListener;
import fi.nls.hakunapi.simple.webapp.jakarta.HakunaTestServletContext;

/**
 * A catalog key outside the localizable whitelist must abort startup.
 *
 * Warn-and-ignore was deliberately rejected: a silently dropped translation
 * key is precisely the failure the catalog exists to make auditable, and the
 * whitelist also stops a catalog overriding unrelated configuration. This is
 * the end-to-end half of that; ReadLocalizationTest covers the parser directly.
 */
public class BadCatalogStartupTest {

    @Test
    public void testNonWhitelistedCatalogKeyAbortsStartup() {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("hakuna_i18n_bad/hakuna.properties").getFile());
        System.setProperty("hakuna.config.path", file.getParentFile().getAbsolutePath() + "/");

        HakunaTestServletContext sc = new HakunaTestServletContext();
        ServletContextEvent sce = new ServletContextEvent(sc);

        try {
            new HakunaContextListener().contextInitialized(sce);
            fail("Expected startup to fail on a non-whitelisted catalog key");
        } catch (RuntimeException e) {
            String message = unwrap(e);
            assertTrue(message, message.contains("getfeatures.limit.max"));
            assertTrue(message, message.contains("messages_fi.properties"));
            // the message should also say what is allowed
            assertTrue(message, message.contains("api.title"));
        } finally {
            System.clearProperty("hakuna.config.path");
        }
    }

    private static String unwrap(Throwable t) {
        StringBuilder sb = new StringBuilder();
        for (Throwable c = t; c != null; c = c.getCause()) {
            if (c.getMessage() != null) {
                sb.append(c.getMessage()).append('\n');
            }
        }
        return sb.toString();
    }

}
