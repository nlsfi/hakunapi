package fi.nls.hakunapi.geojson.hakuna;

import fi.nls.hakunapi.core.FeatureCollectionWriter;
import fi.nls.hakunapi.core.OutputFormat;
import fi.nls.hakunapi.core.SingleFeatureWriter;

public class OutputFormatGeoJSON implements OutputFormat {

    public static final OutputFormat INSTANCE = new OutputFormatGeoJSON(false);
    public static final OutputFormat INSTANCE_LON_LAT = new OutputFormatGeoJSON(true);
    public static final String TYPE = "json";

    private final boolean forceLonLat;

    OutputFormatGeoJSON(boolean forceLonLat) {
        this.forceLonLat = forceLonLat;
    }

    @Override
    public String getId() {
        return TYPE;
    }

    @Override
    public String getMimeType() {
        return HakunaGeoJSONFeatureCollectionWriter.MIME_TYPE;
    }

    @Override
    public FeatureCollectionWriter getFeatureCollectionWriter() {
        HakunaGeoJSONFeatureCollectionWriter w = new HakunaGeoJSONFeatureCollectionWriter();
        w.setForceLonLat(forceLonLat);
        return w;
    }

    @Override
    public SingleFeatureWriter getSingleFeatureWriter() {
        HakunaGeoJSONSingleFeatureWriter w = new HakunaGeoJSONSingleFeatureWriter();
        w.setForceLonLat(forceLonLat);
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
