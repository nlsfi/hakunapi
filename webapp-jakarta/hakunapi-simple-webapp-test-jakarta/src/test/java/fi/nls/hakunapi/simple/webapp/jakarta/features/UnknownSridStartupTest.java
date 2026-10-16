package fi.nls.hakunapi.simple.webapp.jakarta.features;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import jakarta.servlet.ServletContextEvent;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import fi.nls.hakunapi.simple.webapp.jakarta.HakunaContextListener;
import fi.nls.hakunapi.simple.webapp.jakarta.HakunaTestServletContext;

public class UnknownSridStartupTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void testCollectionSridMissingFromGlobalSridFailsStartup() throws Exception {
        String config = String.join("\n",
                "$config.version=2",
                "servers=dev",
                "servers.dev.url=https://localhost/hakuna",
                "db.classes=fi.nls.hakunapi.source.HakunaTestSource",
                "db=db",
                "collections=c",
                "collections.c.type=test",
                "collections.c.db=db",
                "collections.c.table=c",
                "collections.c.srid=3067",
                "collections.c.geometry.mapping=geom",
                "collections.c.geometry.type=POINT",
                "collections.c.testdata.1=1;POINT(1 1)");
        File dir = tmp.newFolder();
        Files.writeString(new File(dir, "hakuna.properties").toPath(), config, StandardCharsets.UTF_8);
        System.setProperty("hakuna.config.path", dir.getAbsolutePath() + "/");

        try {
            new HakunaContextListener().contextInitialized(new ServletContextEvent(new HakunaTestServletContext()));
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("missing from the global 'srid'"));
            return;
        }
        throw new AssertionError("Expected IllegalArgumentException");
    }

}
