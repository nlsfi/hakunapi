package fi.nls.hakunapi.telemetry;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.telemetry.FeatureServiceTelemetry;
import fi.nls.hakunapi.core.telemetry.FeatureTypeTelemetry;

public class LoggingFeatureServiceTelemetry implements FeatureServiceTelemetry {

    protected static ObjectMapper MAPPER = new ObjectMapper();

    protected String name;
    protected Logger log;
    protected Map<String, String> headersMap;
    protected Map<String, String> collectionsMap;

    public LoggingFeatureServiceTelemetry() {
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
    public FeatureTypeTelemetry forFeatureType(FeatureType ft) {
        if (log == null) {
            return NOPFeatureTypeTelemetry.NOP;
        }
        if (collectionsMap.isEmpty()) {
            return NOPFeatureTypeTelemetry.NOP;
        }
        if (ft == null) {
            return NOPFeatureTypeTelemetry.NOP;
        }

        return new LoggingFeatureTypeTelemetry(ft, log, headersMap);
    }

}
