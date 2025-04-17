package fi.nls.hakunapi.core.request;

import java.util.ArrayList;
import java.util.List;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.OrderBy;
import fi.nls.hakunapi.core.PaginationStrategy;
import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.property.HakunaProperty;

public class GetFeatureCollection {

    private FeatureType ft;
    private List<HakunaProperty> properties;
    private List<Filter> filters;
    private String layername;
    private List<OrderBy> orderBy;
    private PaginationStrategy paginationStrategy;

    public GetFeatureCollection(FeatureType ft) {
        this.ft = ft;
        this.properties = getPropertiesBase(ft);
        this.properties.addAll(ft.getProperties());
        this.filters = new ArrayList<>(ft.getStaticFilters());
    }

    public String getName() {
        if (layername != null && !layername.isEmpty()) {
            return layername;
        }
        return ft.getName();
    }

    public void setLayername(String layername) {
        this.layername = layername;
    }

    public FeatureType getFt() {
        return ft;
    }

    public List<HakunaProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<HakunaProperty> properties) {
        this.properties = properties;
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public void addFilter(Filter f) {
        if (f != null) {
            filters.add(f);    
        }
    }

    public List<OrderBy> getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(List<OrderBy> orderBy) {
        this.orderBy = orderBy;
    }

    public PaginationStrategy getPaginationStrategy() {
        return paginationStrategy != null ? paginationStrategy : ft.getPaginationStrategy();
    }

    public void setPaginationStrategy(PaginationStrategy paginationStrategy) {
        this.paginationStrategy = paginationStrategy;
    }

    public List<HakunaProperty> getPropertiesBase(FeatureType ft) {
        List<HakunaProperty> properties = new ArrayList<>();
        if (orderBy != null) {
            properties.addAll(orderBy.stream().map(o -> o.getProperty()).toList());
        }
        properties.add(ft.getId());
        if (ft.getGeom() != null) {
            properties.add(ft.getGeom());
        }
        return properties;
    }

}
