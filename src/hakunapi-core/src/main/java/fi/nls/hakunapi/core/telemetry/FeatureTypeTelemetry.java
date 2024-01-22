package fi.nls.hakunapi.core.telemetry;

public interface FeatureTypeTelemetry {

    @FunctionalInterface
    public interface ToValue {
        String get(String key);
    }

    // implementation independent (javax vs. jakarta etc) header
    public default void headers(ToValue v) {}
    public FeatureTypeTelemetrySpan span();

}