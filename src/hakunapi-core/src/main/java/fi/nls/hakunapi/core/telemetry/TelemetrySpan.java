package fi.nls.hakunapi.core.telemetry;

import fi.nls.hakunapi.core.request.WriteReport;

public interface TelemetrySpan extends AutoCloseable {

    default void counts(int count) {};
    default void counts(WriteReport report) {};
    default void put(String key, String value)  {};

    public void close();

    // No op implementation
    TelemetrySpan NOP = new TelemetrySpan() {

        @Override
        public void close() {

        }

    };

}
