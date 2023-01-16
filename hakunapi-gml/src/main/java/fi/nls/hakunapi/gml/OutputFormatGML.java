package fi.nls.hakunapi.gml;

import java.util.Collections;
import java.util.Map;

import fi.nls.hakunapi.core.FeatureCollectionWriter;
import fi.nls.hakunapi.core.OutputFormat;
import fi.nls.hakunapi.core.SingleFeatureWriter;

public class OutputFormatGML implements OutputFormat {

    public static final OutputFormat INSTANCE = new OutputFormatGML();

    private OutputFormatGML() {
    }

    @Override
    public String getId() {
        return "gml";
    }

    @Override
    public String getMimeType() {
        return WFS110FeatureCollectionWriter.MIME_TYPE;
    }

    @Override
    public FeatureCollectionWriter getFeatureCollectionWriter() {
        return new WFS110FeatureCollectionWriter();
    }

    @Override
    public SingleFeatureWriter getSingleFeatureWriter() {
        return new WFS110SingleFeatureWriter();
    }

    @Override
    public String getMediaMainType() {
        return "text";
    }

    @Override
    public String getMediaSubType() {
        return "xml";
    }

    @Override
    public Map<String, String> getMimeParameters() {
        return Collections.singletonMap("subtype", "3.1.1");
    }

}
