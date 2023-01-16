package fi.nls.hakunapi.geojson.hakuna;

import fi.nls.hakunapi.core.FeatureCollectionWriter;
import fi.nls.hakunapi.core.OutputFormat;
import fi.nls.hakunapi.core.SingleFeatureWriter;

public class OutputFormatGeoJSONTextSeq implements OutputFormat {

    public static final OutputFormat INSTANCE = new OutputFormatGeoJSONTextSeq();
    
    private OutputFormatGeoJSONTextSeq() {}

    @Override
    public String getId() {
        return "json-seq";
    }
    
    @Override
    public String getMimeType() {
        return HakunaGeoJSONFeatureCollectionWriterTextSeq.MIME_TYPE;
    }

    @Override
    public FeatureCollectionWriter getFeatureCollectionWriter() {
        return new HakunaGeoJSONFeatureCollectionWriterTextSeq();
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
        return "geo+json-seq";
    }

}
