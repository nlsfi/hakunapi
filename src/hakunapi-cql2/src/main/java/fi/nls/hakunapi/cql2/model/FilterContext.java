package fi.nls.hakunapi.cql2.model;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.SRIDCode;
import fi.nls.hakunapi.core.property.HakunaProperty;

public class FilterContext {

    public final Map<String, HakunaProperty> queryables;
    public final SRIDCode filterSrid;

    public FilterContext(FeatureType ft, SRIDCode filterSrid) {
        this(ft.getQueryableProperties(), filterSrid);
    }

    public FilterContext(List<HakunaProperty> queryables, SRIDCode filterSrid) {
        this.queryables = queryables.stream()
            .collect(Collectors.toMap(HakunaProperty::getName, it -> it));
        this.filterSrid = filterSrid;
    }

}
