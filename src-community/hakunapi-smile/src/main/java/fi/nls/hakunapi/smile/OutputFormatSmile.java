package fi.nls.hakunapi.smile;

import fi.nls.hakunapi.core.FeatureCollectionWriter;
import fi.nls.hakunapi.core.OutputFormat;
import fi.nls.hakunapi.core.SingleFeatureWriter;

public class OutputFormatSmile implements OutputFormat {

    public static final OutputFormat INSTANCE = new OutputFormatSmile();

    public static final String ID = "smile";
    public static final String MIME_TYPE = "application/x-jackson-smile";

    private OutputFormatSmile() {}

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    @Override
    public FeatureCollectionWriter getFeatureCollectionWriter() {
        return new SmileFeatureCollectionWriter();
    }

    @Override
    public SingleFeatureWriter getSingleFeatureWriter() {
        return new SmileSingleFeatureWriter();
    }

    @Override
    public String getMediaMainType() {
        return "application";
    }

    @Override
    public String getMediaSubType() {
        return "x-jackson-smile";
    }

}
