package fi.nls.hakunapi.geojson.hakuna;

import fi.nls.hakunapi.core.FeatureCollectionWriter;
import fi.nls.hakunapi.core.OutputFormat;
import fi.nls.hakunapi.core.SingleFeatureWriter;

public class OutputFormatGeoJSON implements OutputFormat {

    public static final OutputFormat INSTANCE = new OutputFormatGeoJSON();
    public static final String TYPE = "json";

    private OutputFormatGeoJSON() {}

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
        return new HakunaGeoJSONFeatureCollectionWriter();
    }

    @Override
    public SingleFeatureWriter getSingleFeatureWriter() {
        return new HakunaGeoJSONSingleFeatureWriter();
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
