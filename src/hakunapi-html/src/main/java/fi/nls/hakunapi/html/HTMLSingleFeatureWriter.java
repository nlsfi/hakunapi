package fi.nls.hakunapi.html;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateNotFoundException;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.SingleFeatureWriter;
import fi.nls.hakunapi.core.schemas.Link;
import fi.nls.hakunapi.html.model.HTMLFeature;
import fi.nls.hakunapi.html.model.PropertiesContextMap;

public class HTMLSingleFeatureWriter extends HTMLFeatureWriterBase implements SingleFeatureWriter {
    
    private static final String TEMPLATE_PATTERN_COLLECTION_ID_SRID = "feature_%s_%d.ftl";
    private static final String TEMPLATE_PATTERN_COLLECTION_ID = "feature_%s.ftl";
    private static final String TEMPLATE_GENERIC_SRID = "feature_%d.ftl";
    private static final String TEMPLATE_GENERIC = "feature.ftl";

    @Deprecated
    public HTMLSingleFeatureWriter(Configuration configuration) {
        super(configuration);
    }

    public HTMLSingleFeatureWriter(Configuration configuration, OutputFormatHTMLSettings settings) {
        super(configuration, settings);
    }

    @Override
    public void end(boolean timeStamp, List<Link> links, int numberReturned) {
        feature.setTimestamp(Instant.now().toString());
        feature.setNumberReturned(numberReturned);
        feature.setLinks(links);
    }

    @Override
    public void startFeature(FeatureType ft, String layername, long fid) throws Exception {
        startFeature(ft, layername, Long.toString(fid));
    }

    @Override
    public void startFeature(FeatureType ft, String layername, String fid) throws Exception {
        this.feature = new HTMLFeature();
        this.feature.setId(fid);
        this.feature.setFeatureType(ft);
        this.feature.setSettings(settings);
        this.properties = new PropertiesContextMap(feature.getProperties(), null);
        Template template = getTemplate(ft, srid);
        this.environment = template.createProcessingEnvironment(feature, new OutputStreamWriter(out, StandardCharsets.UTF_8));
        this.environment.setOutputEncoding(StandardCharsets.UTF_8.name());
    }

    @Override
    public void endFeature() throws Exception {
        // NOP
    }
    
    private Template getTemplate(FeatureType ft, int srid) throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException {
        try { return configuration.getTemplate(String.format(TEMPLATE_PATTERN_COLLECTION_ID_SRID, ft.getName(), srid)); } catch (Exception ignore) {}
        try { return configuration.getTemplate(String.format(TEMPLATE_PATTERN_COLLECTION_ID, ft.getName())); } catch (Exception ignore) {}
        try { return configuration.getTemplate(String.format(TEMPLATE_GENERIC_SRID, srid)); } catch (Exception ignore) {}
        return configuration.getTemplate(TEMPLATE_GENERIC);
    }

}
