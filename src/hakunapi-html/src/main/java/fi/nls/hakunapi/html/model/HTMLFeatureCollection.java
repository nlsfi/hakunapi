package fi.nls.hakunapi.html.model;

import java.util.ArrayList;
import java.util.List;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.schemas.Link;
import fi.nls.hakunapi.html.OutputFormatHTMLSettings;

public class HTMLFeatureCollection {

    private int srid;
    private FeatureType featureType;
    private List<HTMLFeature> features;

    private String timestamp;
    private Integer numberReturned;
    private List<Link> links;
    private OutputFormatHTMLSettings settings;

    public HTMLFeatureCollection() {
        this.features = new ArrayList<>();
    }

    public int getSrid() {
        return srid;
    }

    public void setSrid(int srid) {
        this.srid = srid;
    }

    public void setFeatureType(FeatureType ft) {
        this.featureType = ft;
    }

    public FeatureType getFeatureType() {
        return featureType;
    }

    public void add(HTMLFeature feature) {
        features.add(feature);
    }

    public List<HTMLFeature> getFeatures() {
        return features;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setNumberReturned(int numberReturned) {
        this.numberReturned = numberReturned;
    }

    public Integer getNumberReturned() {
        return numberReturned;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public List<Link> getLinks() {
        return links;
    }

    public OutputFormatHTMLSettings getSettings() {
        return settings;
    }

    public void setSettings(OutputFormatHTMLSettings settings) {
        this.settings = settings;
    }

}
