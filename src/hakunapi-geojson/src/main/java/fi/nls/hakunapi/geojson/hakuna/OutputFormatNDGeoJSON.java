package fi.nls.hakunapi.geojson.hakuna;

import fi.nls.hakunapi.core.FeatureCollectionWriter;
import fi.nls.hakunapi.core.OutputFormat;
import fi.nls.hakunapi.core.SingleFeatureWriter;

public class OutputFormatNDGeoJSON implements OutputFormat {

    public static final String MIME_TYPE = "application/geo+json-ndjson";

    public static final OutputFormat INSTANCE = new OutputFormatNDGeoJSON();
    
    private OutputFormatNDGeoJSON() {}

    @Override
    public String getId() {
        return "ndjson";
    }

    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    @Override
    public FeatureCollectionWriter getFeatureCollectionWriter() {
        return new HakunaGeoJSONFeatureCollectionWriterNDJSON();
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
        return "geo+json-ndjson";
    }

}
