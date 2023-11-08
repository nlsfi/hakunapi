package fi.nls.hakunapi.jsonfg;

import fi.nls.hakunapi.core.FeatureCollectionWriter;
import fi.nls.hakunapi.core.OutputFormat;
import fi.nls.hakunapi.core.SingleFeatureWriter;

public class JSONFGOutputFormat implements OutputFormat {

    public static final OutputFormat INSTANCE = new JSONFGOutputFormat();

    @Override
    public String getId() {
        return JSONFG.TYPE;
    }

    @Override
    public String getMediaMainType() {
        return JSONFG.MEDIA_MAIN_TYPE;
    }

    @Override
    public String getMediaSubType() {
        return JSONFG.MEDIA_SUB_TYPE;
    }

    @Override
    public String getMimeType() {
        return JSONFG.MIME_TYPE;
    }

    @Override
    public FeatureCollectionWriter getFeatureCollectionWriter() {
        return new JSONFGFeatureCollectionWriter();
    }

    @Override
    public SingleFeatureWriter getSingleFeatureWriter() {
        return new JSONFGSingleFeatureWriter();
    }

}
