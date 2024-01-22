package fi.nls.hakunapi.core.telemetry;

public interface RequestTelemetry {

    @FunctionalInterface
    public interface ToValue {
        String get(String key);
    }

    // implementation independent (javax vs. jakarta etc) header
    public default void headers(ToValue v) {}
    public TelemetrySpan span();

}