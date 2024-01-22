package fi.nls.hakunapi.core.telemetry;

import fi.nls.hakunapi.core.FeatureType;

public interface FeatureServiceTelemetry {

    public default void setName(String name) {};
    public default String getName() { return "telemetry"; };
    public FeatureTypeTelemetry forFeatureType(FeatureType ft);

    
    // No op implementation
    static FeatureServiceTelemetry NOP = new FeatureServiceTelemetry() {

        @Override
        public FeatureTypeTelemetry forFeatureType(FeatureType ft) {
            return NOPFeatureTypeTelemetry.NOP;

        }

    };

    public static class NOPFeatureTypeTelemetry implements FeatureTypeTelemetry {


        public static final NOPFeatureTypeTelemetry NOP = new NOPFeatureTypeTelemetry();

        @Override
        public FeatureTypeTelemetrySpan span() {
            return FeatureTypeTelemetrySpan.NOP;
        }
    }

}