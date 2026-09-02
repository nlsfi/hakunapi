package fi.nls.hakunapi.core.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import fi.nls.hakunapi.core.i18n.Localization;

public class ReadLocalizationTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void testNoLocaleConfigured() throws IOException {
        Path config = writeConfig("api.title=Base\n");
        Localization l10n = read(load(config), config);

        assertTrue(l10n.isEmpty());
        assertEquals(Collections.emptyList(), l10n.getLanguages());
        assertNull(l10n.getDefaultLanguage());
    }

    @Test
    public void testDeclaredOrderAndDefaultLanguage() throws IOException {
        Path config = writeConfig("locale=en,fi,sv\napi.title=Base\n");
        Localization l10n = read(load(config), config);

        assertEquals(Arrays.asList("en", "fi", "sv"), l10n.getLanguages());
        assertEquals("en", l10n.getDefaultLanguage());
    }


    @Test
    public void testLangOverridesNameAndIsNormalized() throws IOException {
        Path config = writeConfig("locale=finnish\nlocale.finnish.lang=fi-FI\n");
        Localization l10n = read(load(config), config);

        assertEquals(Arrays.asList("fi-fi"), l10n.getLanguages());
    }

    @Test
    public void testSparseCatalogFallsBackPerKey() throws IOException {
        write("messages_fi.properties", "api.title=Otsikko\n");
        Path config = writeConfig("locale=fi\nlocale.fi.path=messages_fi.properties\n"
                + "api.title=Base title\napi.description=Base description\n");
        Localization l10n = read(load(config), config);

        assertEquals("Otsikko", l10n.get("fi", "api.title"));
        assertEquals("Base description", l10n.get("fi", "api.description"));
        assertNull(l10n.get("fi", "api.nonexistent"));
        assertTrue(l10n.hasExplicit("fi", "api.title"));
        assertTrue(!l10n.hasExplicit("fi", "api.description"));
    }

    /**
     * Catalogs must be read through an explicit UTF-8 Reader;
     * Properties.load(InputStream) is Latin-1 and would mangle these.
     */
    @Test
    public void testUtf8RoundTrip() throws IOException {
        write("messages_fi.properties",
                "api.description=Ääkkösiä ja åäö, Åland\n"
                        + "collections.osoitteet.title=Osoitepisteet — koko Suomi\n"
                        + "collections.osoitteet.description=Ruotsiksi: Adresspunkter i hela Finland\n");
        Path config = writeConfig("locale=fi\nlocale.fi.path=messages_fi.properties\n");
        Localization l10n = read(load(config), config);

        assertEquals("Ääkkösiä ja åäö, Åland", l10n.get("fi", "api.description"));
        assertEquals("Osoitepisteet — koko Suomi", l10n.get("fi", "collections.osoitteet.title"));
        assertEquals("Ruotsiksi: Adresspunkter i hela Finland",
                l10n.get("fi", "collections.osoitteet.description"));
    }


    /**
     * A silently dropped translation key is precisely the failure the catalog
     * exists to make auditable, so a non-whitelisted key must fail at startup
     * and name the offending key. It also stops a catalog from overriding
     * unrelated config.
     */
    @Test
    public void testNonWhitelistedKeyFailsFastNamingTheKey() throws IOException {
        write("messages_fi.properties", "api.title=Otsikko\ndb.url=jdbc:evil\n");
        Path config = writeConfig("locale=fi\nlocale.fi.path=messages_fi.properties\n");

        try {
            read(load(config), config);
            fail("Expected a non-whitelisted catalog key to fail");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("db.url"));
            assertTrue(e.getMessage(), e.getMessage().contains("messages_fi.properties"));
        }
    }

    @Test
    public void testNonLinguisticApiKeysAreRejected() throws IOException {
        for (String key : Arrays.asList("api.version", "api.contact.name", "api.license.url",
                "api.license.name", "getfeatures.limit.max")) {
            write("messages_fi.properties", key + "=whatever\n");
            Path config = writeConfig("locale=fi\nlocale.fi.path=messages_fi.properties\n");
            try {
                read(load(config), config);
                fail("Expected " + key + " to be rejected");
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage(), e.getMessage().contains(key));
            }
        }
    }

    @Test
    public void testMissingCatalogFileFails() throws IOException {
        Path config = writeConfig("locale=fi\nlocale.fi.path=absent.properties\n");

        try {
            read(load(config), config);
            fail("Expected a missing catalog file to fail");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("absent.properties"));
        }
    }

    @Test
    public void testDuplicateLanguageFails() throws IOException {
        Path config = writeConfig("locale=fi,finnish\nlocale.finnish.lang=fi\n");

        try {
            read(load(config), config);
            fail("Expected a duplicate language to fail");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("fi"));
        }
    }


    /**
     * The name is not required to be a language: schema.<name>.lang is what is
     * validated, so a schema named for what it is passes as long as it says
     * which language it holds.
     */
    @Test
    public void testSchemaNamedForItselfIsAcceptedWhenItDeclaresItsLanguage() throws IOException {
        Path config = writeConfig("locale=en,fi\nschema=my_schema\n"
                + "schema.my_schema.path=my.json\nschema.my_schema.lang=fi\n");

        assertEquals(Arrays.asList("en", "fi"), read(load(config), config).getLanguages());
    }

    @Test
    public void testSchemaLanguageOutsideLocaleFails() throws IOException {
        Path config = writeConfig("locale=en,fi\nschema=sv\nschema.sv.path=sv.json\n");

        try {
            read(load(config), config);
            fail("Expected a schema language outside locale= to fail");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("sv"));
        }
    }

    /**
     * A schema named for what it is and given no lang defaults to its name,
     * which is not a declared language.
     */
    @Test
    public void testSchemaWithoutLanguageFails() throws IOException {
        Path config = writeConfig("locale=en,fi\nschema=my_schema\nschema.my_schema.path=my.json\n");

        try {
            read(load(config), config);
            fail("Expected a schema with no declared language to fail");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("my_schema"));
        }
    }

    /**
     * Schemas predate localization, so a service with no locale= keeps working
     * with whatever schema names it already has.
     */
    @Test
    public void testSchemasAreNotValidatedWithoutLocale() throws IOException {
        Path config = writeConfig("schema=my_schema\nschema.my_schema.path=my.json\n");

        assertTrue(read(load(config), config).isEmpty());
    }

    private Localization read(Properties cfg, Path configPath) {
        return new HakunaConfigParser(cfg).readLocalization(configPath);
    }

    private Path writeConfig(String content) throws IOException {
        return write("hakuna.properties", content);
    }

    private Path write(String name, String content) throws IOException {
        Path path = tmp.getRoot().toPath().resolve(name);
        Files.writeString(path, content, StandardCharsets.UTF_8);
        return path;
    }

    private static Properties load(Path path) throws IOException {
        Properties p = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            p.load(reader);
        }
        return p;
    }

}
