package fi.nls.hakunapi.core.telemetry;

import fi.nls.hakunapi.core.request.WriteReport;

public interface TelemetrySpan extends AutoCloseable {

    void counts(int count);
    void counts(WriteReport report);
    void put(String key, String value);

    public void close();

    // No op implementation
    TelemetrySpan NOP = new TelemetrySpan() {

        @Override
        public void close() {
        }

        @Override
        public void counts(int count) {
        }

        @Override
        public void counts(WriteReport report) {
        }

        @Override
        public void put(String key, String value) {
        }

    };

}
