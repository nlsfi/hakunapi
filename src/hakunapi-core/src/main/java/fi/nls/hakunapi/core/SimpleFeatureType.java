package fi.nls.hakunapi.core;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.geom.HakunaGeometryDimension;
import fi.nls.hakunapi.core.param.GetFeatureParam;
import fi.nls.hakunapi.core.projection.ProjectionTransformerFactory;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;

public abstract class SimpleFeatureType implements FeatureType {

    private String name;
    private String title;
    private String description;
    private HakunaProperty id;
    private HakunaPropertyGeometry geom;
    private HakunaGeometryDimension geomDimension = HakunaGeometryDimension.DEFAULT;
    private List<HakunaProperty> properties;
    private List<HakunaProperty> queryableProperties;
    private List<DatetimeProperty> datetimeProperties;
    private PaginationStrategy paginationStrategy;
    private List<OrderBy> defaultOrderBy;
    private CacheSettings cacheSettings;

    private double[] spatialExtent;
    private Instant[] temporalExtent;
    private List<GetFeatureParam> parameters;
    private List<Filter> staticFilters;
    private ProjectionTransformerFactory transformerFactory;
    private Map<String, Object> metadata;
    
    public abstract FeatureProducer getFeatureProducer();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public HakunaProperty getId() {
        return id;
    }

    public void setId(HakunaProperty id) {
        this.id = id;
        id.setFeatureType(this);
    }

    public HakunaPropertyGeometry getGeom() {
        return geom;
    }

    public void setGeom(HakunaPropertyGeometry geom) {
        this.geom = geom;
        geom.setFeatureType(this);
    }

    public List<HakunaProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<HakunaProperty> properties) {
        this.properties = properties;
        for (HakunaProperty p : properties) {
            p.setFeatureType(this);
        }
    }

    @Override
    public List<HakunaProperty> getQueryableProperties() {
        if (queryableProperties == null) {
            return Collections.emptyList();
        }
        return queryableProperties;
    }

    public void setQueryableProperties(List<HakunaProperty> queryableProperties) {
        this.queryableProperties = queryableProperties;
    }

    @Override
    public List<DatetimeProperty> getDatetimeProperties() {
        return datetimeProperties;
    }

    public void setDatetimeProperties(List<DatetimeProperty> datetimeProperties) {
        this.datetimeProperties = datetimeProperties;
    }

    @Override
    public PaginationStrategy getPaginationStrategy() {
        return paginationStrategy;
    }

    public void setPaginationStrategy(PaginationStrategy paginationStrategy) {
        this.paginationStrategy = paginationStrategy;
    }

    @Override
    public List<OrderBy> getDefaultOrderBy() {
        return defaultOrderBy;
    }

    public void setDefaultOrderBy(List<OrderBy> defaultOrderBy) {
        this.defaultOrderBy = defaultOrderBy;
    }

    @Override
    public CacheSettings getCacheSettings() {
        return cacheSettings;
    }

    public void setCacheSettings(CacheSettings cacheSettings) {
        this.cacheSettings = cacheSettings;
    }

    @Override
    public String getNS() {
        return null;
    }

    @Override
    public String getSchemaLocation() {
        return null;
    }

    public List<GetFeatureParam> getParameters() {
        if (parameters == null) {
            return Collections.emptyList();
        }
        return parameters;
    }

    public void setParameters(List<GetFeatureParam> parameters) {
        this.parameters = parameters;
    }

    @Override
    public List<Filter> getStaticFilters() {
        return staticFilters;
    }

    public void setStaticFilters(List<Filter> staticFilters) {
        this.staticFilters = staticFilters;
    }

    @Override
    public ProjectionTransformerFactory getProjectionTransformerFactory() {
        return transformerFactory;
    }

    public void setProjectionTransformerFactory(ProjectionTransformerFactory f) {
        this.transformerFactory = f;
    }

    @Override
    public double[] getSpatialExtent() {
        return spatialExtent;
    }

    public void setSpatialExtent(double[] spatialExtent) {
        this.spatialExtent = spatialExtent;
    }

    @Override
    public Instant[] getTemporalExtent() {
        return temporalExtent;
    }

    public void setTemporalExtent(Instant[] temporalExtent) {
        this.temporalExtent = temporalExtent;
    }

    public HakunaGeometryDimension getGeomDimension() {
        return geomDimension;
    }

    public void setGeomDimension(HakunaGeometryDimension geomDimension) {
        this.geomDimension = geomDimension;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    @Override
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
}
