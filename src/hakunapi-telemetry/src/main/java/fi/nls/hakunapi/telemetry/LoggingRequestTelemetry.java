package fi.nls.hakunapi.telemetry;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import fi.nls.hakunapi.core.request.WriteReport;
import fi.nls.hakunapi.core.telemetry.RequestTelemetry;
import fi.nls.hakunapi.core.telemetry.TelemetrySpan;

public class LoggingRequestTelemetry implements RequestTelemetry {

    protected final Map<String, Integer> counts = new HashMap<>();
    protected final Logger log;
    protected final GetFeatureRequest request;
    protected final FeatureType ft;
    protected final Map<String, String> headersMap;

    public LoggingRequestTelemetry(GetFeatureRequest request, Logger log, Map<String, String> headersMap) {
        this.log = log;
        this.request = request;
        this.ft = request.getCollections().get(0).getFt();
        this.headersMap = headersMap;
    }

    @Override
    public TelemetrySpan span() {
        return new LoggingFeatureTypeTelemetrySpan();
    }
    
    
    protected String getHeaderValue(String key) {
        return request.getQueryHeaders().get(key);
    }

    class LoggingFeatureTypeTelemetrySpan implements TelemetrySpan {

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
            Map<String, Object> values = new HashMap<>();
            // this is to get implementation independent headers
            // key = logged header name, value = actual header name
            // value = actual value
            headersMap.entrySet().forEach(e -> {
                Object value = getHeaderValue(e.getValue());
                if (value != null) {
                    values.put(e.getKey(), value);
                }

            });

            values.putAll(counts);
            try {
                String str = LoggingServiceTelemetry.MAPPER.writeValueAsString(values);
                if (str != null) {
                    log.info(str);
                }
            } catch (Exception e) {
            }
        }
    }

}
