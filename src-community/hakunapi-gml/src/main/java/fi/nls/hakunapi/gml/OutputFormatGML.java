package fi.nls.hakunapi.gml;

import java.util.Collections;
import java.util.Map;

import fi.nls.hakunapi.core.FeatureCollectionWriter;
import fi.nls.hakunapi.core.OutputFormat;
import fi.nls.hakunapi.core.SingleFeatureWriter;
import fi.nls.hakunapi.core.util.DefaultFloatingPointFormatter;

public class OutputFormatGML implements OutputFormat {
    
    public static final String MIME_TYPE = "text/xml; subtype=3.1.1";

    public static final OutputFormat INSTANCE = new OutputFormatGML();

    private OutputFormatGML() {
    }

    @Override
    public String getId() {
        return "gml";
    }

    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    @Override
    public FeatureCollectionWriter getFeatureCollectionWriter() {
        WFS110FeatureCollectionWriter w = new WFS110FeatureCollectionWriter();
        w.setFormatter(new DefaultFloatingPointFormatter(0, 5, 0, 8, 0, 8));
        return w;
    }

    @Override
    public SingleFeatureWriter getSingleFeatureWriter() {
        WFS110SingleFeatureWriter w = new WFS110SingleFeatureWriter();
        w.setFormatter(new DefaultFloatingPointFormatter(0, 5, 0, 8, 0, 8));
        return w;        
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
