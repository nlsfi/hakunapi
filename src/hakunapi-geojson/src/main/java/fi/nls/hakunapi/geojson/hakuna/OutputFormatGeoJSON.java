package fi.nls.hakunapi.geojson.hakuna;

import fi.nls.hakunapi.core.FeatureCollectionWriter;
import fi.nls.hakunapi.core.FloatingPointFormatter;
import fi.nls.hakunapi.core.OutputFormat;
import fi.nls.hakunapi.core.SingleFeatureWriter;
import fi.nls.hakunapi.core.util.DefaultFloatingPointFormatter;

public class OutputFormatGeoJSON implements OutputFormat {

    public static final OutputFormatGeoJSON INSTANCE = new OutputFormatGeoJSON(false, DefaultFloatingPointFormatter.DEFAULT_DEGREES, DefaultFloatingPointFormatter.DEFAULT_METERS);

    public static final String MIME_TYPE = "application/geo+json";

    public static final String TYPE = "json";

    private final boolean forceLonLat;
    private final FloatingPointFormatter formatterDegrees;
    private final FloatingPointFormatter formatterMeters;

    OutputFormatGeoJSON(boolean forceLonLat, FloatingPointFormatter formatterDegrees, FloatingPointFormatter formatterMeters) {
        this.forceLonLat = forceLonLat;
        this.formatterDegrees = formatterDegrees;
        this.formatterMeters = formatterMeters;
    }

    @Override
    public String getId() {
        return TYPE;
    }

    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    @Override
    public FeatureCollectionWriter getFeatureCollectionWriter() {
        HakunaGeoJSONFeatureCollectionWriter w = new HakunaGeoJSONFeatureCollectionWriter();
        w.setForceLonLat(forceLonLat);
        w.setFormatterFactory(isDegrees -> isDegrees ? formatterDegrees : formatterMeters);
        return w;
    }

    @Override
    public SingleFeatureWriter getSingleFeatureWriter() {
        HakunaGeoJSONSingleFeatureWriter w = new HakunaGeoJSONSingleFeatureWriter();
        w.setForceLonLat(forceLonLat);
        w.setFormatterFactory(isDegrees -> isDegrees ? formatterDegrees : formatterMeters);
        return w;
    }

    @Override
    public String getMediaMainType() {
        return "application";
    }

    @Override
    public String getMediaSubType() {
        return "geo+json";
    }

}
