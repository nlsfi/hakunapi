package fi.nls.hakunapi.simple.webapp.javax.features;

import static com.jayway.jsonassert.JsonAssert.collectionWithSize;
import static com.jayway.jsonassert.JsonAssert.mapContainingKey;
import static com.jayway.jsonassert.JsonAssert.with;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.either;
import static org.junit.Assert.assertFalse;

import java.io.File;

import javax.servlet.ServletContextEvent;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.nls.hakunapi.simple.webapp.javax.HakunaContextListener;
import fi.nls.hakunapi.simple.webapp.javax.HakunaTestServletContext;
import fi.nls.hakunapi.simple.webapp.javax.SimpleFeaturesApplication;

public class OgcApiFeaturesPart1CoreTest extends JerseyTest {
	private static final Logger LOG = LoggerFactory.getLogger(OgcApiFeaturesPart1CoreTest.class);

	@Override
	protected Application configure() {
		enable(TestProperties.LOG_TRAFFIC);
		enable(TestProperties.DUMP_ENTITY);

		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("hakuna/hakuna.properties").getFile());

		System.setProperty("hakuna.config.path", file.getParentFile().getAbsolutePath() + "/");

		final HakunaTestServletContext sc = new HakunaTestServletContext();
		final ServletContextEvent sce = new ServletContextEvent(sc);
		new HakunaContextListener().contextInitialized(sce);

		final Application app = new SimpleFeaturesApplication(sc);

