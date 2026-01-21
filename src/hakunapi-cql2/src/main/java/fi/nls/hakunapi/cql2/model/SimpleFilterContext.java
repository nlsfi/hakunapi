package fi.nls.hakunapi.cql2.model;

import java.util.Optional;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.SRIDCode;
import fi.nls.hakunapi.core.projection.ProjectionTransformerFactory;
import fi.nls.hakunapi.core.property.HakunaProperty;

public class SimpleFilterContext implements FilterContext {

    private final FeatureType ft;
    private final SRIDCode filterSrid;

    public SimpleFilterContext(FeatureType ft, SRIDCode filterSrid) {
        this.ft = ft;
        this.filterSrid = filterSrid;
    }

    @Override
    public Optional<HakunaProperty> queryable(String name) {
        return ft.getQueryableProperties().stream()
                .filter(prop -> prop.getName().equals(name))
                .findAny();
    }

    @Override
    public Optional<SRIDCode> storageSrid() {
        return ft.getGeom() == null ? Optional.empty() : ft.getSrid(ft.getGeom().getStorageSRID());
    }

    @Override
    public Optional<ProjectionTransformerFactory> projectionTransformer() {
        return Optional.ofNullable(ft.getProjectionTransformerFactory());
    }
    @Override
    public SRIDCode filterSrid() {
        return filterSrid;
    }

}
