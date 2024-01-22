package fi.nls.hakunapi.telemetry;

import java.util.Map;

import org.slf4j.Logger;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import fi.nls.hakunapi.core.request.WriteReport;
import fi.nls.hakunapi.core.telemetry.RequestTelemetry;
import fi.nls.hakunapi.core.telemetry.TelemetrySpan;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;

public class TracingRequestTelemetry implements RequestTelemetry {

    protected final Logger log;
    protected final GetFeatureRequest request;
    protected final FeatureType ft;
    protected final Map<String, String> headersMap;
    protected final Tracer tracer;

    public TracingRequestTelemetry(GetFeatureRequest request, Logger log, 
            Map<String, String> headersMap,
            Tracer tracer) {
        this.log = log;
        this.request = request;
        this.ft = request.getCollections().get(0).getFt();
        this.headersMap = headersMap;
        this.tracer = tracer;
    }

    @Override
    public TelemetrySpan span() {
        return new TracingTelemetrySpan(ft.getName());
    }

    protected String getHeaderValue(String key) {
        return request.getQueryHeaders().get(key);
    }

    class TracingTelemetrySpan implements TelemetrySpan {

        private Span span;

        TracingTelemetrySpan(String name) {
            span = tracer.spanBuilder(name).startSpan();
        }

        @Override
        public void counts(int count) {
            span.setAttribute("count", count);
        }

        @Override
        public void counts(WriteReport report) {
            span.setAttribute("count", report.numberReturned);
        }

        @Override
        public void close() {
            span.end();
        }
    }

}