		return app;

	}

	@Test
	public void testLandingPage() {
		final String response = target("/").request().get(String.class);
		LOG.info(response);

		with(response)
				//
				.assertThat("$.title", equalTo("Maastotiedot OGC API Features"));
	}

	@Test
	public void testConformance() {
		final String response = target("/conformance").request().get(String.class);
		LOG.info(response);

		with(response)
				//
				.assertThat("$.conformsTo",
						hasItems(equalTo("http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/core")));
	}

	@Test
	public void testApi() {
		final String response = target("/api").request().get(String.class);
		LOG.info(response);

		with(response)
				//
				.assertThat("$.openapi", equalTo("3.0.1"));
		assertFalse(response.contains("exampleSetFlag"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCollections() {
		final String response = target("/collections").request().get(String.class);
		LOG.info(response);

		with(response)
				//
				.assertThat("$.collections[?(@.id == 'aallonmurtaja')]", is(collectionWithSize(equalTo(1))))
				.assertThat("$.collections[?(@.id == 'test_collection')]", is(collectionWithSize(equalTo(1))))
				.assertThat("$.collections[?(@.id == 'should_not_exist')]", is(collectionWithSize(equalTo(0))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCollectionsAallonmurtaja() {
		final String response = target("/collections/aallonmurtaja").request().get(String.class);
		LOG.info(response);

		with(response)
				//
				.assertThat("$.id", equalTo("aallonmurtaja"))
				//
				.assertThat("$.title", equalTo("Aallonmurtaja"))

				//
				.assertThat("$.links[?(@.rel == 'items')]", is(collectionWithSize(equalTo(2))))

				//
				.assertThat("$.links[?(@.rel == 'items')].href",
						hasItems("https://localhost/hakuna/collections/aallonmurtaja/items",
								"https://localhost/hakuna/collections/aallonmurtaja/items?f=json"));
	}

    @SuppressWarnings("unchecked")
    @Test
    public void testCollectionsAallonmurtajaWithApiKey() {
        final String response = target("/collections/aallonmurtaja").queryParam("api-key", "12345").request().get(String.class);
        LOG.info(response);

        with(response)
                //
                .assertThat("$.id", equalTo("aallonmurtaja"))
                //
                .assertThat("$.title", equalTo("Aallonmurtaja"))

                //
                .assertThat("$.links[?(@.rel == 'items')]", is(collectionWithSize(equalTo(2))))

                //
                .assertThat("$.links[?(@.rel == 'items')].href",
                        either(
                                hasItems("https://localhost/hakuna/collections/aallonmurtaja/items?api-key=12345",
                                        "https://localhost/hakuna/collections/aallonmurtaja/items?api-key=12345&f=json")
                        ).or(
                                hasItems("https://localhost/hakuna/collections/aallonmurtaja/items?api-key=12345",
                                        "https://localhost/hakuna/collections/aallonmurtaja/items?f=json&api-key=12345")
                        ));
    }

	@Test
	public void testCollectionsAallonmurtajaQueryables() {
		final String response = target("/collections/aallonmurtaja/queryables").request().get(String.class);
		LOG.info(response);

		with(response)
				//
				.assertThat("$['$schema']", equalTo("https://json-schema.org/draft/2019-09/schema"))
				//
				.assertThat("$['$id']", equalTo("https://localhost/hakuna/collections/aallonmurtaja/queryables"))
				//
				.assertThat("$.title", equalTo("Aallonmurtaja"))
				//
				.assertThat("$.type", equalTo("object"))
				//
				.assertThat("$.properties", mapContainingKey(equalTo("mtk_id"))).and()
				//
				.assertThat("$.properties", mapContainingKey(equalTo("kohdeluokka")));

	}

	@Test
	public void testCollectionsAallonmurtajaSchema() {
		final String response = target("/collections/aallonmurtaja/schema").request().get(String.class);
		LOG.info(response);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCollectionsAallonmurtajaItems() {
		final String response = target("/collections/aallonmurtaja/items").request().get(String.class);
		LOG.info(response);

		with(response)
				//
				.assertThat("$.numberReturned", equalTo(2)).and()
				//
				.assertThat("$.features[?(@.id == '1')]", collectionWithSize(equalTo(1))).and()
				//
				.assertThat("$.features[0].properties.mtk_id", equalTo("11")).and()
				//
				.assertThat("$.features[0].properties.sijaintitarkkuus", equalTo("4.0")).and()
				//
				.assertThat("$.features[0].properties.kulkutapa", equalTo("11")).and()
				//
				.assertThat("$.features[0].properties.kohderyhma", equalTo("12")).and()
				//
				.assertThat("$.features[0].properties.kohdeluokka", equalTo("13")).and()
				//
				.assertThat("$.features[0].geometry.type", equalTo("Point"));

	}

	@Test
	public void testCollectionsAallonmurtajaItems1() {
		final String response = target("/collections/aallonmurtaja/items/1").request().get(String.class);
		LOG.info(response);

		with(response)
				//
				//
				.assertThat("$.type", equalTo("Feature")).and()
				//
				.assertThat("$.id", is(equalTo("1"))).and()
				//
				.assertThat("$.properties.mtk_id", equalTo("11")).and()
				//
				.assertThat("$.geometry", mapContainingKey(equalTo("type")));

	}

	@Test
	public void testCollectionsAallonmurtajaItems2() {
		final String response = target("/collections/aallonmurtaja/items/2").request().get(String.class);
		LOG.info(response);

		with(response)
				//
				//
				.assertThat("$.type", equalTo("Feature")).and()
				//
				.assertThat("$.id", is(equalTo("2"))).and()
				//
				.assertThat("$.properties.mtk_id", equalTo("22")).and()
				//
				.assertThat("$.geometry", mapContainingKey(equalTo("type")));

	}

    @SuppressWarnings("unchecked")
    @Test
    public void testTestCollection() {
        final String response = target("/collections/test_collection").request().get(String.class);
        LOG.info(response);

        with(response)
                //
                .assertThat("$.id", equalTo("test_collection"))
                //
                .assertThat("$.title", equalTo("Test Collection"))

                .assertThat("$.extent.spatial.bbox", collectionWithSize(equalTo(1)))
                .assertThat("$.extent.spatial.bbox[0]", collectionWithSize(equalTo(4)))
                .assertThat("$.extent.spatial.bbox[0][0]", equalTo(-110.0))
                .assertThat("$.extent.spatial.bbox[0][1]", equalTo(-60.0))
                .assertThat("$.extent.spatial.bbox[0][2]", equalTo(140.0))
                .assertThat("$.extent.spatial.bbox[0][3]", equalTo(30.0))

                //
                .assertThat("$.links[?(@.rel == 'items')]", is(collectionWithSize(equalTo(2))))

                //
                .assertThat("$.links[?(@.rel == 'items')].href",
                        hasItems("https://localhost/hakuna/collections/test_collection/items",
                                "https://localhost/hakuna/collections/test_collection/items?f=json"));
    }

}
