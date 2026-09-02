package fi.nls.hakunapi.core.i18n;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import fi.nls.hakunapi.core.ConformanceClass;
import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.FilterParser;
import fi.nls.hakunapi.core.MetadataFormat;
import fi.nls.hakunapi.core.OutputFormat;
import fi.nls.hakunapi.core.SRIDCode;
import fi.nls.hakunapi.core.schemas.ConformanceClasses;
import fi.nls.hakunapi.core.schemas.FunctionsContent;
import fi.nls.hakunapi.core.schemas.Link;
import fi.nls.hakunapi.core.telemetry.ServiceTelemetry;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

/**
 * A FeatureServiceConfig whose descriptive elements resolve in one language.
 *
 * @see FeatureServiceConfig#localized(String)
 */
public class LocalizedFeatureServiceConfig extends FeatureServiceConfig {

    private final FeatureServiceConfig service;
    private final String lang;
    private final Localization l10n;

    public LocalizedFeatureServiceConfig(FeatureServiceConfig service, String lang) {
        this.service = service;
        this.lang = lang;
        this.l10n = service.getLocalization();
    }

    public FeatureServiceConfig unwrap() {
        return service;
    }

    public String getLang() {
        return lang;
    }

    /* Localized */

    @Override
    public String getTitle() {
        return l10n.get(lang, LocalizableKeys.apiTitle(), service.getTitle());
    }

    @Override
    public String getDescription() {
        return l10n.get(lang, LocalizableKeys.apiDescription(), service.getDescription());
    }

    @Override
    public Collection<FeatureType> getCollections() {
        Collection<FeatureType> collections = service.getCollections();
        List<FeatureType> localized = new ArrayList<>(collections.size());
        for (FeatureType ft : collections) {
            localized.add(new LocalizedFeatureType(ft, lang, l10n));
        }
        return localized;
    }

    @Override
    public FeatureType getCollection(String name) {
        FeatureType ft = service.getCollection(name);
        return ft == null ? null : new LocalizedFeatureType(ft, lang, l10n);
    }

    /**
     * @return a sibling view rather than a wrapper stacked on this one
     */
    @Override
    public FeatureServiceConfig localized(String lang) {
        return service.localized(lang);
    }

    @Override
    public Localization getLocalization() {
        return service.getLocalization();
    }

    @Override
    public void setLocalization(Localization localization) {
        service.setLocalization(localization);
    }

    @Override
    public List<String> getLanguages() {
        return service.getLanguages();
    }

    /* Delegated */

    @Override
    public int getLimitDefault() {
        return service.getLimitDefault();
    }

    @Override
    public void setLimitDefault(int limitDefault) {
        service.setLimitDefault(limitDefault);
    }

    @Override
    public int getLimitMaximum() {
        return service.getLimitMaximum();
    }

    @Override
    public void setLimitMaximum(int limitMaximum) {
        service.setLimitMaximum(limitMaximum);
    }

    @Override
    public Info getInfo() {
        return service.getInfo();
    }

    @Override
    public void setInfo(Info info) {
        service.setInfo(info);
    }

    @Override
    public Map<String, Map<String, Object>> getSchemaExtensions() {
        return service.getSchemaExtensions();
    }

    @Override
    public void setSchemaExtensions(Map<String, Map<String, Object>> schemaExtensions) {
        service.setSchemaExtensions(schemaExtensions);
    }

    @Override
    public void setServers(List<Server> servers) {
        service.setServers(servers);
    }

    @Override
    public Server getServer() {
        return service.getServer();
    }

    @Override
    public void setServer(Server server) {
        service.setServer(server);
    }

    @Override
    @Deprecated
    public ConformanceClasses getConformance() {
        return service.getConformance();
    }

    @Override
    @Deprecated
    public void setConformance(ConformanceClasses conformance) {
        service.setConformance(conformance);
    }

    @Override
    public List<ConformanceClass> getConformanceClasses() {
        return service.getConformanceClasses();
    }

    @Override
    public void setConformanceClasses(List<ConformanceClass> conformsTo) {
        service.setConformanceClasses(conformsTo);
    }

    @Override
    public boolean conformsTo(ConformanceClass c) {
        return service.conformsTo(c);
    }

    @Override
    public Map<String, SecurityScheme> getSecuritySchemes() {
        return service.getSecuritySchemes();
    }

    @Override
    public void setSecuritySchemes(Map<String, SecurityScheme> securitySchemes) {
        service.setSecuritySchemes(securitySchemes);
    }

    @Override
    public List<SecurityRequirement> getSecurityRequirements() {
        return service.getSecurityRequirements();
    }

    @Override
    public void setSecurityRequirements(List<SecurityRequirement> securityRequirements) {
        service.setSecurityRequirements(securityRequirements);
    }

    @Override
    @Deprecated
    public String getCurrentServerURL() {
        return service.getCurrentServerURL();
    }

    @Override
    public String getCurrentServerURL(Function<String, String> dynamicValues) {
        return service.getCurrentServerURL(dynamicValues);
    }

    @Override
    public OutputFormat getOutputFormat(String f) {
        return service.getOutputFormat(f);
    }

    @Override
    public Collection<OutputFormat> getOutputFormats() {
        return service.getOutputFormats();
    }

    @Override
    public FunctionsContent getFunctions() {
        return service.getFunctions();
    }

    @Override
    public void setFunctions(FunctionsContent functionsContent) {
        service.setFunctions(functionsContent);
    }

    @Override
    public List<MetadataFormat> getMetadataFormats() {
        return service.getMetadataFormats();
    }

    @Override
    public void setMetadataFormats(List<MetadataFormat> metadataFormats) {
        service.setMetadataFormats(metadataFormats);
    }

    @Override
    public void setKnownSrids(List<SRIDCode> knownSrids) {
        service.setKnownSrids(knownSrids);
    }

    @Override
    public Optional<SRIDCode> getSridCode(int srid) {
        return service.getSridCode(srid);
    }

    @Override
    public boolean isCrsLatLon(int srid) {
        return service.isCrsLatLon(srid);
    }

    @Override
    public Collection<FilterParser> getFilterParsers() {
        return service.getFilterParsers();
    }

    @Override
    public FilterParser getFilterParser(String filterLang) {
        return service.getFilterParser(filterLang);
    }

    @Override
    public String getApiKeyQueryParam() {
        return service.getApiKeyQueryParam();
    }

    @Override
    public ServiceTelemetry getTelemetry() {
        return service.getTelemetry();
    }

    @Override
    public void setTelemetry(ServiceTelemetry usage) {
        service.setTelemetry(usage);
    }

    @Override
    public List<Link> getAdditionalLinks() {
        return service.getAdditionalLinks();
    }

    @Override
    public void setAdditionalLinks(List<Link> additionalLinks) {
        service.setAdditionalLinks(additionalLinks);
    }

}
