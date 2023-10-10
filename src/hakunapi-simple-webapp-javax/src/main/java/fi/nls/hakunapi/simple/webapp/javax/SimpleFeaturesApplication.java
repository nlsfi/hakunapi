package fi.nls.hakunapi.simple.webapp.javax;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Context;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import fi.nls.hakunapi.core.ConformanceClass;
import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.OutputFormat;
import fi.nls.hakunapi.core.config.HakunaConfigParser;
import fi.nls.hakunapi.core.operation.CollectionMetadataOperation;
import fi.nls.hakunapi.core.operation.CollectionsMetadataOperation;
import fi.nls.hakunapi.core.operation.ConformanceOperation;
import fi.nls.hakunapi.core.operation.FunctionsMetadataOperation;
import fi.nls.hakunapi.core.operation.GetFeatureByIdOperation;
import fi.nls.hakunapi.core.operation.GetFeaturesOperation;
import fi.nls.hakunapi.core.operation.GetQueryablesOperation;
import fi.nls.hakunapi.core.operation.GetSchemaOperation;
import fi.nls.hakunapi.core.operation.LandingPageOperation;
import fi.nls.hakunapi.core.operation.OperationImpl;
import fi.nls.hakunapi.core.schemas.CollectionInfo;
import fi.nls.hakunapi.core.schemas.CollectionsContent;
import fi.nls.hakunapi.core.schemas.ConformanceClasses;
import fi.nls.hakunapi.core.schemas.Queryables;
import fi.nls.hakunapi.core.schemas.Root;
import fi.nls.hakunapi.html.OutputFormatHTML;
import fi.nls.hakunapi.simple.servlet.javax.CacheManager;
import fi.nls.hakunapi.simple.servlet.javax.CatchAllExceptionMapper;
import fi.nls.hakunapi.simple.servlet.javax.CorsFilter;
import fi.nls.hakunapi.simple.servlet.javax.GlobalFQueryParamFilter;
import fi.nls.hakunapi.simple.servlet.javax.GzipFilter;
import fi.nls.hakunapi.simple.servlet.javax.GzipInterceptor;
import fi.nls.hakunapi.simple.servlet.javax.NotFoundExceptionMapper;
import fi.nls.hakunapi.simple.servlet.javax.ObjectMapperProvider;
import fi.nls.hakunapi.simple.servlet.javax.OpenAPIObjectMapperProvider;
import fi.nls.hakunapi.simple.servlet.javax.operation.CollectionMetadataImpl;
import fi.nls.hakunapi.simple.servlet.javax.operation.CollectionsMetadataImpl;
import fi.nls.hakunapi.simple.servlet.javax.operation.ConformanceImpl;
import fi.nls.hakunapi.simple.servlet.javax.operation.FunctionsMetadataImpl;
import fi.nls.hakunapi.simple.servlet.javax.operation.GetCollectionItemByIdOperation;
import fi.nls.hakunapi.simple.servlet.javax.operation.GetCollectionItemsOperation;
import fi.nls.hakunapi.simple.servlet.javax.operation.GetCollectionQueryablesImpl;
import fi.nls.hakunapi.simple.servlet.javax.operation.GetCollectionSchemaImpl;
import fi.nls.hakunapi.simple.servlet.javax.operation.GetItemsOperation;
import fi.nls.hakunapi.simple.servlet.javax.operation.LandingPageImpl;
import fi.nls.hakunapi.simple.servlet.javax.operation.OpenAPI30ApiOperation;
import freemarker.template.Configuration;
import io.swagger.v3.oas.models.OpenAPI;

@ApplicationPath("/")
public class SimpleFeaturesApplication extends ResourceConfig {

    public SimpleFeaturesApplication(@Context ServletContext servletContext) {
        final SimpleFeatureServiceConfig service = (SimpleFeatureServiceConfig) servletContext.getAttribute("hakunaService");
        final HakunaConfigParser parser = (HakunaConfigParser) servletContext.getAttribute("hakunaConfig");

        List<OperationImpl> opToImpl = new ArrayList<>();
        // Core is REQUIRED
        opToImpl.add(new OperationImpl(new LandingPageOperation(), LandingPageImpl.class));
        opToImpl.add(new OperationImpl(new CollectionsMetadataOperation(), CollectionsMetadataImpl.class));
        opToImpl.add(new OperationImpl(new CollectionMetadataOperation(), CollectionMetadataImpl.class));
        opToImpl.add(new OperationImpl(new ConformanceOperation(), ConformanceImpl.class));
        opToImpl.add(new OperationImpl(new GetFeatureByIdOperation(), GetCollectionItemByIdOperation.class));
        opToImpl.add(new OperationImpl(new GetFeaturesOperation(), GetCollectionItemsOperation.class));
        opToImpl.add(new OperationImpl(new GetSchemaOperation(), GetCollectionSchemaImpl.class));

        if (service.conformsTo(ConformanceClass.FILTER)) {
            opToImpl.add(new OperationImpl(new GetQueryablesOperation(), GetCollectionQueryablesImpl.class));
        }
        
        if(service.getFunctions()!=null) {
            opToImpl.add(new OperationImpl(new FunctionsMetadataOperation(), FunctionsMetadataImpl.class));            
        }

        // Experimental
        opToImpl.add(new OperationImpl(new GetFeaturesOperation(), GetItemsOperation.class));

        for (OperationImpl p : opToImpl) {
            register(p.implementation);
        }

        register(NotFoundExceptionMapper.class);
        register(CatchAllExceptionMapper.class);

        register(GlobalFQueryParamFilter.class);

        if (Boolean.parseBoolean(parser.get("hakuna.cors", "true"))) {
            register(CorsFilter.class);
        }
        if (Boolean.parseBoolean(parser.get("hakuna.gzip", "true"))) {
            register(GzipFilter.class);
            register(GzipInterceptor.class);
        }

        final OpenAPI30ApiOperation api = new OpenAPI30ApiOperation(service, opToImpl);
        register(OpenAPI30ApiOperation.class);

        final CacheManager cacheManager = new CacheManager();

        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(service).to(FeatureServiceConfig.class);
                bind(service).to(SimpleFeatureServiceConfig.class);
                bind(api).to(OpenAPI30ApiOperation.class);
                bind(cacheManager).to(CacheManager.class);
            }
        });

        property(ServerProperties.WADL_FEATURE_DISABLE, true);

        // Use Jackson as POJO provider
        register(ObjectMapperProvider.class);
        register(JacksonFeature.class);

        register(OpenAPIObjectMapperProvider.class);

        OutputFormat html = service.getOutputFormat(OutputFormatHTML.ID);
        if (html != null) {
            Configuration cfg = ((OutputFormatHTML) html).getConfiguration();
            HTMLMessageBodyWriter htmlWriter = new HTMLMessageBodyWriter(cfg);
            htmlWriter.add(CollectionInfo.class, "collectionInfo.ftl");
            htmlWriter.add(CollectionsContent.class, "collections.ftl");
            htmlWriter.add(ConformanceClasses.class, "conformance.ftl");
            htmlWriter.add(Root.class, "root.ftl");
            htmlWriter.add(OpenAPI.class, "api.ftl");
            htmlWriter.add(Queryables.class, "queryables.ftl");
            register(htmlWriter);
        }
    }

}
