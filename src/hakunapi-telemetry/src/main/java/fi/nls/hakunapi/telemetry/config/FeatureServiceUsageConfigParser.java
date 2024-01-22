package fi.nls.hakunapi.telemetry.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.config.HakunaConfigParser;
import fi.nls.hakunapi.core.telemetry.FeatureServiceTelemetry;
import fi.nls.hakunapi.telemetry.LoggingFeatureServiceTelemetry;

public class FeatureServiceUsageConfigParser {

    protected static final String TELEMETRY_LOG_JSON = "json";
    protected static final String TELEMETRY_LOG_NOP = "NOP";
    protected static final String TELEMETRY_MODE = "telemetry.mode";
    protected static final String TELEMETRY_LOGGER = "telemetry.logger";
    protected static final String TELEMETRY_FIELDS = "telemetry.fields";
    protected static final String TELEMETRY_FIELDS_HEADER = "telemetry.fields.%s.header";
    protected static final String TELEMETRY_COLLECTIONS = "telemetry.collections";
    protected static final String TELEMETRY_COLLECTIONS_NAME = "telemetry.collections.%s.name";
    protected static final String TELEMETRY_COLLECTIONS_WILDCARD = "*";

    public static FeatureServiceTelemetry parse(FeatureServiceConfig service, HakunaConfigParser parser) {

        String usageLog = parser.get(TELEMETRY_MODE, TELEMETRY_LOG_NOP);
        if (TELEMETRY_LOG_NOP.equals(usageLog)) {
            return FeatureServiceTelemetry.NOP;
        }

        if (!TELEMETRY_LOG_JSON.equals(usageLog)) {
            return FeatureServiceTelemetry.NOP;
        }

        LoggingFeatureServiceTelemetry telemetry = new LoggingFeatureServiceTelemetry();

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
            return FeatureServiceTelemetry.NOP;

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

        return telemetry;
    }

}
