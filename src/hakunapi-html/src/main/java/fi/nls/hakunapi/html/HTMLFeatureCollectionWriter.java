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
import fi.nls.hakunapi.core.FeatureCollectionWriter;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.schemas.Link;
import fi.nls.hakunapi.html.model.HTMLFeature;
import fi.nls.hakunapi.html.model.HTMLFeatureCollection;
import fi.nls.hakunapi.html.model.PropertiesContextMap;

public class HTMLFeatureCollectionWriter extends HTMLFeatureWriterBase implements FeatureCollectionWriter {

    private static final String TEMPLATE_PATTERN_COLLECTION_ID_SRID = "featureCollection_%s_%d.ftl";
    private static final String TEMPLATE_PATTERN_COLLECTION_ID = "featureCollection_%s.ftl";
    private static final String TEMPLATE_GENERIC_SRID = "featureCollection_%d.ftl";
    private static final String TEMPLATE_GENERIC = "featureCollection.ftl";

    private HTMLFeatureCollection model;

    @Deprecated
    public HTMLFeatureCollectionWriter(Configuration configuration) {
        this(configuration, new OutputFormatHTMLSettings());
    }

    public HTMLFeatureCollectionWriter(Configuration configuration, OutputFormatHTMLSettings settings) {
        super(configuration, settings);
    }

    @Override
    public void end(boolean timeStamp, List<Link> links, int numberReturned) {
        model.setTimestamp(Instant.now().toString());
        model.setNumberReturned(numberReturned);
        model.setLinks(links);
    }

    @Override
    public void startFeatureCollection(FeatureType ft, String layername) throws Exception {
        this.model = new HTMLFeatureCollection();
        model.setSrid(srid);
        model.setFeatureType(ft);
        model.setSettings(settings);
        Template template = getTemplate(ft, srid);
        this.environment = template.createProcessingEnvironment(model, new OutputStreamWriter(out, StandardCharsets.UTF_8));
        this.environment.setOutputEncoding(StandardCharsets.UTF_8.name());
    }

    private Template getTemplate(FeatureType ft, int srid) throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException {
        try { return configuration.getTemplate(String.format(TEMPLATE_PATTERN_COLLECTION_ID_SRID, ft.getName(), srid)); } catch (Exception ignore) {}
        try { return configuration.getTemplate(String.format(TEMPLATE_PATTERN_COLLECTION_ID, ft.getName())); } catch (Exception ignore) {}
        try { return configuration.getTemplate(String.format(TEMPLATE_GENERIC_SRID, srid)); } catch (Exception ignore) {}
        return configuration.getTemplate(TEMPLATE_GENERIC);
    }

    @Override
    public void endFeatureCollection() {
        // NOP
    }

    @Override
    public void startFeature(long fid) throws Exception {
        startFeature(Long.toString(fid));
    }

    @Override
    public void startFeature(int fid) throws Exception {
        startFeature(Integer.toString(fid));
    }

    @Override
    public void startFeature(String fid) throws Exception {
        this.feature = new HTMLFeature();
        this.feature.setId(fid);
        this.model.add(feature);
        this.properties = new PropertiesContextMap(feature.getProperties(), null);
    }

    @Override
    public void endFeature() throws Exception {
        // NOP
    }

}
