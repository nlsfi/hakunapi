package fi.nls.hakunapi.cql2.model;

import java.util.Optional;

import fi.nls.hakunapi.core.SRIDCode;
import fi.nls.hakunapi.core.projection.ProjectionTransformerFactory;
import fi.nls.hakunapi.core.property.HakunaProperty;

public interface FilterContext {

    public Optional<HakunaProperty> queryable(String name);
    public Optional<SRIDCode> storageSrid();
    public Optional<ProjectionTransformerFactory> projectionTransformer();
    public SRIDCode filterSrid();

}
