package fi.nls.hakunapi.telemetry;

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

public class TracingServiceTelemetry implements ServiceTelemetry {

     protected String name;
    protected Logger log;
    protected Map<String, String> headersMap;
    protected Map<String, String> collectionsMap;
    
    protected OpenTelemetry openTelemetry;
    protected Tracer tracer;
    

    public TracingServiceTelemetry() {
        openTelemetry = TracingConfiguration.initOpenTelemetry();
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
        tracer = openTelemetry.getTracer(name);
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
        if(!collectionsMap.containsKey(ft.getName())) {
            return NOPFeatureTypeTelemetry.NOP;
        }
        return new TracingRequestTelemetry(request, log, headersMap, tracer);
    }

    @Override
    public void parse(FeatureServiceConfig service, HakunaConfigParser parser) {
        // additional configuration
    }
    
    

}
