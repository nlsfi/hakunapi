package fi.nls.hakunapi.telemetry;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;

import fi.nls.hakunapi.core.DatetimeProperty;
import fi.nls.hakunapi.core.FeatureProducer;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.PaginationStrategy;
import fi.nls.hakunapi.core.PaginationStrategyOffset;
import fi.nls.hakunapi.core.config.HakunaConfigParser;
import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.geom.HakunaGeometryDimension;
import fi.nls.hakunapi.core.param.GetFeatureParam;
import fi.nls.hakunapi.core.projection.ProjectionTransformerFactory;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;
import fi.nls.hakunapi.core.request.GetFeatureCollection;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import fi.nls.hakunapi.core.telemetry.ServiceTelemetry;
import fi.nls.hakunapi.core.telemetry.TelemetryConfigParser;
import fi.nls.hakunapi.core.telemetry.RequestTelemetry;
import fi.nls.hakunapi.core.telemetry.TelemetrySpan;

public class TestLoggingFeatureServiceTelemetry {

    @Test
    public void testReadConfig() throws IOException {

        Properties props = new Properties();
        props.load(getClass().getResourceAsStream("telemetry.properties"));

        HakunaConfigParser parser = new HakunaConfigParser(props);

        ServiceTelemetry fst = TelemetryConfigParser.parse(null, parser);

        assertTrue(fst instanceof LoggingServiceTelemetry);

        Map<String, String> testHeaders = Map.of("remote-user", "testuser");

        GetFeatureRequest request = new GetFeatureRequest();
        request.setQueryHeaders(testHeaders);
        GetFeatureCollection c = new GetFeatureCollection(ft);
        request.addCollection(c);
        
        
        RequestTelemetry ftt = fst.forRequest(request);
        try (TelemetrySpan span = ftt.span()) {
            Thread.sleep(1000);
            span.counts(5);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    FeatureType ft = new FeatureType() {

        @Override
        public String getName() {
            return "example-collection";
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

            return null;
        }

        @Override
        public String getDescription() {

            return null;
        }

        @Override
        public Map<String, Object> getMetadata() {

            return null;
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
            return List.of();
        }

        @Override
        public List<HakunaProperty> getQueryableProperties() {

            return null;
        }

        @Override
        public List<DatetimeProperty> getDatetimeProperties() {

            return null;
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

            return null;
        }

        @Override
        public List<Filter> getStaticFilters() {

            return List.of();
        }

        @Override
        public ProjectionTransformerFactory getProjectionTransformerFactory() {

            return null;
        }

        @Override
        public HakunaGeometryDimension getGeomDimension() {

            return null;
        }

        @Override
        public PaginationStrategy getPaginationStrategy() {

            return PaginationStrategyOffset.INSTANCE;
        }

        @Override
        public FeatureProducer getFeatureProducer() {

            return null;
        }

    };
}
