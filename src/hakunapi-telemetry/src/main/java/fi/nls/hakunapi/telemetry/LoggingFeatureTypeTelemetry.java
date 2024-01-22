package fi.nls.hakunapi.telemetry;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.request.WriteReport;
import fi.nls.hakunapi.core.telemetry.FeatureTypeTelemetry;
import fi.nls.hakunapi.core.telemetry.FeatureTypeTelemetrySpan;

public class LoggingFeatureTypeTelemetry implements FeatureTypeTelemetry {

    protected final Map<String, Object> values = new HashMap<>();
    protected final Map<String, Integer> counts = new HashMap<>();
    protected final Logger log;
    protected final FeatureType ft;
    protected final Map<String, String> headersMap;

    public LoggingFeatureTypeTelemetry(FeatureType ft, Logger log, Map<String, String> headersMap) {
        this.log = log;
        this.ft = ft;
        this.headersMap = headersMap;
    }

    @Override
    public FeatureTypeTelemetrySpan span() {
        return new LoggingFeatureTypeTelemetrySpan();
    }
    
    @Override
    public void headers(ToValue v) {
        // this is to get implementation independent headers
        // key = logged header name, value = actual header name
        // value = actual value
        headersMap.entrySet().forEach(e -> {
            Object value = v.get(e.getValue());
            if (value != null) {
                values.put(e.getKey(), value);
            }

        });
    }

    class LoggingFeatureTypeTelemetrySpan implements FeatureTypeTelemetrySpan {

        @Override
        public void counts(int count) {
            final String name = ft.getName();
            counts.put(name, count);
        }

        @Override
        public void counts(WriteReport report) {
            final String name = ft.getName();
            counts.put(name, report.numberReturned);
        }

        @Override
        public void close() {
            if(counts.isEmpty()) {
                return;
            }
            values.putAll(counts);
            try {
                String str = LoggingFeatureServiceTelemetry.MAPPER.writeValueAsString(values);
                if (str != null) {
                    log.info(str);
                }
            } catch (Exception e) {
            }
        }
    }

}
