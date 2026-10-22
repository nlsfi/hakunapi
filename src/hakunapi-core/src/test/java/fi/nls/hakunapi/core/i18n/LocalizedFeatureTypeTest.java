package fi.nls.hakunapi.core.i18n;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import fi.nls.hakunapi.core.DatetimeProperty;
import fi.nls.hakunapi.core.FeatureProducer;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.PaginationStrategy;
import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.param.GetFeatureParam;
import fi.nls.hakunapi.core.projection.ProjectionTransformerFactory;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;
import fi.nls.hakunapi.core.schemas.Link;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

public class LocalizedFeatureTypeTest {

    private static final String COLLECTION = "addresses";

    /**
     * Title and description resolve catalog, then schema file, then base config:
     * an explicit localization beats metadata derived from a schema, and a
     * schema for another language is not picked up at all.
     */
    @Test
    public void testTitleAndDescriptionResolutionOrder() {
        FeatureType catalogWins = wrap("fi",
                catalog("collections.addresses.title", "Osoitteet"),
                schema("fi", "Osoitteet schemasta", "Kuvaus schemasta"));
        assertEquals("Osoitteet", catalogWins.getTitle());
        // no catalog entry for the description, so the schema file wins
        assertEquals("Kuvaus schemasta", catalogWins.getDescription());

        FeatureType schemaWins = wrap("fi", catalog(), schema("fi", "Osoitteet schemasta", null));
        assertEquals("Osoitteet schemasta", schemaWins.getTitle());
        // neither catalog nor schema has a description, so the base value stands
        assertEquals("Base description", schemaWins.getDescription());

        FeatureType noSource = wrap("fi", catalog(), Collections.emptyMap());
        assertEquals("Base title", noSource.getTitle());
        assertEquals("Base description", noSource.getDescription());

        FeatureType otherLang = wrap("fi", catalog(), schema("sv", "Adresser", "Beskrivning"));
        assertEquals("Base title", otherLang.getTitle());
        assertEquals("Base description", otherLang.getDescription());
    }

    /**
     * A schema is matched the same way a request's language is: exactly, then by
     * primary subtag, so a "sv" schema serves a locale declared as "sv-fi".
     */
    @Test
    public void testSchemaMatchesByPrimarySubtag() {
        FeatureType ft = wrap("sv-fi", catalog(), schema("sv", "Adresser", "Beskrivning"));

        assertEquals("Adresser", ft.getTitle());
        assertEquals("Beskrivning", ft.getDescription());
    }

    /**
     * Link titles are not localized: a translated title on a link whose href
     * points elsewhere would promise a language the target may not have. A
     * config declares one link per language instead, with its own hreflang.
     */
    @Test
    public void testLinksArePassedThroughUntouched() {
        TestFeatureType base = new TestFeatureType();
        base.links = List.of(new Link("http://example.org/legend", "describedby",
                "application/xml", "Legend", "en"));

        FeatureType ft = wrap("fi", base,
                catalog("collections.addresses.links.legend.title", "Karttaselite"),
                Collections.emptyMap());

        List<Link> links = ft.getAdditionalLinks();
        assertSame(base.links, links);
        assertEquals("Legend", links.get(0).getTitle());
    }

    /* Delegation and identity */

    /**
     * Only title, description and link titles are localized; getLangToSchema in
     * particular passes the map through rather than resolving it.
     */
    @Test
    public void testEverythingElseDelegates() {
        TestFeatureType base = new TestFeatureType();
        FeatureType ft = wrap("fi", base, catalog("collections.addresses.title", "Osoitteet"),
                Collections.emptyMap());

        assertEquals(COLLECTION, ft.getName());
        assertEquals(base.getMetadata(), ft.getMetadata());
        assertEquals(base.getProperties(), ft.getProperties());
        assertEquals(base.getQueryableProperties(), ft.getQueryableProperties());
        assertNull(ft.getGeom());
        assertNull(ft.getNS());
        assertTrue(ft.getAdditionalLinks().isEmpty());

        Map<String, Schema<?>> schemas = schema("fi", "T", "D");
        assertEquals(schemas.keySet(), wrap("fi", catalog(), schemas).getLangToSchema().keySet());
    }

