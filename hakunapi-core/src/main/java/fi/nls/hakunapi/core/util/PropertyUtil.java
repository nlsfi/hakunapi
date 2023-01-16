package fi.nls.hakunapi.core.util;

import java.util.Properties;

public class PropertyUtil {

    public static int getInt(Properties properties, String key, int defaultValue) {
        try {
            return Integer.parseInt(properties.getProperty(key));
        } catch (Exception ignore) {
            return defaultValue;
        }
    }

}
