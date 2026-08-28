package fi.nls.hakunapi.html.model;

import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.SRIDCode;
import fi.nls.hakunapi.core.schemas.Link;
import fi.nls.hakunapi.html.OutputFormatHTMLSettings;

public class HTMLFeature {
    
    private String id;
    private String geometry;
    private Map<String, Object> properties;

    private FeatureType featureType;
    private int srid;
    private SRIDCode sridCode;

    private String timestamp;
    private Integer numberReturned;
    private List<Link> links;
    private OutputFormatHTMLSettings settings;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGeometry() {
        return geometry;
    }

    public void setGeometry(String geometry) {
        this.geometry = geometry;
    }

    public Map<String, Object> getProperties() {
        if (properties == null) {
            properties = new LinkedHashMap<>();
        }
        return properties;
    }

    public FeatureType getFeatureType() {
        return featureType;
    }

    public void setFeatureType(FeatureType featureType) {
        this.featureType = featureType;
    }

    public int getSrid() {
        return srid;
    }

    public void setSrid(int srid) {
        this.srid = srid;
    }

    public SRIDCode getSridCode() {
        return sridCode;
    }

    public void setSridCode(SRIDCode sridCode) {
        this.sridCode = sridCode;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getNumberReturned() {
        return numberReturned;
    }

    public void setNumberReturned(Integer numberReturned) {
        this.numberReturned = numberReturned;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public OutputFormatHTMLSettings getSettings() {
        return settings;
    }

    public void setSettings(OutputFormatHTMLSettings settings) {
        this.settings = settings;
    }


    private final HTMLLanguageContext language = new HTMLLanguageContext();

    /**
     * @return the language chrome for the templates: resolved language,
     *         switchable languages and the query fragment carrying it
     */
    public HTMLLanguageContext getLanguage() {
        return language;
    }

    public void setLang(String lang) {
        language.setLang(lang);
    }

    public void setAvailableLanguages(List<String> availableLanguages) {
        language.setAvailableLanguages(availableLanguages);
    }

    /** @return shorthand for getLanguage().getLang(), for the html lang attribute */
    public String getLang() {
        return language.getLang();
    }

    public List<String> getAvailableLanguages() {
        return language.getAvailableLanguages();
    }

    public List<String> getAlternateLanguages() {
        return language.getAlternateLanguages();
    }

    public String getLangQuery() {
        return language.getLangQuery();
    }

}