    /**
     * Nothing in the codebase compares feature types by identity, but a wrapper
     * reaching such a call site must not break it.
     */
    @Test
    public void testUnwrapEqualsAndHashCodeFollowTheWrappedInstance() {
        TestFeatureType base = new TestFeatureType();
        Localization l10n = localization(catalog(), key -> null);
        LocalizedFeatureType a = new LocalizedFeatureType(base, "fi", l10n);
        LocalizedFeatureType b = new LocalizedFeatureType(base, "sv", l10n);

        assertSame(base, a.unwrap());
        assertEquals("fi", a.getLang());

        assertEquals(base.hashCode(), a.hashCode());
        assertEquals(a, base);
        assertEquals(a, b);
        assertEquals(a, a);
        assertEquals(base.toString(), a.toString());
    }

    /* Helpers */

    private static FeatureType wrap(String lang, Map<String, String> catalog, Map<String, Schema<?>> schemas) {
        TestFeatureType base = new TestFeatureType();
        base.langToSchema = schemas;
        return wrap(lang, base, catalog, schemas);
    }

    private static FeatureType wrap(String lang, TestFeatureType base, Map<String, String> catalog,
            Map<String, Schema<?>> schemas) {
        base.langToSchema = schemas;
        return new LocalizedFeatureType(base, lang, localization(catalog, LocalizedFeatureTypeTest::baseConfig));
    }

    private static Localization localization(Map<String, String> catalog,
            java.util.function.Function<String, String> base) {
        return new Localization(Arrays.asList("fi"), Map.of("fi", catalog), base);
    }

    private static String baseConfig(String key) {
        return null;
    }

    private static Map<String, String> catalog(String... kvp) {
        Map<String, String> m = new LinkedHashMap<>();
        for (int i = 0; i < kvp.length; i += 2) {
            m.put(kvp[i], kvp[i + 1]);
        }
        return m;
    }

    private static Map<String, Schema<?>> schema(String lang, String title, String description) {
        ObjectSchema s = new ObjectSchema();
        s.setTitle(title);
        s.setDescription(description);
        return Map.of(lang, s);
    }

    /** Minimal FeatureType; only the display-related methods matter here. */
    private static class TestFeatureType implements FeatureType {

        private List<Link> links = Collections.emptyList();
        private Map<String, Schema<?>> langToSchema = Collections.emptyMap();

        @Override
        public String getName() {
            return COLLECTION;
        }

        @Override
        public String getNS() {
            return null;
        }

        @Override
        public String getSchemaLocation() {
            return null;
        }

        @Override
        public String getTitle() {
            return "Base title";
        }

        @Override
        public String getDescription() {
            return "Base description";
        }

        @Override
        public Map<String, Object> getMetadata() {
            return Map.of("k", "v");
        }

        @Override
        public HakunaProperty getId() {
            return null;
        }

        @Override
        public HakunaPropertyGeometry getGeom() {
            return null;
        }

        @Override
        public List<HakunaProperty> getProperties() {
            return Collections.emptyList();
        }

        @Override
        public List<HakunaProperty> getQueryableProperties() {
            return Collections.emptyList();
        }

        @Override
        public List<DatetimeProperty> getDatetimeProperties() {
            return Collections.emptyList();
        }

        @Override
        public double[] getSpatialExtent() {
            return null;
        }

        @Override
        public Instant[] getTemporalExtent() {
            return null;
        }

        @Override
        public List<GetFeatureParam> getParameters() {
            return Collections.emptyList();
        }

        @Override
        public List<Filter> getStaticFilters() {
            return Collections.emptyList();
        }

        @Override
        public ProjectionTransformerFactory getProjectionTransformerFactory() {
            return null;
        }

        @Override
        public PaginationStrategy getPaginationStrategy() {
            return null;
        }

        @Override
        public FeatureProducer getFeatureProducer() {
            return null;
        }

        @Override
        public List<Link> getAdditionalLinks() {
            return links;
        }

        @Override
        public Map<String, Schema<?>> getLangToSchema() {
            return langToSchema;
        }

    }

}
