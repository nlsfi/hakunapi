package fi.nls.hakunapi.core.config;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Properties;

import org.junit.Test;

import fi.nls.hakunapi.core.schemas.Link;

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

    @Test
    public void testReadCollectionsAdditionalLinks() {
        Properties cfg = new Properties();
        cfg.setProperty("api.links", "api");
        cfg.setProperty("api.links.api.href", "https://example.org/api");
        cfg.setProperty("api.links.api.rel", "describedby");
        cfg.setProperty("api.links.api.type", "application/xml");
        cfg.setProperty("api.links.api.title", "Service metadata");
        cfg.setProperty("api.collections.links", "metadata,license");
        cfg.setProperty("api.collections.links.metadata.href", "https://example.org/metadata");
        cfg.setProperty("api.collections.links.metadata.rel", "describedby");
        cfg.setProperty("api.collections.links.metadata.type", "application/xml");
        cfg.setProperty("api.collections.links.metadata.title", "Data set metadata");
        cfg.setProperty("api.collections.links.license.href", "https://example.org/license");
        cfg.setProperty("api.collections.links.license.rel", "license");
        cfg.setProperty("api.collections.links.license.type", "text/html");
        cfg.setProperty("api.collections.links.license.title", "Licence");

        HakunaConfigParser parser = new HakunaConfigParser(cfg);

        List<Link> links = parser.readCollectionsAdditionalLinks();
        assertEquals(2, links.size());
        assertEquals("https://example.org/metadata", links.get(0).getHref());
        assertEquals("describedby", links.get(0).getRel());
        assertEquals("application/xml", links.get(0).getType());
        assertEquals("Data set metadata", links.get(0).getTitle());
        assertEquals("https://example.org/license", links.get(1).getHref());
        assertEquals("license", links.get(1).getRel());
        assertEquals("text/html", links.get(1).getType());
        assertEquals("Licence", links.get(1).getTitle());

        // The two sources are independent
        List<Link> apiLinks = parser.readAdditionalLinks();
        assertEquals(1, apiLinks.size());
        assertEquals("https://example.org/api", apiLinks.get(0).getHref());
    }

    @Test
    public void testReadCollectionsAdditionalLinksUnconfigured() {
        HakunaConfigParser parser = new HakunaConfigParser(new Properties());
        assertTrue(parser.readCollectionsAdditionalLinks().isEmpty());
    }

    @Test
    public void testReadCollectionsAdditionalLinksMissingProperty() {
        Properties cfg = new Properties();
        cfg.setProperty("api.collections.links", "metadata");
        cfg.setProperty("api.collections.links.metadata.href", "https://example.org/metadata");
        cfg.setProperty("api.collections.links.metadata.rel", "describedby");
        cfg.setProperty("api.collections.links.metadata.type", "application/xml");

        HakunaConfigParser parser = new HakunaConfigParser(cfg);

        assertThrows(IllegalArgumentException.class, () -> parser.readCollectionsAdditionalLinks());
    }

}
