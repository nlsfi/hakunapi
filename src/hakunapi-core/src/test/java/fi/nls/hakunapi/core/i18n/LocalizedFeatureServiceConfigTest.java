package fi.nls.hakunapi.core.i18n;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.Set;

import org.junit.Test;

import java.time.Instant;

import fi.nls.hakunapi.core.DatetimeProperty;
import fi.nls.hakunapi.core.FeatureProducer;
import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.PaginationStrategy;
import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.param.GetFeatureParam;
import fi.nls.hakunapi.core.projection.ProjectionTransformerFactory;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.OutputFormat;
import io.swagger.v3.oas.models.info.Info;

public class LocalizedFeatureServiceConfigTest {

    /**
     * The wrapper must delegate every inherited method explicitly and copy no
     * protected fields. A method left undelegated would read the wrapper's own
     * always-null field instead of the real service's value, which fails
     * silently rather than loudly, so it is checked mechanically here.
     */
    @Test
    public void testEveryInheritedConcreteMethodIsOverridden() {
        Set<String> overridden = new TreeSet<>();
        for (Method m : LocalizedFeatureServiceConfig.class.getDeclaredMethods()) {
            overridden.add(signature(m));
        }

        List<String> missing = new ArrayList<>();
        for (Method m : FeatureServiceConfig.class.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isStatic(mod) || Modifier.isAbstract(mod)) {
                continue;
            }
            if (!Modifier.isPublic(mod) && !Modifier.isProtected(mod)) {
                continue;
            }
            if (!overridden.contains(signature(m))) {
                missing.add(signature(m));
            }
        }

        assertTrue("LocalizedFeatureServiceConfig does not delegate: " + missing, missing.isEmpty());
    }

    private static String signature(Method m) {
        StringBuilder sb = new StringBuilder(m.getName()).append('(');
        Class<?>[] params = m.getParameterTypes();
        for (int i = 0; i < params.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(params[i].getName());
        }
        return sb.append(')').toString();
    }

    @Test
    public void testNotLocalizedWithoutLocalization() {
        TestServiceConfig service = service();
        assertSame(service, service.localized("fi"));
        assertSame(service, service.localized(null));
        assertEquals(Collections.emptyList(), service.getLanguages());
    }

    @Test
    public void testUnknownLanguageReturnsBaseService() {
        TestServiceConfig service = service();
        service.setLocalization(localization(Map.of("api.title", "Otsikko")));
        assertSame(service, service.localized("de"));
        assertSame(service, service.localized(null));
    }

    @Test
    public void testLocalizedTitleAndDescription() {
        TestServiceConfig service = service();
        service.setLocalization(localization(Map.of("api.title", "Otsikko")));

        FeatureServiceConfig fi = service.localized("fi");
        assertEquals("Otsikko", fi.getTitle());
        // sparse catalog: description falls back to the base config
        assertEquals("Base description", fi.getDescription());
        // the base service is untouched
        assertEquals("Base title", service.getTitle());
    }


    @Test
    public void testLocalizedDoesNotStackWrappers() {
        TestServiceConfig service = service();
        service.setLocalization(new Localization(Arrays.asList("en", "fi"),
                Map.of("fi", Map.of("api.title", "Otsikko")), service::baseValue));

        FeatureServiceConfig fi = service.localized("fi");
        FeatureServiceConfig en = fi.localized("en");
        assertSame(service.localized("en"), en);
        assertEquals("Base title", en.getTitle());
    }

    @Test
    public void testSetLocalizationInvalidatesCache() {
        TestServiceConfig service = service();
        service.setLocalization(localization(Map.of("api.title", "Otsikko")));
        FeatureServiceConfig before = service.localized("fi");
        assertEquals("Otsikko", before.getTitle());

        service.setLocalization(localization(Map.of("api.title", "Uusi")));
        FeatureServiceConfig after = service.localized("fi");
        assertEquals("Uusi", after.getTitle());
    }

    @Test
    public void testDelegatedMethodReadsRealServiceState() {
        TestServiceConfig service = service();
        service.setLimitDefault(123);
        service.setLocalization(localization(Map.of("api.title", "Otsikko")));

        // Would return 0 off the wrapper's own field if delegation were missing
        assertEquals(123, service.localized("fi").getLimitDefault());
    }

    @Test
    public void testCollectionsAreWrapped() {
        TestServiceConfig service = service();
        service.setLocalization(localization(Map.of("collections.test.title", "Testi")));

        FeatureServiceConfig fi = service.localized("fi");
        FeatureType ft = fi.getCollection("test");
        assertNotNull(ft);
        assertEquals("Testi", ft.getTitle());
        assertEquals("test", ft.getName());
        assertEquals(1, fi.getCollections().size());
        assertEquals("Testi", fi.getCollections().iterator().next().getTitle());

        assertNull(fi.getCollection("nope"));
        // base service unaffected
        assertEquals("Base collection title", service.getCollection("test").getTitle());
    }

    /**
     * Identity comparisons must survive a wrapper reaching a call site that is
     * about feature-type identity rather than display.
     */
    @Test
    public void testWrappedFeatureTypeEqualsUnwrapped() {
        TestServiceConfig service = service();
        service.setLocalization(localization(Map.of("collections.test.title", "Testi")));

        FeatureType raw = service.getCollection("test");
        FeatureType wrapped = service.localized("fi").getCollection("test");

        assertEquals(wrapped, raw);
        assertEquals(raw.hashCode(), wrapped.hashCode());
        assertEquals(wrapped, service.localized("fi").getCollection("test"));
    }

    private static TestServiceConfig service() {
        TestServiceConfig service = new TestServiceConfig();
        Info info = new Info();
        info.setTitle("Base title");
        info.setDescription("Base description");
        service.setInfo(info);
        return service;
    }

    private static Localization localization(Map<String, String> fiCatalog) {
        return new Localization(Arrays.asList("fi"), Map.of("fi", fiCatalog),
                key -> baseConfig(key));
    }

    private static String baseConfig(String key) {
        switch (key) {
        case "api.title":
            return "Base title";
        case "api.description":
            return "Base description";
        default:
            return null;
        }
    }

    private static class TestServiceConfig extends FeatureServiceConfig {

        private final FeatureType collection = new TestFeatureType();

        String baseValue(String key) {
            return baseConfig(key);
        }

        @Override
        public Collection<FeatureType> getCollections() {
            return List.of(collection);
        }

        @Override
        public FeatureType getCollection(String name) {
            return "test".equals(name) ? collection : null;
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

    /** Minimal FeatureType; only the display-related methods are exercised. */
    private static class TestFeatureType implements FeatureType {

        @Override
        public String getName() {
            return "test";
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
            return "Base collection title";
        }

        @Override
        public String getDescription() {
            return "Base collection description";
        }

        @Override
        public Map<String, Object> getMetadata() {
            return Collections.emptyMap();
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

    }

}
