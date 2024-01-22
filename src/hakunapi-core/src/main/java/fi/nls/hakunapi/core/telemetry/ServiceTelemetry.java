package fi.nls.hakunapi.core.telemetry;

import java.util.Map;

import fi.nls.hakunapi.core.request.GetFeatureRequest;

public interface ServiceTelemetry {

    public default String getId() {
        return "nop";
    }
    
    public default void setName(String name) {};
    public default String getName() { return "telemetry"; };
    public default void setHeaders(Map<String, String> headersMap) {};
    public default void setCollections(Map<String, String> collectionsMap) {};

    public RequestTelemetry forRequest(GetFeatureRequest r);

    
    // No op implementation
    static ServiceTelemetry NOP = new ServiceTelemetry() {

        @Override
        public RequestTelemetry forRequest(GetFeatureRequest r) {
            return NOPFeatureTypeTelemetry.NOP;

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