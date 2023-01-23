package fi.nls.hakunapi.html;

import freemarker.template.Configuration;
import fi.nls.hakunapi.core.FeatureCollectionWriter;
import fi.nls.hakunapi.core.OutputFormat;
import fi.nls.hakunapi.core.SingleFeatureWriter;

public class OutputFormatHTML implements OutputFormat {

    public static final String ID = "html";
    public static final String MIME_TYPE = "text/html";

    private final Configuration configuration;

    public OutputFormatHTML(Configuration configuration) {
        this.configuration = configuration;
    }
    
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getMediaMainType() {
        return "text";
    }

    @Override
    public String getMediaSubType() {
        return "html";
    }

    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    @Override
    public FeatureCollectionWriter getFeatureCollectionWriter() {
        return new HTMLFeatureCollectionWriter(configuration);
    }

    @Override
    public SingleFeatureWriter getSingleFeatureWriter() {
        return new HTMLSingleFeatureWriter(configuration);
    }

}
