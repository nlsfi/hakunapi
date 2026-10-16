package fi.nls.hakunapi.simple.webapp.jakarta;

import java.util.List;
import java.util.Map;

import fi.nls.hakunapi.core.ConformanceClass;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.extension.ApiExtension;
import fi.nls.hakunapi.core.extension.ApiExtensionContext;
import fi.nls.hakunapi.core.extension.ApiExtensionFactory;
import fi.nls.hakunapi.core.operation.OperationImpl;
import fi.nls.hakunapi.core.schemas.CollectionInfo;
import fi.nls.hakunapi.core.schemas.Link;

public class TestApiExtensionFactory implements ApiExtensionFactory {

	public static final String LINK_REL = "test-extension";

	// Unused by the rest of the code base, so it does not change any other conformance output
	public static final ConformanceClass CONFORMANCE = ConformanceClass.ARITHMETIC;

	public static final String ITEM_TYPE = "test-item";
	public static final String COLLECTION_ID = "test-ext-collection";
	public static final String COLLECTION_LINK_REL = "test-ext-collection-link";
	public static final String FORMAT_ID = "testfmt";

	@Override
	public ApiExtension create(ApiExtensionContext context) {
		if (!"true".equals(context.config().get("test.apiextension"))) {
			return null;
		}
		return new TestApiExtension();
	}

	static final class TestApiExtension implements ApiExtension {

		@Override
		public String getName() {
			return "test-extension";
		}

		@Override
		public List<ConformanceClass> getConformanceClasses() {
			return List.of(CONFORMANCE);
		}

		@Override
		public List<OperationImpl> getOperations() {
			return List.of();
		}

		@Override
		public Map<Class<?>, Object> getInjectables() {
			return Map.of();
		}

		@Override
		public List<Link> getLandingPageLinks(String baseUrl, Map<String, String> queryParams) {
			return List.of(new Link(baseUrl + "/" + LINK_REL, LINK_REL, "application/json", "Test extension"));
		}

		@Override
		public List<Link> getCollectionLinks(FeatureType ft, String baseUrl, Map<String, String> queryParams) {
			return List.of(new Link(baseUrl + "/collections/" + ft.getName() + "/" + COLLECTION_LINK_REL,
					COLLECTION_LINK_REL, "application/json", "Test extension link"));
		}

		@Override
		public List<CollectionInfo> getCollections(String baseUrl, Map<String, String> queryParams) {
			String self = baseUrl + "/collections/" + COLLECTION_ID;
			CollectionInfo info = new CollectionInfo(COLLECTION_ID, "Test extension collection",
					"Non-feature collection published by the test extension",
					List.of(new Link(self, "self", "application/json")), null, null, null);
			info.setItemType(ITEM_TYPE);
			return List.of(info);
		}

		@Override
		public List<String> getCollectionIds(String resource) {
			return List.of();
		}

		@Override
		public Map<String, List<String>> getFormatMediaTypes() {
			return Map.of(FORMAT_ID, List.of("application/json"));
		}

		@Override
		public void close() {
		}

	}

}
