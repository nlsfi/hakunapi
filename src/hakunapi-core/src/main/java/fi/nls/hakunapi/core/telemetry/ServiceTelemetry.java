package fi.nls.hakunapi.core.telemetry;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.config.HakunaConfigParser;
import fi.nls.hakunapi.core.request.GetFeatureRequest;

public interface ServiceTelemetry extends Closeable {

    public String getId();
    
    public void setName(String name);
    public String getName();
    public void setHeaders(Map<String, String> headersMap) ;
    public void setCollections(Map<String, String> collectionsMap);
    
    public void parse(FeatureServiceConfig service, HakunaConfigParser parser);
    public void start();

    public RequestTelemetry forRequest(GetFeatureRequest r);

    
    // No op implementation
    static ServiceTelemetry NOP = new ServiceTelemetry() {

        String name;
        @Override
        public RequestTelemetry forRequest(GetFeatureRequest r) {
            return NOPFeatureTypeTelemetry.NOP;

        }

        @Override
        public void setName(String name) {
            this.name = name;                
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void setHeaders(Map<String, String> headersMap) {
        }

        @Override
        public String getId() {
            return "nop";
        }

        @Override
        public void setCollections(Map<String, String> collectionsMap) {
        }

        @Override
        public void parse(FeatureServiceConfig service, HakunaConfigParser parser) {
        }

        @Override
        public void start() {
        }

        @Override
        public void close() throws IOException {
        }

    };

    public static class NOPFeatureTypeTelemetry implements RequestTelemetry {


        public static final NOPFeatureTypeTelemetry NOP = new NOPFeatureTypeTelemetry();

        @Override
        public TelemetrySpan span() {
            return TelemetrySpan.NOP;
        }
    }




}