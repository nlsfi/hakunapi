package fi.nls.hakunapi.jsonfg;

import fi.nls.hakunapi.core.FeatureCollectionWriter;
import fi.nls.hakunapi.core.FloatingPointFormatter;
import fi.nls.hakunapi.core.OutputFormat;
import fi.nls.hakunapi.core.SingleFeatureWriter;

public class JSONFGOutputFormat implements OutputFormat {

    private final FloatingPointFormatter formatterDegrees;
    private final FloatingPointFormatter formatterMeters;

    JSONFGOutputFormat(FloatingPointFormatter formatterDegrees, FloatingPointFormatter formatterMeters) {
        this.formatterDegrees = formatterDegrees;
        this.formatterMeters = formatterMeters;
    }

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
        JSONFGFeatureCollectionWriter w = new JSONFGFeatureCollectionWriter();
        w.setFormatterFactory(isDegrees -> isDegrees ? formatterDegrees : formatterMeters);
        return w;
    }

    @Override
    public SingleFeatureWriter getSingleFeatureWriter() {
        JSONFGSingleFeatureWriter w = new JSONFGSingleFeatureWriter();
        w.setFormatterFactory(isDegrees -> isDegrees ? formatterDegrees : formatterMeters);
        return w;
    }

}
