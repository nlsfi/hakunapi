package fi.nls.hakunapi.core;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import fi.nls.hakunapi.core.schemas.ConformanceClasses;
import fi.nls.hakunapi.core.schemas.FunctionsContent;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import io.swagger.v3.oas.models.servers.Server;

public abstract class FeatureServiceConfig {

    protected int limitDefault;
    protected int limitMaximum;
    protected Info info;
    protected List<Server> servers;
    protected EnumSet<ConformanceClass> conformanceClasses;
    protected Map<String, SecurityScheme> securitySchemes;
    protected List<SecurityRequirement> securityRequirements;
    protected Map<String, Map<String, Object>> schemaExtensions;
    protected FunctionsContent functionsContent;

    public int getLimitDefault() {
        return limitDefault;
    }

    public void setLimitDefault(int limitDefault) {
        this.limitDefault = limitDefault;
    }

    public int getLimitMaximum() {
        return limitMaximum;
    }

    public void setLimitMaximum(int limitMaximum) {
        this.limitMaximum = limitMaximum;
    }

    public Info getInfo() {
        return info;
    }

    public void setInfo(Info info) {
        this.info = info;
    }

    public Map<String, Map<String, Object>> getSchemaExtensions() {
        if (schemaExtensions == null) {
            return Collections.emptyMap();
        }
        return schemaExtensions;
    }

    public void setSchemaExtensions(Map<String, Map<String, Object>> schemaExtensions) {
        this.schemaExtensions = schemaExtensions;
    }

    public List<Server> getServers() {
        return servers;
    }

    public void setServers(List<Server> servers) {
        this.servers = servers;
    }

    /**
     * @return ConformanceClasses implemented by this service 
     * @deprecated for removal, if you need ConformanceClasses create one yourself
     */
    @Deprecated
    public ConformanceClasses getConformance() {
        return new ConformanceClasses(getConformanceClasses());
    }

    /**
     * @param conformance conformance classes implements by this service
     * @deprecated for removal, use setConformanceClasses() instead!
     */
    @Deprecated
    public void setConformance(ConformanceClasses conformance) {
        setConformanceClasses(
                conformance.conformsTo.stream().map(ConformanceClass::valueOf).collect(Collectors.toList()));
    }

    public List<ConformanceClass> getConformanceClasses() {
        return conformanceClasses == null ? Collections.emptyList()
                : conformanceClasses.stream().collect(Collectors.toList());
    }

    public void setConformanceClasses(List<ConformanceClass> conformsTo) {
        this.conformanceClasses = EnumSet.copyOf(conformsTo);
    }

    public boolean conformsTo(ConformanceClass c) {
        return conformanceClasses.contains(c);
    }

    public Map<String, SecurityScheme> getSecuritySchemes() {
        if (securitySchemes == null) {
            return Collections.emptyMap();
        }
        return securitySchemes;
    }

    public void setSecuritySchemes(Map<String, SecurityScheme> securitySchemes) {
        this.securitySchemes = securitySchemes;
    }

    public List<SecurityRequirement> getSecurityRequirements() {
        return securityRequirements;
    }

    public void setSecurityRequirements(List<SecurityRequirement> securityRequirements) {
        this.securityRequirements = securityRequirements;
    }

    public String getCurrentServerURL() {
        return servers.get(0).getUrl();
    }

    public abstract Collection<FeatureType> getCollections();

    public abstract FeatureType getCollection(String name);

    public abstract OutputFormat getOutputFormat(String f);

    public abstract Collection<OutputFormat> getOutputFormats();

    public FunctionsContent getFunctions() {
        return functionsContent;
    }

    public void setFunctions(FunctionsContent functionsContent) {
        this.functionsContent = functionsContent;
    }

    public String getTitle() {
        return info.getTitle();
    }

    public String getDescription() {
        return info.getDescription();
    }

    public Collection<FilterParser> getFilterParsers() {
        return null;
    }

    public FilterParser getFilterParser(String filterLang) {
        return null;
    }

    public String getApiKeyQueryParam() {
        return getSecuritySchemes().values().stream().filter(it -> it.getType() == Type.APIKEY)
                .filter(it -> it.getIn() == In.QUERY).findAny().map(it -> it.getName()).orElse(null);
    }

}
