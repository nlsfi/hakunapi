package fi.nls.hakunapi.core.telemetry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.config.HakunaConfigParser;

public class TelemetryConfigParser {

    private static final Logger LOG = LoggerFactory.getLogger(TelemetryConfigParser.class);

    protected static final String TELEMETRY_LOG_NOP = "NOP";
    protected static final String TELEMETRY_MODE = "telemetry.mode";
    protected static final String TELEMETRY_LOGGER = "telemetry.logger";
    protected static final String TELEMETRY_FIELDS = "telemetry.fields";
    protected static final String TELEMETRY_FIELDS_HEADER = "telemetry.fields.%s.header";
    protected static final String TELEMETRY_COLLECTIONS = "telemetry.collections";
    protected static final String TELEMETRY_COLLECTIONS_NAME = "telemetry.collections.%s.name";
    protected static final String TELEMETRY_COLLECTIONS_WILDCARD = "*";
    

    public static ServiceTelemetry parse(FeatureServiceConfig service, HakunaConfigParser parser) {

        String telemetryId = parser.get(TELEMETRY_MODE, TELEMETRY_LOG_NOP);
        if (TELEMETRY_LOG_NOP.equals(telemetryId)) {
            LOG.info("Using no-op telemetry");
            return ServiceTelemetry.NOP;
        }
       
        ServiceTelemetry telemetry = TelemetryProvider.getTelemetries().get(telemetryId);
        if(telemetry==null) {
            LOG.info("Telemetry not found "+telemetryId+". Using no-op telemetry");
            return ServiceTelemetry.NOP;
        }

        LOG.info("Using telemetry "+telemetry.getClass().getName());

        String usageLogName = parser.get(TELEMETRY_LOGGER);
        telemetry.setName(usageLogName);

        String[] headers = parser.getMultiple(TELEMETRY_FIELDS);

        Map<String, String> headersMap = new HashMap<>();
        telemetry.setHeaders(headersMap);
        if (headers != null) {
            for (String header : headers) {
                String lookup = parser.get(String.format(TELEMETRY_FIELDS_HEADER, header), header);
                headersMap.put(header, lookup);
            }
        }

        String collectionsWildcard = parser.get(TELEMETRY_COLLECTIONS);
        if (collectionsWildcard == null) {
            return ServiceTelemetry.NOP;

        } else if (service != null && "*".equals(collectionsWildcard)) {
            Collection<FeatureType> collections = service.getCollections();

            // defaults to logging ft.name as ft.name
            Map<String, String> collectionsMap = collections.stream()
                    .collect(Collectors.toMap(FeatureType::getName, FeatureType::getName));
            telemetry.setCollections(collectionsMap);

        } else {
            String[] collections = parser.getMultiple(TELEMETRY_COLLECTIONS);
            Map<String, String> collectionsMap = new HashMap<>();
            telemetry.setCollections(collectionsMap);

            // defaults to logging ft.name as ft.name with optional rename
            if (collections != null) {
                for (String collection : collections) {
                    String lookup = parser.get(String.format(TELEMETRY_COLLECTIONS_NAME, collection), collection);
                    collectionsMap.put(collection, lookup);
                }
            }
        }
        
        telemetry.parse(service,parser);

        return telemetry;
    }
    
}
