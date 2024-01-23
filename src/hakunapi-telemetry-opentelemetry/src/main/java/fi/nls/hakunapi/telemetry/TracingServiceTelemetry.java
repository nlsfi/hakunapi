package fi.nls.hakunapi.telemetry;

import static fi.nls.hakunapi.core.telemetry.TelemetryConfigParser.TELEMETRY_MODULE_PROP;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.config.HakunaConfigParser;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import fi.nls.hakunapi.core.telemetry.RequestTelemetry;
import fi.nls.hakunapi.core.telemetry.ServiceTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.logging.LoggingMetricExporter;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

public class TracingServiceTelemetry implements ServiceTelemetry {

    private static final long METRIC_EXPORT_INTERVAL_MS = 800L;

    protected String name;
    protected Logger log;
    protected Map<String, String> headersMap;
    protected Map<String, String> collectionsMap;

    protected OpenTelemetry openTelemetry;
    protected Tracer tracer;

    protected long exportIntervalMs;

    private PeriodicMetricReader periodicReader;
    private SdkMeterProvider meterProvider;
    private SdkTracerProvider tracerProvider;

    public TracingServiceTelemetry() {
    }

    @Override
    public String getId() {
        return "opentelemetry";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
        if (name == null) {
            return;
        }
        log = LoggerFactory.getLogger(name);
    }

    public void setHeaders(Map<String, String> headersMap) {
        this.headersMap = headersMap;
    }

    public void setCollections(Map<String, String> collectionsMap) {
        this.collectionsMap = collectionsMap;
    }

    @Override
    public RequestTelemetry forRequest(GetFeatureRequest request) {
        if (log == null) {
            return NOPFeatureTypeTelemetry.NOP;
        }
        if (collectionsMap.isEmpty()) {
            return NOPFeatureTypeTelemetry.NOP;
        }
        if (request == null) {
            return NOPFeatureTypeTelemetry.NOP;
        }

        FeatureType ft = request.getCollections().get(0).getFt();
        if (!collectionsMap.containsKey(ft.getName())) {
            return NOPFeatureTypeTelemetry.NOP;
        }
        return new TracingRequestTelemetry(request, log, headersMap, tracer, openTelemetry);
    }

    @Override
    public void parse(FeatureServiceConfig service, HakunaConfigParser parser) {

        String intervalValue = parser.get(String.format(TELEMETRY_MODULE_PROP, "opentelemetry.export.interval"));

        exportIntervalMs = intervalValue != null ? Long.valueOf(intervalValue) : METRIC_EXPORT_INTERVAL_MS;

    }

    @Override
    public void close() throws IOException {
        meterProvider.close();
        tracerProvider.close();
    }

    @Override
    public void start() {
        initOpenTelemetry(exportIntervalMs);
        tracer = openTelemetry.getTracer(name);
    }

    /**
     * Initializes an OpenTelemetry SDK with a logging exporter and a
     * SimpleSpanProcessor.
     *
     * @return A ready-to-use {@link OpenTelemetry} instance.
     */
    public void initOpenTelemetry(long exportInterval) {
        // Create an instance of PeriodicMetricReader and configure it
        // to export via the logging exporter
        periodicReader = PeriodicMetricReader.builder(LoggingMetricExporter.create())
                .setInterval(Duration.ofMillis(exportInterval)).build();

        // This will be used to create instruments
        meterProvider = SdkMeterProvider.builder().registerMetricReader(periodicReader).build();

        // Tracer provider configured to export spans with SimpleSpanProcessor using
        // the logging exporter.
        tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(LoggingSpanExporter.create())).build();
        openTelemetry = OpenTelemetrySdk.builder().setMeterProvider(meterProvider).setTracerProvider(tracerProvider)
                .buildAndRegisterGlobal();
    }
}
