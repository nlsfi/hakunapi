package fi.nls.hakunapi.core.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HakunaApplicationJson {
    public static final String HAKUNA_APPLICATION_JSON_PROP = "HAKUNA_APPLICATION_JSON";

    private static final Logger LOG = LoggerFactory.getLogger(HakunaApplicationJson.class);

    final Map<String, Properties> contextProperties = new HashMap<>();

    public Properties getContextProperties(String context) {
        return contextProperties.get(context);
    }

    public Properties setContextProperties(String context, Properties to) {
        Properties from = contextProperties.get(context);
        if (from == null) {
            return to;
        }

        from.stringPropertyNames().forEach(key -> {
            to.setProperty(key, from.getProperty(key));
        });

        return to;
    }

    protected HakunaApplicationJson() {

    }

    public static HakunaApplicationJson readFromString(String json) {

        HakunaApplicationJson appJson = new HakunaApplicationJson();
        if (json == null || json.isEmpty()) {
            return appJson;
        }
        try {
            Map<String, Map<String, String>> contextJson = loadJson(appJson, json);
            contextJson.forEach((ctx, valuesAsMap) -> {
                Properties valuesAsProperties = new Properties();
                valuesAsMap.forEach((k, v) -> {
                    valuesAsProperties.setProperty(k, v);
                });
                appJson.contextProperties.put(ctx, valuesAsProperties);
            });
        } catch (JsonProcessingException e) {
            LOG.warn("Invalid HAKUNA_APPLICATION_JSON " + e);
        }

        return appJson;
    }

    protected static final ObjectMapper MAPPER = new ObjectMapper();

    protected static Map<String, Map<String, String>> loadJson(HakunaApplicationJson appJson, String json)
            throws JsonMappingException, JsonProcessingException {
        TypeReference<Map<String, Map<String, String>>> type = new TypeReference<Map<String, Map<String, String>>>() {
        };
        return MAPPER.readValue(json, type);

    }

}
