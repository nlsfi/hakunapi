package fi.nls.hakunapi.core.extension;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import fi.nls.hakunapi.core.ConformanceClass;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.operation.OperationImpl;
import fi.nls.hakunapi.core.schemas.CollectionInfo;
import fi.nls.hakunapi.core.schemas.Link;

public interface ApiExtension extends Closeable {

    public static final ApiExtension NOP = new ApiExtension() {

        @Override
        public String getName() {
            return "nop";
        }

        @Override
        public List<ConformanceClass> getConformanceClasses() {
            return List.of();
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
            return List.of();
        }

        @Override
        public List<Link> getCollectionLinks(FeatureType ft, String baseUrl, Map<String, String> queryParams) {
            return List.of();
        }

        @Override
        public List<CollectionInfo> getCollections(String baseUrl, Map<String, String> queryParams) {
            return List.of();
        }

        @Override
        public List<String> getCollectionIds(String resource) {
            return List.of();
        }

        @Override
        public Map<String, List<String>> getFormatMediaTypes() {
            return Map.of();
        }

        @Override
        public void close() {
            // NOP
        }

    };

    public String getName();

    public List<ConformanceClass> getConformanceClasses();

    public List<OperationImpl> getOperations();

    public Map<Class<?>, Object> getInjectables();

    /** baseUrl is without trailing slash; queryParams may be modified during the call but must be restored before returning. */
    public List<Link> getLandingPageLinks(String baseUrl, Map<String, String> queryParams);

    public List<Link> getCollectionLinks(FeatureType ft, String baseUrl, Map<String, String> queryParams);

    /** Non-feature collections, listed in /collections and resolvable at /collections/{collectionId}. */
    public List<CollectionInfo> getCollections(String baseUrl, Map<String, String> queryParams);

    /** Collection ids for which this extension serves the named resource, used to expand DynamicPathOperation paths. */
    public List<String> getCollectionIds(String resource);

    /** Maps extra f values to media types, most preferred first; must not collide with MetadataFormat or output format ids. */
    public Map<String, List<String>> getFormatMediaTypes();

    @Override
    public void close() throws IOException;

}
