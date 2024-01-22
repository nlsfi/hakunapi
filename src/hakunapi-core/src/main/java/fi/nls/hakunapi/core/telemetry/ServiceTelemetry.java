package fi.nls.hakunapi.core.telemetry;

import java.util.Map;

import fi.nls.hakunapi.core.FeatureType;

public interface ServiceTelemetry {

    public default String getId() {
        return "nop";
    }
    
    public default void setName(String name) {};
    public default String getName() { return "telemetry"; };
    public default void setHeaders(Map<String, String> headersMap) {};
    public default void setCollections(Map<String, String> collectionsMap) {};

    public RequestTelemetry forFeatureType(FeatureType ft);

    
    // No op implementation
    static ServiceTelemetry NOP = new ServiceTelemetry() {

        @Override
        public RequestTelemetry forFeatureType(FeatureType ft) {
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