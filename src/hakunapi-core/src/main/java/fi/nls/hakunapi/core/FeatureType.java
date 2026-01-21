package fi.nls.hakunapi.core;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.param.GetFeatureParam;
import fi.nls.hakunapi.core.projection.ProjectionTransformerFactory;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;

public interface FeatureType {

    public String getName();
    public String getNS();
    public String getSchemaLocation();
    public String getTitle();
    public String getDescription();
    public Map<String, Object> getMetadata();

    public HakunaProperty getId();
    public HakunaPropertyGeometry getGeom();
    public List<HakunaProperty> getProperties();
    public default List<HakunaProperty> getSchemaProperties() { return getProperties(); } 
    public List<HakunaProperty> getQueryableProperties();
    public List<DatetimeProperty> getDatetimeProperties();

    @Deprecated
    public default boolean isWriteNullProperties() {
        return true;
    }

    public double[] getSpatialExtent();
    public Instant[] getTemporalExtent();

    public List<GetFeatureParam> getParameters();
    public default List<GetFeatureParam> getConformanceParams(List<GetFeatureParam> conformanceSpecificParams) {
        return conformanceSpecificParams;
    }
    public List<Filter> getStaticFilters();
    
    public ProjectionTransformerFactory getProjectionTransformerFactory();
    public default boolean isSourceWillProject() {
        return false;
    }

    public PaginationStrategy getPaginationStrategy();
    public default List<OrderBy> getDefaultOrderBy() {
        return Collections.emptyList();
    }

    public FeatureProducer getFeatureProducer();

    public default CacheSettings getCacheSettings() {
        return null;
    }

    public Optional<SRIDCode> getSrid(int srid);

}
