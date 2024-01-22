package fi.nls.hakunapi.telemetry;

import java.util.Map;

import org.slf4j.Logger;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import fi.nls.hakunapi.core.request.WriteReport;
import fi.nls.hakunapi.core.telemetry.RequestTelemetry;
import fi.nls.hakunapi.core.telemetry.TelemetrySpan;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;

public class TracingRequestTelemetry implements RequestTelemetry {

    protected final Logger log;
    protected final GetFeatureRequest request;
    protected final FeatureType ft;
    protected final Map<String, String> headersMap;
    protected final Tracer tracer;
    protected final OpenTelemetry openTelemetry;

    public TracingRequestTelemetry(GetFeatureRequest request, Logger log, Map<String, String> headersMap, Tracer tracer,
            OpenTelemetry openTelemetry) {
        this.log = log;
        this.request = request;
        this.ft = request.getCollections().get(0).getFt();
        this.headersMap = headersMap;
        this.tracer = tracer;
        this.openTelemetry = openTelemetry;
    }

    @Override
    public TelemetrySpan span() {
        return new TracingTelemetrySpan(ft.getName());
    }

    protected String getHeaderValue(String key) {
        return request.getQueryHeaders().get(key);
    }

    // https://opentelemetry.io/docs/languages/java/instrumentation/#context-propagation
    
    TextMapGetter<GetFeatureRequest> getter = new TextMapGetter<>() {
        @Override
        public String get(GetFeatureRequest carrier, String key) {
            if (carrier.getQueryHeaders().containsKey(key)) {
                return carrier.getQueryHeaders().get(key);
            }
            return null;
        }

        @Override
        public Iterable<String> keys(GetFeatureRequest carrier) {
            return carrier.getQueryHeaders().keySet();
        }
    };

    class TracingTelemetrySpan implements TelemetrySpan {

        private Span span;
        private Scope scope;

        TracingTelemetrySpan(String name) {
            Context extractedContext = openTelemetry.getPropagators().getTextMapPropagator().extract(Context.current(),
                    request, getter);
            scope = extractedContext.makeCurrent();
            span = tracer.spanBuilder(name).setSpanKind(SpanKind.SERVER).startSpan();
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
            scope.close();
        }
    }

}
