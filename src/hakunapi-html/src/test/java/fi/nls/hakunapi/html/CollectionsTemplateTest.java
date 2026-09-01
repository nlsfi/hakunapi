package fi.nls.hakunapi.html;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.junit.Test;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import io.swagger.v3.oas.models.info.Info;

import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.OutputFormat;
import fi.nls.hakunapi.core.schemas.CollectionsContent;
import fi.nls.hakunapi.core.schemas.Link;
import fi.nls.hakunapi.html.model.HTMLContext;

public class CollectionsTemplateTest {

    @Test
    public void testAdditionalLinks() throws Exception {
        List<Link> links = List.of(
                new Link("https://example.org/collections", "self", "text/html", "This document"),
                new Link("https://example.org/collections", "alternate", "application/json", "This document"),
                new Link("https://example.org/metadata", "describedby", "application/xml", "Data set metadata"),
                new Link("https://example.org/license", "license", "text/html", "Licence"));

        String html = render(new CollectionsContent(links, Collections.emptyList()));

        assertTrue(html.contains("Additional Resources"));
        assertTrue(html.contains("https://example.org/metadata"));
        assertTrue(html.contains("Data set metadata"));
        assertTrue(html.contains("https://example.org/license"));
        assertTrue(html.contains("Licence"));
        // self and alternate are page navigation, not additional resources
        assertFalse(html.contains("This document"));
    }

    @Test
    public void testNoAdditionalLinks() throws Exception {
        List<Link> links = List.of(new Link("https://example.org/collections", "self", "text/html", "This document"));

        String html = render(new CollectionsContent(links, Collections.emptyList()));

        assertFalse(html.contains("Additional Resources"));
    }

    private String render(CollectionsContent content) throws Exception {
        Configuration cfg = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setLocale(Locale.US);
        cfg.setTemplateLoader(new ClassTemplateLoader(OutputFormatFactoryHTML.class, ""));

        Template template = cfg.getTemplate("collections.ftl");
        TestServiceConfig service = new TestServiceConfig();
        service.setInfo(new Info().title("Test service"));
        HTMLContext<CollectionsContent> ctx = new HTMLContext<>(service, "https://example.org", content);

        StringWriter out = new StringWriter();
        template.process(ctx, out);
        return out.toString();
    }

    private static class TestServiceConfig extends FeatureServiceConfig {

        @Override
        public Collection<FeatureType> getCollections() {
            return Collections.emptyList();
        }

        @Override
        public FeatureType getCollection(String name) {
            return null;
        }

        @Override
        public OutputFormat getOutputFormat(String f) {
            return null;
        }

        @Override
        public Collection<OutputFormat> getOutputFormats() {
            return Collections.emptyList();
        }

    }

}
