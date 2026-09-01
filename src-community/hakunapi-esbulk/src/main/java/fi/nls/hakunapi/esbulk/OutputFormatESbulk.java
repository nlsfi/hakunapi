package fi.nls.hakunapi.esbulk;

import fi.nls.hakunapi.core.FeatureCollectionWriter;
import fi.nls.hakunapi.core.OutputFormat;
import fi.nls.hakunapi.core.SingleFeatureWriter;

public class OutputFormatESbulk implements OutputFormat {

    public static final OutputFormat INSTANCE = new OutputFormatESbulk();

    public static final String ID = "esbulk";
    public static final String MIME_TYPE = "application/x-ndjson";

    private OutputFormatESbulk() {}

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
        return new ESbulkFeatureCollectionWriter();
    }

    @Override
    public SingleFeatureWriter getSingleFeatureWriter() {
        return new ESbulkSingleFeatureWriter();
    }

    @Override
    public String getMediaMainType() {
        return "application";
    }

    @Override
    public String getMediaSubType() {
        return "x-ndjson";
    }

}
