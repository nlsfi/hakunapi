package fi.nls.hakunapi.tiles.generator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;

import fi.nls.hakunapi.core.config.HakunaConfigParser;

public class TileSourcesTest {

    private static GeneratorControl control(String... collectionIds) {
        return new GeneratorControl("TEST",
                List.of(new ZoomRange("all", 0, 2, List.of(collectionIds), Map.of())));
    }

    private static HakunaConfigParser cfg(String... keyValues) {
        Properties p = new Properties();
        for (int i = 0; i < keyValues.length; i += 2) {
            p.setProperty(keyValues[i], keyValues[i + 1]);
        }
        return new HakunaConfigParser(p);
    }

    private static void assertRejected(HakunaConfigParser cfg, GeneratorControl control, String... messages) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> TileSources.read(cfg, control));
        for (String message : messages) {
            assertTrue(e.getMessage(), e.getMessage().contains(message));
        }
    }

    @Test
    public void collectionsAreGroupedIntoSourcesInConfigurationOrderOrEachIsItsOwnSource() {
        TileSources grouped = TileSources.read(cfg(
                "sources", "vesi, maasto",
                "vesi.collections", "jarvi, meri",
                "maasto.collections", "suo"),
                control("jarvi", "meri", "suo"));

        assertEquals(List.of("vesi", "maasto"), grouped.getSourceIds());
        assertEquals("vesi", grouped.getSource("jarvi"));
        assertEquals("vesi", grouped.getSource("meri"));
        assertEquals("maasto", grouped.getSource("suo"));

        TileSources ungrouped = TileSources.read(cfg(), control("jarvi", "suo"));

        assertEquals(List.of("jarvi", "suo"), ungrouped.getSourceIds());
        assertEquals("suo", ungrouped.getSource("suo"));
    }

    @Test
    public void inconsistentGroupingsAreRejected() {
        assertRejected(cfg("sources", "vesi, maasto", "vesi.collections", "jarvi",
                "maasto.collections", "jarvi"), control("jarvi"), "jarvi", "exactly one source");
        assertRejected(cfg("sources", "vesi", "vesi.collections", "jarvi"),
                control("jarvi", "suo"), "suo");
        assertRejected(cfg("sources", "vesi", "vesi.collections", "jarvi, meri"),
                control("jarvi"), "meri", "no zoom range renders");
        assertRejected(cfg("sources", "vesi"), control("jarvi"), "vesi.collections");
        assertRejected(cfg("sources", "vesi, vesi", "vesi.collections", "jarvi"),
                control("jarvi"), "declared twice");
    }

}
