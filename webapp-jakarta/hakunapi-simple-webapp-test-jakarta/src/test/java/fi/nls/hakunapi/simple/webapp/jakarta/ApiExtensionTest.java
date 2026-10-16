package fi.nls.hakunapi.simple.webapp.jakarta;

import static com.jayway.jsonassert.JsonAssert.collectionWithSize;
import static com.jayway.jsonassert.JsonAssert.with;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import jakarta.servlet.ServletContextEvent;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.nls.hakunapi.core.ConformanceClass;

public class ApiExtensionTest extends JerseyTest {

	private static final Logger LOG = LoggerFactory.getLogger(ApiExtensionTest.class);

	@Override
	protected Application configure() {
		enable(TestProperties.LOG_TRAFFIC);
		enable(TestProperties.DUMP_ENTITY);

		return boot("hakuna_api_extension/hakuna.properties");
	}

	private static Application boot(String configResource) {
		ClassLoader classLoader = ApiExtensionTest.class.getClassLoader();
		File file = new File(classLoader.getResource(configResource).getFile());

		System.setProperty("hakuna.config.path", file.getParentFile().getAbsolutePath() + "/");

		final HakunaTestServletContext sc = new HakunaTestServletContext();
		final ServletContextEvent sce = new ServletContextEvent(sc);
		new HakunaContextListener().contextInitialized(sce);

		return new SimpleFeaturesApplication(sc);
	}

	@Test
	public void testExtensionPublishedOnlyWhenConfigured() throws Exception {
		String landing = target("/").request().get(String.class);
		LOG.info(landing);
		with(landing)
				.assertThat("$.links[?(@.rel == '" + TestApiExtensionFactory.LINK_REL + "')]",
						is(collectionWithSize(equalTo(1))))
				.assertThat("$.links[?(@.rel == '" + TestApiExtensionFactory.LINK_REL + "')].href",
						hasItems(endsWith("/" + TestApiExtensionFactory.LINK_REL)));

		String conformance = target("/conformance").request().get(String.class);
		LOG.info(conformance);
		with(conformance).assertThat("$.conformsTo", hasItems(equalTo(TestApiExtensionFactory.CONFORMANCE.uri)));

		String collections = target("/collections").request().get(String.class);
		LOG.info(collections);
		with(collections).assertThat("$.collections[*].id",
				hasItems(equalTo(TestApiExtensionFactory.COLLECTION_ID), equalTo("test_collection")));

		Response extCollection = target("/collections/" + TestApiExtensionFactory.COLLECTION_ID).request().get();
		assertEquals(200, extCollection.getStatus());
		with(extCollection.readEntity(String.class))
				.assertThat("$.id", equalTo(TestApiExtensionFactory.COLLECTION_ID))
				.assertThat("$.itemType", equalTo(TestApiExtensionFactory.ITEM_TYPE));

		Response unknownCollection = target("/collections/no-such-collection").request().get();
		assertEquals(404, unknownCollection.getStatus());
		unknownCollection.close();

		String featureCollection = target("/collections/test_collection").request().get(String.class);
		LOG.info(featureCollection);
		with(featureCollection)
				.assertThat("$.links[?(@.rel == '" + TestApiExtensionFactory.COLLECTION_LINK_REL + "')]",
						is(collectionWithSize(equalTo(1))));

		Response testfmt = target("/").queryParam("f", TestApiExtensionFactory.FORMAT_ID).request().get();
		assertEquals(200, testfmt.getStatus());
		assertTrue(testfmt.getMediaType().isCompatible(MediaType.APPLICATION_JSON_TYPE));
		testfmt.close();

		Response nonsense = target("/").queryParam("f", "nonsense").request().get();
		assertEquals(400, nonsense.getStatus());
		with(nonsense.readEntity(String.class))
				.assertThat("$.description", containsString(TestApiExtensionFactory.FORMAT_ID));

		JerseyTest plain = new JerseyTest() {
			@Override
			protected Application configure() {
				enable(TestProperties.LOG_TRAFFIC);
				enable(TestProperties.DUMP_ENTITY);

				return boot("hakuna/hakuna.properties");
			}
		};
		plain.setUp();
		try {
			landing = plain.target("/").request().get(String.class);
			LOG.info(landing);
			with(landing).assertThat("$.links[?(@.rel == '" + TestApiExtensionFactory.LINK_REL + "')]",
					is(collectionWithSize(equalTo(0))));

			conformance = plain.target("/conformance").request().get(String.class);
			LOG.info(conformance);
			with(conformance).assertThat("$.conformsTo",
					not(hasItems(equalTo(TestApiExtensionFactory.CONFORMANCE.uri))));

			Response plainCollection = plain.target("/collections/" + TestApiExtensionFactory.COLLECTION_ID)
					.request().get();
			assertEquals(404, plainCollection.getStatus());
			plainCollection.close();

			Response plainFormat = plain.target("/").queryParam("f", TestApiExtensionFactory.FORMAT_ID)
					.request().get();
			assertEquals(400, plainFormat.getStatus());
			plainFormat.close();
		} finally {
			plain.tearDown();
		}
	}

}
