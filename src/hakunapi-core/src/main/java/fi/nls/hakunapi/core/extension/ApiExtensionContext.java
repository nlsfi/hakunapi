package fi.nls.hakunapi.core.extension;

import java.nio.file.Path;
import java.util.Map;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.config.HakunaConfigParser;

public record ApiExtensionContext(HakunaConfigParser config, Path configPath,
        Map<String, FeatureType> collections) {

}
