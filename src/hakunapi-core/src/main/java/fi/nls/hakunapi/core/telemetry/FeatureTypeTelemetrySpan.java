package fi.nls.hakunapi.core.telemetry;

import fi.nls.hakunapi.core.request.WriteReport;

public interface FeatureTypeTelemetrySpan extends AutoCloseable {

    default void counts(int count) {};
    default void counts(WriteReport report) {};
    default void put(String key, String value)  {};

    public void close();

    // No op implementation
    FeatureTypeTelemetrySpan NOP = new FeatureTypeTelemetrySpan() {

        @Override
        public void close() {

        }

    };

}
