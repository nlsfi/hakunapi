package fi.nls.hakunapi.geojson.hakuna;

import java.util.Arrays;
import java.util.Map;

import fi.nls.hakunapi.core.FloatingPointFormatter;
import fi.nls.hakunapi.core.OutputFormat;
import fi.nls.hakunapi.core.OutputFormatFactorySpi;
import fi.nls.hakunapi.core.util.DefaultFloatingPointFormatter;
import fi.nls.hakunapi.core.util.FixedFloatingPoint3FormatterInt;

public class OutputFormatFactoryGeoJSON implements OutputFormatFactorySpi {

    // Selects FixedFloatingPoint3FormatterInt: ordinates always get three
    // decimals, faster to write but never trimmed ("123.400" for 123.4)
    public static final String FORMATTER_FIXED3 = "fixed3";

    @Override
    public boolean canCreate(Map<String, String> params) {
        return OutputFormatGeoJSON.TYPE.equals(params.get("type"));
    }

    @Override
    public OutputFormat create(Map<String, String> params) {
        boolean forceLonLat = "true".equalsIgnoreCase(params.get("forceLonLat"));

        FloatingPointFormatter formatterDegrees = parseFormatter(params.get("formatter.degrees"), DefaultFloatingPointFormatter.DEFAULT_DEGREES);
        FloatingPointFormatter formatterMeters = parseFormatter(params.get("formatter.meters"), DefaultFloatingPointFormatter.DEFAULT_METERS);

        return new OutputFormatGeoJSON(forceLonLat, formatterDegrees, formatterMeters);
    }

    public static FloatingPointFormatter parseFormatter(String s, FloatingPointFormatter fallback) {
        if (s == null || s.isBlank()) {
            return fallback;
        }
        if (FORMATTER_FIXED3.equalsIgnoreCase(s.trim())) {
            return FixedFloatingPoint3FormatterInt.INSTANCE;
        }
        int[] v = Arrays.stream(s.split(","))
                .map(String::trim)
                .mapToInt(Integer::parseInt)
                .toArray();
        if (v.length != 6) {
            throw new IllegalArgumentException("Expected six integers or '" + FORMATTER_FIXED3 + "'!");
        }
        return new DefaultFloatingPointFormatter(v[0], v[1], v[2], v[3], v[4], v[5]);

    }

}
