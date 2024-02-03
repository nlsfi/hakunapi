package fi.nls.hakunapi.telemetry;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.config.HakunaConfigParser;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import fi.nls.hakunapi.core.telemetry.RequestTelemetry;
import fi.nls.hakunapi.core.telemetry.ServiceTelemetry;

public class LoggingServiceTelemetry implements ServiceTelemetry {

    protected static ObjectMapper MAPPER = new ObjectMapper();

    protected String name;
    protected Logger log;
    protected Map<String, String> headersMap;
    protected Map<String, String> collectionsMap;

    public LoggingServiceTelemetry() {
    }
    
    @Override
    public String getId() {
        return "log-json";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
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
        return new LoggingRequestTelemetry(request, log, headersMap);
    }

    @Override
    public void parse(FeatureServiceConfig service, HakunaConfigParser parser) {
    }

    @Override
    public void close() throws IOException {        
    }

    @Override
    public void start() {
        if(name==null) {
            return;
        }
        log = LoggerFactory.getLogger(name);
    }
}
