package fi.nls.hakunapi.html;

import java.util.HashMap;
import java.util.Map;

import freemarker.template.Configuration;
import fi.nls.hakunapi.core.FeatureCollectionWriter;
import fi.nls.hakunapi.core.OutputFormat;
import fi.nls.hakunapi.core.SingleFeatureWriter;

public class OutputFormatHTML implements OutputFormat {

    public static final String ID = "html";
    public static final String MIME_TYPE = "text/html";

    private final Configuration configuration;
    private final OutputFormatHTMLSettings settings;
    private final Map<Integer, OutputFormatHTMLSettings> sridSettings;

    public OutputFormatHTML(Configuration configuration, OutputFormatHTMLSettings settings) {
        this(configuration, settings, new HashMap<>());
    }

    public OutputFormatHTML(Configuration configuration, OutputFormatHTMLSettings settings, Map<Integer, OutputFormatHTMLSettings> sridSettings) {
        this.configuration = configuration;
        this.settings = settings;
        this.sridSettings = sridSettings;
    }
    
    public Configuration getConfiguration() {
        return configuration;
    }

    public OutputFormatHTMLSettings getSettingsForSrid(int srid) {
        return sridSettings.getOrDefault(srid, settings);
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
        return new HTMLFeatureCollectionWriter(configuration, settings, sridSettings);
    }

    @Override
    public SingleFeatureWriter getSingleFeatureWriter() {
        return new HTMLSingleFeatureWriter(configuration, settings, sridSettings);
    }

}
